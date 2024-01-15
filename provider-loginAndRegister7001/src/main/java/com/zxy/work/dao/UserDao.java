package com.zxy.work.dao;

import com.zxy.work.entities.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {

    int create(User user);

    int delete(User user);

    int update(User user);

    User selectById(@Param("id")Integer id);

    User selectByMobile(@Param("mobile")String mobile);

}
