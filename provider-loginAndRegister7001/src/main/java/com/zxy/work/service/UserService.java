package com.zxy.work.service;

import com.zxy.work.entities.User;
import org.apache.ibatis.annotations.Param;

public interface UserService {

    int create(User user);

    int delete(User user);

    int update(User user);

    User selectById(Integer id);

    User selectByMobile(String mobile);

}
