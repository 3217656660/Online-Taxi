package com.zxy.work.dao;

import com.zxy.work.entities.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    int create(Order order);

    int deleteByUser(@Param("id") long id);

    int deleteByDriver(@Param("id") long id);

    int update(Order order);

    List<Order> selectByUserId(@Param("userId") long userId);

    List<Order> selectByDriverId(@Param("driverId") long driverId);

    Order selectByOrderId(@Param("id") long id);

    Order selectByOrderIdWithUser(@Param("id") long id);

    Order selectByOrderIdWithDriver(@Param("id") long id);

    Order selectNotSolve(@Param("userId")long userId);
    int cancelOrder(@Param("id") long id);
}
