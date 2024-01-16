package com.zxy.work.service;

import com.zxy.work.entities.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface OrderService {

    int create(Order order);

    int delete(Order order);

    int update(Order order);

    List<Order> selectByUserId(Integer userId);

    List<Order> selectByDriverId(Integer driverId);

    Order selectByOrderId(Integer id);

}
