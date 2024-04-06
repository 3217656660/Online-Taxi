package com.zxy.work.dao;

import com.zxy.work.entities.Driver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DriverMapper {

    int create(Driver driver);

    int delete(@Param("mobile") String mobile);

    int update(Driver driver);

    Driver selectByMobile(@Param("mobile") String mobile);

}
