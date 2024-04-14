package com.zxy.work.service.impl;


import com.zxy.work.dao.UserMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import com.zxy.work.util.cache.CacheUtil;
import com.zxy.work.util.encode.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CacheUtil redisUtil;

    /**
     * 设置通用缓存TTL(30分钟)
     */
    private static final int cacheTTL = 30 * 60;

    /**
     * 设置缓存通用key前缀
     */
    private static final String commonKey = "user:mobile:";

    /**
     * 用户注册
     * @param user 传来的用户信息
     * @return  注册结果
     */
    @Transactional
    @Override
    public int create(User user) throws MyException {
        String key = commonKey + user.getMobile();
        //先检查手机号是否已经注册
        User registeredUser;
        try{
            registeredUser = userMapper.selectByMobile(user.getMobile());
        }catch (Exception e){
            log.error("手机号查询用户异常，msg={}", e.getMessage());
            throw new MyException("手机号查询用户出现异常");
        }

        if ( registeredUser != null){
            log.info("手机号={}，已经注册", registeredUser.getMobile());
            throw new MyException("手机号已经注册过了");
        }

        //没有注册则对密码进行加密，执行插入
        String encodedPassword = PasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        try{
            int result = userMapper.create(user);
            if (result == 1){
                User select = userMapper.selectByMobile(user.getMobile());
                redisUtil.set(key, select.setPassword("******"), cacheTTL);
                log.info("key={}已经在注册成功后放入缓存", key);
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
        selectByMobile(mobile);
        //删除所有用户相关内容
        String key = commonKey + mobile;
        try{
            int result = userMapper.deleteByMobile(mobile);
            if (result == 1){
                redisUtil.del(key);
                log.info("删除了key={}的缓存信息", key);
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
        String key = commonKey + user.getMobile();
        selectByMobile(user.getMobile());
        try{
            int result = userMapper.update(user);
            if (result == 1){
                User select = userMapper.selectByMobile(user.getMobile());
                redisUtil.set(key, select.setPassword("******"), cacheTTL);
                log.info("用户信息key={}更新，重新设置了缓存中的信息", key);
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
    @Override
    public User selectByMobile(String mobile) throws MyException{
        String key = commonKey + mobile;
        Object tempUser = redisUtil.get(key);
        if (tempUser != null){
            redisUtil.set(key, tempUser, cacheTTL);
            return (User) tempUser;
        }

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
        redisUtil.set(key, user, cacheTTL);
        return user;
    }


    /**
     * 登录
     * @param user 传来的用户数据
     * @return  登录结果
     */
    @Override
    public boolean login(User user) throws MyException {
        String key = commonKey + user.getMobile();
        User resultUser = selectByMobile(user.getMobile());
        //匹配密码
        String inputPassword = user.getPassword();
        String encodedPassword = resultUser.getPassword();
        boolean matches = PasswordEncoder.matches(inputPassword, encodedPassword);
        if (matches){
            User select = userMapper.selectByMobile(user.getMobile());
            redisUtil.set(key, select.setPassword("******"), cacheTTL);
            log.info("用户登录成功，设置key={}至缓存中", key);
        }
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
    public int updatePassword(String mobile, String inputOldPassword,String newPassword) throws MyException {
        User resultUser = selectByMobile(mobile);

        String encodedOldPassword = resultUser.getPassword();
        boolean matches = PasswordEncoder.matches(inputOldPassword, encodedOldPassword);
        if (!matches){
            log.warn("手机号={}，原密码输入错误", mobile);
            throw new MyException("旧密码输入错误");
        }
        //加密新密码
        String newEncodedPassword = PasswordEncoder.encode(newPassword);
        try{
            return userMapper.update(new User().setMobile(mobile).setPassword(newEncodedPassword));
        }catch (Exception e){
            log.error("手机号修改用户密码异常，msg={}", e.getMessage());
            throw new MyException("修改用户密码异常");
        }
    }

}
