package com.zxy.work.dao;

import com.zxy.work.entities.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    int create(Order order);

    int delete(Order order);

    int update(Order order);

    List<Order> selectByUserId(@Param("userId") Integer userId);

    List<Order> selectByDriverId(@Param("driverId") Integer driverId);

    Order selectByOrderId(@Param("id") Integer id);


}
