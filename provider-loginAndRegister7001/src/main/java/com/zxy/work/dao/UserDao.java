package com.zxy.work.dao;

import com.zxy.work.entities.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {

    int create(User user);

    int deleteById(@Param("id")Integer id);

    int update(User user);

    User selectById(@Param("id")Integer id);

}
