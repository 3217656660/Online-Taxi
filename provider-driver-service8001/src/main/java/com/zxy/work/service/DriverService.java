package com.zxy.work.service;


import com.zxy.work.entities.Driver;
import com.zxy.work.entities.MyException;


public interface DriverService {

    int create(Driver driver) throws MyException;

    int delete(String mobile) throws MyException;

    int update(Driver driver) throws MyException;

    Driver selectByMobile(String mobile) throws MyException;

}
