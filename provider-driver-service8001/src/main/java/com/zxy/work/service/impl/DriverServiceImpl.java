package com.zxy.work.service.impl;

import com.zxy.work.dao.DriverMapper;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.User;
import com.zxy.work.service.DriverService;
import com.zxy.work.util.MyString;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class DriverServiceImpl implements DriverService {

    @Resource
    private DriverMapper driverMapper;

    @Resource
    private CacheUtil redisUtil;

    /**
     * 设置通用缓存TTL(30分钟)
     */
    private static final int cacheTTL = 30 * 60;

    /**
     * 设置缓存通用key前缀
     */
    private static final String commonKey = "driver:mobile:";


    /**
     * 成为司机
     * @param driver 传来的司机信息
     * @return 认证结果
     */
    @Transactional
    @Override
    public int create(Driver driver) throws MyException {
        String key = commonKey + driver.getMobile();
        //检查该手机号否已成为司机
        Driver registeredDriver = driverMapper.selectByMobile(driver.getMobile());
        if (registeredDriver != null){
            throw new MyException("该手机号已成为司机");
        }

        try{
            int result = driverMapper.create(driver);
            if (result == 1){
                Driver select = driverMapper.selectByMobile(driver.getMobile());
                redisUtil.set(key, select, cacheTTL);
                log.info("key={}在注册司机时放入缓存", key);
            }
            return result;
        }catch (Exception e){
            log.warn("创建司机时产生异常，msg={}", e.getMessage());
            throw new MyException("创建司机时产生异常");
        }
    }


    /**
     * 注销司机，逻辑删除
     * @param mobile 传来的用户手机号
     * @return  注销结果
     */
    @Transactional
    @Override
    public int delete(String mobile) throws MyException{
        String key = commonKey + mobile;
        checkRegister(mobile);

        try{
            int result = driverMapper.delete(mobile);
            if (result == 1){
                redisUtil.del(key);
                log.info("key={}司机删除后的信息移除缓存", key);
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
        String key = commonKey + driver.getMobile();
        checkRegister(driver.getMobile());

        try{
            int result = driverMapper.update(driver);
            if (result == 1){
                Driver select = driverMapper.selectByMobile(driver.getMobile());
                redisUtil.set(key, select, cacheTTL);
                log.info("key={}司机更新后的信息加入缓存", key);
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
    @Override
    public Driver selectByMobile(String mobile) throws MyException{
        String key = commonKey + mobile;
        Object tempDriver = redisUtil.get(key);
        if (tempDriver != null)
            return (Driver) tempDriver;

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
        redisUtil.set(key, driver, cacheTTL);
        return driver;
    }

    /**
     * 通用方法：用于检查司机是否已经注册
     * @param mobile 手机号
     * @return 司机信息
     */
    private Driver checkRegister(String mobile) throws MyException{
        String key = commonKey + mobile;
        Object tempDriver = redisUtil.get(key);
        if (tempDriver != null)
            return (Driver) tempDriver;

        Driver registeredDriver;
        try{
            registeredDriver = driverMapper.selectByMobile(mobile);
        }catch (Exception e){
            log.error("查询司机异常，msg={}", e.getMessage());
            throw new MyException("查询司机出现异常");
        }

        if (registeredDriver == null){
            log.info("手机号={}，未注册", mobile);
            throw new MyException("该手机号未注册成司机");
        }
        redisUtil.set(key, registeredDriver, cacheTTL);
        return registeredDriver;
    }
}
