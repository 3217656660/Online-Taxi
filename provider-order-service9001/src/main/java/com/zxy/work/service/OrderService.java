package com.zxy.work.service;

import com.zxy.work.entities.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface OrderService {

    int create(Order order);

    int deleteByUser(Order order);

    int deleteByDriver(Order order);

    int updateByOrderId(Order order);

    List<Order> selectByUserId(Integer userId);

    List<Order> selectByDriverId(Integer driverId);

    Order selectByOrderId(Integer id);

}
