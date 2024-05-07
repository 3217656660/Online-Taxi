package com.zxy.work.service;

import com.zxy.work.entities.MyException;
import com.zxy.work.entities.User;

public interface UserService {

    int create(User user) throws MyException;

    int deleteByMobile(String mobile) throws MyException;

    int update(User user) throws MyException;

    User selectByMobile(String mobile) throws MyException;

    boolean login(User user) throws MyException;

    int updatePassword(String mobile, String inputOldPassword,String newPassword) throws MyException;

    boolean sendEmail(String mobile,String email) throws MyException;

    int updatePwdWithVerityCode(String mobile, String email, Integer code, String newPassword) throws MyException;

}
