package com.zxy.work.service;

import com.zxy.work.entities.User;

public interface UserService {

    int create(User user);

    int delete(User user);

    int update(User user);

    User selectByMobile(String mobile);

    int updatePassword(User user,String newPassword);

    User selectById(Integer id);

}
