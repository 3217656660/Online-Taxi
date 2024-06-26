package com.zxy.work.service;


import com.zxy.work.entities.Driver;
import com.zxy.work.entities.MyException;


public interface DriverService {

    int create(Driver driver) throws MyException;

    int delete(String mobile) throws MyException;

    int update(Driver driver) throws MyException;

    Driver selectByMobile(String mobile) throws MyException;

    Driver selectById(Long id);

    boolean login(Driver driver) throws MyException;

    void logout(String mobile) throws MyException;

    int updatePassword(String mobile, String inputOldPassword,String newPassword) throws MyException;

    boolean sendEmail(String mobile,String email) throws MyException;

    int updatePwdWithVerityCode(String mobile, String email, Integer code, String newPassword) throws MyException;

}
