package com.zxy.work.service.impl;

import com.zxy.work.dao.UserDao;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public int create(User user) {
        return userDao.create(user);
    }

}
