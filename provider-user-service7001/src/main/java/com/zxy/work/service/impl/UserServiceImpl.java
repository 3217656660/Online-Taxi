package com.zxy.work.service.impl;


import com.zxy.work.dao.UserMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.User;
import com.zxy.work.service.EmailService;
import com.zxy.work.service.UserService;
import com.zxy.work.util.cache.CacheUtil;
import com.zxy.work.util.encode.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Random;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CacheUtil redisUtil;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private EmailService emailService;


    /**
     * 设置通用缓存TTL(30分钟)
     */
    private static final int cacheTTL = 30 * 60;

    /**
     * 设置缓存通用key前缀
     */
    private static final String commonKey = "user:mobile:";

    /**
     * kafka topic name
     */
    private static final String TOPIC_NAME = "users";

    /**
     * 设置缓存消息key
     */
    private static final String MQ_SET_CACHE_KEY = "setCache";

    /**
     * 移除缓存消息key
     */
    private static final String MQ_REMOVE_CACHE_KEY = "removeCache";

    /**
     * 发送邮箱验证码消息key
     */
    private static final String MQ_SEND_EMAIL_KEY = "sendEmail";

    /**
     * 用于不需要指定顺序的消息随机分区
     */
    private static final Random random = new Random();


    /**
     * 用户注册
     * @param user 传来的用户信息
     * @return  注册结果
     */
    @Transactional
    @Override
    public int create(User user) throws MyException {
        //先检查手机号是否已经注册
        User registeredUser;
        try{
            registeredUser = userMapper.selectByMobile(user.getMobile());
        }catch (Exception e){
            log.error("手机号查询用户异常，msg={}", e.getMessage());
            throw new MyException("手机号查询用户出现异常");
        }
        if (registeredUser != null){
            log.info("手机号={}，已经注册", registeredUser.getMobile());
            throw new MyException("手机号已经注册过了");
        }

        //没有注册则对密码进行加密，执行插入
        String encodedPassword = PasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        try{
            int result = userMapper.create(user);
            if (result == 1) {
                //随机分发到0-2分区
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, user.getMobile());
            }
            return result;
        }catch (Exception e){
            log.error("创建用户异常，msg={}", e.getMessage());
            throw new MyException("创建用户异常");
        }
    }


    /**
     * 用户注销
     * @param mobile 传来的用户手机号
     * @return  注销结果
     */
    @Transactional
    @Override
    public int deleteByMobile(String mobile) throws MyException{
        //先检查手机号是否已经注册
        User registeredUser;
        try{
            registeredUser = userMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("手机号查询用户异常，msg={}", e.getMessage());
            throw new MyException("手机号查询用户出现异常");
        }
        if (registeredUser == null){
            log.info("手机号={}，未注册", mobile);
            throw new MyException("手机号未注册");
        }

        try{
            int result = userMapper.delete(registeredUser.getId());
            if (result > 0){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_REMOVE_CACHE_KEY, mobile);
            }
            return result;
        }catch (Exception e){
            log.error("手机号删除用户异常，msg={}", e.getMessage());
            throw new MyException("手机号删除用户出现异常");
        }
    }


    /**
     * 更新用户信息（不包括密码）
     * @param user 传来的用户信息json
     * @return  更新的用户信息结果
     */
    @Transactional
    @Override
    public int update(User user) throws MyException{
        //先检查手机号是否已经注册
        User registeredUser;
        try{
            registeredUser = userMapper.selectByMobile(user.getMobile());
        }catch (Exception e){
            log.error("手机号查询用户异常，msg={}", e.getMessage());
            throw new MyException("手机号查询用户出现异常");
        }
        if (registeredUser == null){
            log.info("手机号={}，未注册", user.getMobile());
            throw new MyException("手机号未注册");
        }

        try{//注意此方法不允许用户修改密码
            int result = userMapper.update(user.setPassword(null).setVersion(registeredUser.getVersion()));
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, user.getMobile());
            }
            return result;
        }catch (Exception e){
            log.error("手机号更新用户异常，msg={}", e.getMessage());
            throw new MyException("手机号更新用户出现异常");
        }
    }


    /**
     * 通过电话号查询用户
     * @param mobile 传来的电话号
     * @return  查询结果
     */
    @Transactional(readOnly = true)
    @Override
    public User selectByMobile(String mobile) throws MyException{
        //查缓存
        String key = commonKey + mobile;
        Object tempUser = redisUtil.get(key);
        if (tempUser != null){
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, mobile);
            return (User) tempUser;
        }
        //查数据库
        User user;
        try{
            user = userMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("手机号查询用户异常，msg={}", e.getMessage());
            throw new MyException("手机号查询用户出现异常");
        }

        if (user == null){
            log.info("手机号={}，未注册", mobile);
            throw new MyException("该手机号未注册");
        }
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, mobile);
        return user;
    }


    /**
     * 登录
     * @param user 传来的用户数据
     * @return  登录结果
     */
    @Transactional(readOnly = true)
    @Override
    public boolean login(User user) throws MyException {
        User resultUser;
        try{
            resultUser = userMapper.selectByMobile(user.getMobile());
        }catch (Exception e){
            log.error("手机号查询用户异常，msg={}", e.getMessage());
            throw new MyException("手机号查询用户出现异常");
        }
        if (resultUser == null){
            log.info("手机号={}，未注册", user.getMobile());
            throw new MyException("该手机号未注册");
        }

        //匹配密码
        String inputPassword = user.getPassword();
        String encodedPassword = resultUser.getPassword();
        boolean matches = PasswordEncoder.matches(inputPassword, encodedPassword);
        if (matches)
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, user.getMobile());
        return matches;
    }

    @Override
    public void logout(String mobile) throws MyException {
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_REMOVE_CACHE_KEY, mobile);
    }


    /**
     * 更新用户密码
     * @param mobile 传来的用户手机号
     * @param inputOldPassword 用户输入的旧的明文密码
     * @param newPassword 用户输入的新的明文密码密码
     * @return 修改结果
     */
    @Transactional
    @Override
    public int updatePassword(String mobile, String inputOldPassword,String newPassword) throws MyException {
        User resultUser;
        try{
            resultUser = userMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("手机号查询用户异常，msg={}", e.getMessage());
            throw new MyException("手机号查询用户出现异常");
        }
        if (resultUser == null){
            log.info("手机号={}，未注册", mobile);
            throw new MyException("该手机号未注册");
        }

        String encodedOldPassword = resultUser.getPassword();
        boolean matches = PasswordEncoder.matches(inputOldPassword, encodedOldPassword);
        if (!matches){
            log.warn("手机号={}，原密码输入错误", mobile);
            throw new MyException("旧密码输入错误");
        }
        //加密新密码
        String newEncodedPassword = PasswordEncoder.encode(newPassword);
        try{
            return userMapper.update(
                    new User()
                            .setMobile(mobile)
                            .setPassword(newEncodedPassword)
                            .setVersion(resultUser.getVersion())
            );
        }catch (Exception e){
            log.error("手机号修改用户密码异常，msg={}", e.getMessage());
            throw new MyException("修改用户密码异常");
        }
    }


    /**
     * 发送邮箱验证码
     * @param mobile 传来的手机号
     * @param email 传来的邮箱
     */
    @Transactional(readOnly = true)
    @Override
    public boolean sendEmail(String mobile, String email) throws MyException {
        Object result = redisUtil.get(commonKey + mobile);
        User user;
        try {
            if (result == null)
                user = userMapper.selectByMobile(mobile);
            else
                user = (User) result;
        }catch (Exception e){
            log.error("查询用户出现异常mobile={}", mobile);
            throw new MyException("查询用户出现异常");
        }
        if (!Objects.equals(user.getEmail(), email))
            return false;

        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, mobile);
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SEND_EMAIL_KEY, email);
        return true;
    }


    /**
     * 通过邮箱验证码修改用户密码
     * @param mobile 用户手机号
     * @param email 用户邮箱
     * @param code 用户验证码
     * @param newPassword 明文新密码
     * @return 修改结果
     */
    @Transactional
    @Override
    public int updatePwdWithVerityCode(String mobile, String email, Integer code, String newPassword) throws MyException {
        Object codeObj = redisUtil.get("verityCode:email:" + email);
        if (codeObj == null)
            throw new MyException("验证码过期,请重新获取");
        Integer codeStored = (Integer) codeObj;
        if (!codeStored.equals(code))
            throw new MyException("验证码错误");

        redisUtil.del("verityCode:email:" + email);

        User user;
        try {
            user = userMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("查询用户出现异常mobile={}", mobile);
            throw new MyException("查询用户出现异常");
        }
        if (user == null)
            throw new MyException("该手机号未注册");

        try {
            String encodedNewPwd = PasswordEncoder.encode(newPassword);
            return userMapper.update(user.setPassword(encodedNewPwd));
        }catch (Exception e){
            log.error("查询用户出现异常mobile={}", mobile);
            throw new MyException("查询用户出现异常");
        }
    }


    /**
     * 消费者监听器
     * @param record 生产者传来的数据
     * @param ack 回复
     */
    @KafkaListener(topics = TOPIC_NAME, groupId = "myGroup")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack){
        //消息分类
        if (Objects.equals(record.key(), MQ_SET_CACHE_KEY)){//设置缓存
            String mobile = record.value();
            String key = commonKey + mobile;
            User user = userMapper.selectByMobile(mobile);
            redisUtil.set(key, user.setPassword("******"), cacheTTL);
            log.info("key={}已经放入缓存", key);
        }else if (Objects.equals(record.key(), MQ_REMOVE_CACHE_KEY)){//移除缓存
            String mobile = record.value();
            String key = commonKey + mobile;
            redisUtil.del(key);
            log.info("key={}已经移除缓存", key);
        }else if (Objects.equals(record.key(), MQ_SEND_EMAIL_KEY)){//发邮件
            String email = record.value();
            //验证码存在就不要再生成新的
            Object codeObj = redisUtil.get("verityCode:email:" + email);
            int code;
            if (codeObj == null){
                code = random.nextInt(900000) + 100000;
            }else {
                code = (int) codeObj;
            }
            String message = "验证码为:" + code + ",有效期30分钟,如非本人操作请忽略此信息";
            emailService.sendSimpleMessage(email, "验证码", message);
            redisUtil.set("verityCode:email:" + email, code, 30*60);
            log.info("code={}已经发送成功并放入缓存", code);
        }

        //手动提交
        ack.acknowledge();
        log.info("offset={}手动提交成功", record.offset());
    }

}
