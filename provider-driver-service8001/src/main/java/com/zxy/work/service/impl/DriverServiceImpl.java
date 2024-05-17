package com.zxy.work.service.impl;

import com.zxy.work.dao.DriverMapper;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.User;
import com.zxy.work.service.DriverService;
import com.zxy.work.service.EmailService;
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
public class DriverServiceImpl implements DriverService {

    @Resource
    private DriverMapper driverMapper;

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
    private static final String commonKey = "driver:mobile:";

    /**
     * kafka topic name
     */
    private static final String TOPIC_NAME = "drivers";

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
     * 成为司机
     * @param driver 传来的司机信息
     * @return 认证结果
     */
    @Transactional
    @Override
    public int create(Driver driver) throws MyException {
        //检查该手机号否已成为司机
        Driver registeredDriver;
        try{
            registeredDriver = driverMapper.selectByMobile(driver.getMobile());
        }catch (Exception e){
            log.error("手机号查询司机异常，msg={}", e.getMessage());
            throw new MyException("手机号查询司机出现异常");
        }
        if (registeredDriver != null){
            log.info("手机号={}，已经注册", registeredDriver.getMobile());
            throw new MyException("手机号已经注册过了");
        }

        //没有注册则对密码进行加密，执行插入
        String encodedPassword = PasswordEncoder.encode(driver.getPassword());
        driver.setPassword(encodedPassword);
        try{
            int result = driverMapper.create(driver);
            if (result == 1)
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, driver.getMobile());
            return result;
        }catch (Exception e){
            log.error("创建司机时产生异常，msg={}", e.getMessage());
            throw new MyException("创建司机时产生异常");
        }
    }


    /**
     * 注销司机，逻辑删除
     * @param mobile 传来的司机手机号
     * @return  注销结果
     */
    @Transactional
    @Override
    public int delete(String mobile) throws MyException{
        //检查该手机号否已成为司机
        Driver registeredDriver;
        try{
            registeredDriver = driverMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("手机号查询司机异常，msg={}", e.getMessage());
            throw new MyException("手机号查询司机出现异常");
        }
        if (registeredDriver == null){
            log.info("手机号={}，未注册", mobile);
            throw new MyException("手机号未注册");
        }

        try{
            int result = driverMapper.delete(registeredDriver.getId());
            if (result > 0){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_REMOVE_CACHE_KEY, mobile);
            }
            return result;
        }catch (Exception e){
            log.error("删除司机异常，msg={}", e.getMessage());
            throw new MyException("删除司机出现异常");
        }
    }


    /**
     * 更新司机信息
     * @param driver 传来的信息json
     * @return  更新的信息结果
     */
    @Transactional
    @Override
    public int update(Driver driver) throws MyException{
        Driver registeredDriver;
        try{
            registeredDriver = driverMapper.selectByMobile(driver.getMobile());
        }catch (Exception e){
            log.error("手机号查询司机异常，msg={}", e.getMessage());
            throw new MyException("手机号查询司机出现异常");
        }
        if (registeredDriver == null){
            log.info("手机号={}，未注册", driver.getMobile());
            throw new MyException("手机号未注册");
        }

        try{
            int result = driverMapper.update(driver.setPassword(null).setVersion(registeredDriver.getVersion()));
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, driver.getMobile());
            }
            return result;
        }catch (Exception e){
            log.error("更新司机信息异常，msg={}", e.getMessage());
            throw new MyException("更新司机信息出现异常");
        }
    }


    /**
     * 根据手机号获取司机信息
     * @param mobile    传来的司机手机号
     * @return  获取的结果以及数据
     */
    @Transactional(readOnly = true)
    @Override
    public Driver selectByMobile(String mobile) throws MyException{
        String key = commonKey + mobile;
        Object tempDriver = redisUtil.get(key);
        if (tempDriver != null){
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, mobile);
            return (Driver) tempDriver;
        }

        Driver driver;
        try{
            driver = driverMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("查询司机异常，msg={}", e.getMessage());
            throw new MyException("查询司机出现异常");
        }

        if (driver == null){
            log.info("手机号={}，未注册", mobile);
            throw new MyException("该手机号未注册成司机");
        }
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, mobile);
        return driver;
    }


    @Transactional(readOnly = true)
    @Override
    public Driver selectById(Long id) {
        try{
            return driverMapper.selectById(id);
        }catch (Exception e){
            log.error("查询司机异常，msg={}", e.getMessage());
            throw new MyException("查询司机出现异常");
        }
    }


    /**
     * 登录
     * @param driver 传来的司机数据
     * @return  登录结果
     */
    @Transactional(readOnly = true)
    @Override
    public boolean login(Driver driver) throws MyException {
        Driver registeredDriver;
        try{
            registeredDriver = driverMapper.selectByMobile(driver.getMobile());
        }catch (Exception e){
            log.error("手机号查询司机异常，msg={}", e.getMessage());
            throw new MyException("手机号查询司机出现异常");
        }
        if (registeredDriver == null){
            log.info("手机号={}，未注册", driver.getMobile());
            throw new MyException("手机号未注册");
        }

        //匹配密码
        String inputPassword = driver.getPassword();
        String encodedPassword = registeredDriver.getPassword();
        boolean matches = PasswordEncoder.matches(inputPassword, encodedPassword);
        if (matches)
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, driver.getMobile());
        return matches;
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
    public int updatePassword(String mobile, String inputOldPassword, String newPassword) throws MyException {
        Driver registeredDriver;
        try{
            registeredDriver = driverMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("手机号查询司机异常，msg={}", e.getMessage());
            throw new MyException("手机号查询司机出现异常");
        }
        if (registeredDriver == null){
            log.info("手机号={}，未注册", mobile);
            throw new MyException("手机号未注册");
        }

        String encodedOldPassword = registeredDriver.getPassword();
        boolean matches = PasswordEncoder.matches(inputOldPassword, encodedOldPassword);
        if (!matches){
            log.warn("手机号={}，原密码输入错误", mobile);
            throw new MyException("旧密码输入错误");
        }
        //加密新密码
        String newEncodedPassword = PasswordEncoder.encode(newPassword);
        try{
            return driverMapper.update(
                    new Driver()
                    .setMobile(mobile)
                    .setPassword(newEncodedPassword)
                    .setVersion(registeredDriver.getVersion())
            );
        }catch (Exception e){
            log.error("手机号修改司机密码异常，msg={}", e.getMessage());
            throw new MyException("修改司机密码异常");
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
        Driver driver;
        try {
            if (result == null)
                driver = driverMapper.selectByMobile(mobile);
            else
                driver = (Driver) result;
        }catch (Exception e){
            log.error("查询司机出现异常mobile={}", mobile);
            throw new MyException("查询司机出现异常");
        }
        if (!Objects.equals(driver.getEmail(), email))
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

        Driver driver;
        try {
            driver = driverMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("查询司机出现异常mobile={}", mobile);
            throw new MyException("查询司机出现异常");
        }
        if (driver == null)
            throw new MyException("该手机号未注册");

        try {
            String encodedNewPwd = PasswordEncoder.encode(newPassword);
            return driverMapper.update(driver.setPassword(encodedNewPwd));
        }catch (Exception e){
            log.error("查询司机出现异常mobile={}", mobile);
            throw new MyException("查询司机出现异常");
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
            Driver driver = driverMapper.selectByMobile(mobile);
            redisUtil.set(key, driver.setPassword("******"), cacheTTL);
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
