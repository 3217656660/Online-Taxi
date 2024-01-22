package com.zxy.work.service;

import com.zxy.work.entities.Order;


public interface OrderService {

    Object create(Order order);

    Object delete(Order order);

    Object update(Order order);

    Object selectByUserId(Integer userId);

    Object selectByDriverId(Integer driverId);

    Object selectByOrderId(Integer id);

    Object selectByUserOrderStatus(Order order);

    void updateByStatusAndUserId(Order order);

}
