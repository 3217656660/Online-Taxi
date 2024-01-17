package com.zxy.work.service.impl;


import com.zxy.work.dao.UserMapper;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import com.zxy.work.util.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

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

        return userMapper.create(user);
    }


    @Override
    public int delete(User user) {
        //添加时间和逻辑删除的值
        Date now = new Date();
        user.setUpdateTime(now)
                .setIsDeleted(1);
        return userMapper.delete(user);
    }


    @Override
    public int update(User user) {
        //添加时间
        Date now = new Date();
        user.setUpdateTime(now);
        return userMapper.update(user);
    }


    @Override
    public User selectByMobile(String mobile) {
        return userMapper.selectByMobile(mobile);
    }


    @Override
    public int updatePassword(User user,String newPassword) {
        //先比较旧密码输入是否正确
        String inputOldPassword = user.getPassword();
        String encodedOldPassword = userMapper.selectByMobile( user.getMobile() ).getPassword();
        boolean matches = PasswordEncoder.matches(inputOldPassword, encodedOldPassword);

        //旧密码输入正确
        if (matches){
            //添加更新时间
            Date now = new Date();
            user.setUpdateTime(now);
            newPassword = PasswordEncoder.encode(newPassword);
            user.setPassword(newPassword);
            return userMapper.update(user);
        }
        return 0;
    }

    @Override
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

}
