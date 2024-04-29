package com.zxy.work.service;

import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;

import java.util.List;


public interface OrderService {

    int create(Order order) throws MyException;

    int deleteByUser(long id) throws MyException;

    int deleteByDriver(long id) throws MyException;

    int update(Order order) throws MyException;

    List<Order> selectByUserId(long userId) throws MyException;

    List<Order> selectByDriverId(long driverId) throws MyException;

    Order selectByOrderId(long id) throws MyException;

    Order selectByOrderIdWithUser(long id) throws MyException;

    Order selectByOrderIdWithDriver(long id) throws MyException;

    Order selectNotSolve(long userId) throws MyException;

    int cancelOrder(long id) throws MyException;


}
