package com.zxy.work.service.impl;

import com.zxy.work.dao.UserDao;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import com.zxy.work.util.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public int create(User user) {

        //添加时间和逻辑删除默认值
        Date now = new Date();
        user
                .setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);

        //对密码进行加密
        String encodedPassword = PasswordEncoder.encode(user.getPassword());
        user.setPassword( encodedPassword );

        return userDao.create(user);
    }


    @Override
    public int delete(User user) {
        //添加时间和逻辑删除默认值
        Date now = new Date();
        user
                .setUpdateTime(now)
                .setIsDeleted(1);
        return userDao.delete(user);
    }

    @Override
    public int update(User user) {
        //添加更新时间
        Date now = new Date();
        user.setUpdateTime(now);
        return userDao.update(user);
    }

    @Override
    public User selectById(Integer id) {
        return userDao.selectById(id);
    }

    @Override
    public User selectByMobile(String mobile) {
        return userDao.selectByMobile(mobile);
    }


}
