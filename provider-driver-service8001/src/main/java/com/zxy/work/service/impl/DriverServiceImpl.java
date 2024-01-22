package com.zxy.work.service.impl;

import com.zxy.work.dao.DriverMapper;
import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.DriverService;
import com.zxy.work.util.MyString;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class DriverServiceImpl implements DriverService {

    @Resource
    private DriverMapper driverMapper;


    /**
     * 成为司机
     * @param driver 传来的司机信息
     * @return 认证结果
     */
    @Override
    public Object create(Driver driver) {
        //检查该手机号否已成为司机
        Driver registeredDriver = driverMapper.selectByMobile( driver.getMobile() );
        if ( registeredDriver != null ) return  MyString.MOBILE_EXIST;

        Date now = new Date();
        driver.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);
        return driverMapper.create(driver) == 0
                ? MyString.REGISTER_ERROR
                : MyString.REGISTER_SUCCESS;
    }


    /**
     * 注销司机，逻辑删除
     * @param driver 传来的用户信息json
     * @return  注销结果
     */
    @Override
    public Object delete(Driver driver) {
        Date now = new Date();
        driver.setUpdateTime(now)
                .setIsDeleted(1);
        return driverMapper.delete(driver) == 0
                ? MyString.DELETE_ERROR
                : MyString.DELETE_SUCCESS;
    }


    /**
     * 更新司机信息
     * @param driver 传来的信息json
     * @return  更新的信息结果
     */
    @Override
    public Object update(Driver driver) {
        Date now = new Date();
        driver.setUpdateTime(now);
        return driverMapper.update(driver) == 0
                ? MyString.UPDATE_ERROR
                : driver;
    }


    /**
     * 根据手机号获取司机信息
     * @param mobile    传来的司机手机号
     * @return  获取的结果以及数据
     */
    @Override
    public Object selectByMobile(String mobile) {
        Driver driver = driverMapper.selectByMobile(mobile);
        return driver == null
                ? MyString.FIND_ERROR
                : driver;
    }


    /**
     * 通过id查找司机
     * @param id 司机id
     * @return  查找结果
     */
    @Override
    public Object selectById(Integer id) {
        Driver driver = driverMapper.selectById(id);
        return driver == null
                ? MyString.FIND_ERROR
                : driver;
    }


}
