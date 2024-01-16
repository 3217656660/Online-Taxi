package com.zxy.work.service.impl;

import com.zxy.work.dao.DriverMapper;
import com.zxy.work.entities.Driver;
import com.zxy.work.service.DriverService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class DriverServiceImpl implements DriverService {

    @Resource
    private DriverMapper driverMapper;


    @Override
    public int create(Driver driver) {
        Date now = new Date();
        driver.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);

        return driverMapper.create(driver);
    }

    @Override
    public int delete(Driver driver) {
        Date now = new Date();
        driver.setUpdateTime(now)
                .setIsDeleted(1);
        return driverMapper.delete(driver);
    }

    @Override
    public int update(Driver driver) {
        Date now = new Date();
        driver.setUpdateTime(now);
        return driverMapper.update(driver);
    }

    @Override
    public Driver selectByMobile(String mobile) {
        return driverMapper.selectByMobile(mobile);
    }


}
