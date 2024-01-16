package com.zxy.work.service.impl;

import com.zxy.work.dao.UserDao;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import com.zxy.work.util.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public int create(User user) {
        //产生唯一的账号ID:8位随机整数(1000 0000 - 9999 9999)
        User existedUser;
        int min = 10000000;
        int max = 99999999;

        //临时Id
        int tempId;

        //查询id是否唯一，不唯一继续生成
        do {
            tempId =(int) (Math.random() * (max - min + 1) + min);
            existedUser = userDao.selectById(tempId);
        }
        while( existedUser != null );

        //添加时间和逻辑删除默认值
        Date now = new Date();
        user
                .setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0)
                .setId( tempId );

        //对密码进行加密
        String encodedPassword = PasswordEncoder.encode(user.getPassword());
        user.setPassword( encodedPassword );

        return userDao.create(user);
    }


    @Override
    public int delete(User user) {
        //添加时间和逻辑删除的值
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


    @Override
    public int updatePassword(User user,String newPassword) {
        //先比较旧密码输入是否正确
        String inputOldPassword = user.getPassword();
        String encodedOldPassword = userDao.selectById(user.getId()).getPassword();
        boolean matches = PasswordEncoder.matches(inputOldPassword, encodedOldPassword);

        //旧密码输入正确
        if (matches){
            //添加更新时间
            Date now = new Date();
            user.setUpdateTime(now);
            newPassword = PasswordEncoder.encode(newPassword);
            user.setPassword(newPassword);
            return userDao.update(user);
        }
        return 0;
    }



}
