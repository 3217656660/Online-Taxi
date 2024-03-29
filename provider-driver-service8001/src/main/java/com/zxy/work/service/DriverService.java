package com.zxy.work.service;


import com.zxy.work.entities.Driver;


public interface DriverService {

    Object create(Driver driver);

    Object delete(Driver driver);

    Object update(Driver driver);

    Object selectByMobile(String mobile);

    Object selectById(Integer id);

}
