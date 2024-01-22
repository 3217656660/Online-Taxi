package com.zxy.work.service;

import com.zxy.work.entities.User;

public interface UserService {

    Object create(User user);

    Object delete(User user);

    Object update(User user);

    Object selectByMobile(String mobile);

    Object login(User user);

    Object updatePassword(User user,String newPassword);

    Object selectById(Integer id);

}
