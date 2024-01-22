package com.zxy.work.service.impl;


import com.zxy.work.dao.UserMapper;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import com.zxy.work.util.MyString;
import com.zxy.work.util.encode.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;


    /**
     * 用户注册
     * @param user 传来的用户信息
     * @return  注册结果
     */
    @Override
    public Object create(User user) {
        //添加时间和逻辑删除默认值
        Date now = new Date();
        user
                .setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);

        //先检查手机号是否已经注册
        User registeredUser = userMapper.selectByMobile(user.getMobile());
        if ( registeredUser != null)
            return MyString.MOBILE_EXIST;

        //没有注册则对密码进行加密，执行插入
        String encodedPassword = PasswordEncoder.encode(user.getPassword());
        user.setPassword( encodedPassword );
        return userMapper.create(user) == 0
                ? MyString.REGISTER_ERROR
                : MyString.REGISTER_SUCCESS;
    }


    /**
     * 用户注销
     * @param user 传来的用户信息
     * @return  注销结果
     */
    @Override
    public Object delete(User user) {
        //添加时间和逻辑删除的值
        Date now = new Date();
        user.setUpdateTime(now)
                .setIsDeleted(1);

        return userMapper.delete(user) == 0
                ? MyString.DELETE_ERROR
                : MyString.DELETE_SUCCESS;
    }


    /**
     * 更新用户信息（不包括密码）
     * @param user 传来的用户信息json
     * @return  更新的用户信息结果
     */
    @Override
    public Object update(User user) {
        //添加时间
        Date now = new Date();
        user.setUpdateTime(now);
        return userMapper.update(user) == 0
                ? MyString.UPDATE_ERROR
                : MyString.UPDATE_SUCCESS;
    }


    /**
     * 通过电话号查询用户
     * @param mobile 传来的电话号
     * @return  查询结果
     */
    @Override
    public Object selectByMobile(String mobile) {
        User user =  userMapper.selectByMobile(mobile);
        return user == null
                ? MyString.FIND_ERROR
                : user.setPassword("**********");
    }


    /**
     * 登录
     * @param user 传来的用户数据
     * @return  登录结果
     */
    @Override
    public Object login(User user) {
        User resultUser = userMapper.selectByMobile( user.getMobile() );
        if (resultUser == null) return MyString.ACCOUNT_ERROR;

        //匹配密码
        String inputPassword = user.getPassword();
        String encodedPassword = resultUser.getPassword();
        return PasswordEncoder.matches(inputPassword, encodedPassword)
                ? resultUser.setPassword("**************")
                : MyString.PASSWORD_ERROR;
    }


    /**
     * 更新用户密码
     * @param user 传来的用户
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Override
    public Object updatePassword(User user,String newPassword) {
        String inputOldPassword = user.getPassword();
        User resultUser = userMapper.selectById(user.getId());
        if (resultUser == null)
            return MyString.ACCOUNT_ERROR;

        String encodedOldPassword = resultUser.getPassword();
        boolean matches = PasswordEncoder.matches(inputOldPassword, encodedOldPassword);
        if (matches){
            Date now = new Date();
            user.setUpdateTime(now);
            newPassword = PasswordEncoder.encode(newPassword);
            user.setPassword(newPassword);
            return userMapper.update(user) == 0
                    ?  MyString.UPDATE_ERROR
                    :  MyString.UPDATE_SUCCESS;
        }
        return MyString.PASSWORD_ERROR;
    }


    /**
     * 通过id查找用户
     * @param id    传来的用户id
     * @return  查找结果
     */
    @Override
    public Object selectById(Integer id) {
        User user = userMapper.selectById(id);
        return user == null
                ? MyString.FIND_ERROR
                : user.setPassword("***********");
    }

}
