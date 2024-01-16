package com.zxy.work.service;


import com.zxy.work.entities.Driver;


public interface DriverService {

    int create(Driver driver);

    int delete(Driver driver);

    int update(Driver driver);

    Driver selectByMobile(String mobile);

}
