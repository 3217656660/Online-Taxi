package com.zxy.work.service.impl;

import com.zxy.work.dao.UserDao;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import com.zxy.work.util.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public int create(User user) {
        //1.添加时间和逻辑删除默认值
        LocalDate currentDate = LocalDate.now();
        user
                .setCreateTime(currentDate)
                .setUpdateTime(currentDate)
                .setIsDeleted(0);

        //2.对密码进行加密
        String encodedPassword = PasswordEncoder.encode(user.getPassword());
        user.setPassword( encodedPassword );

        return userDao.create(user);
    }

}
