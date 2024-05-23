package com.zxy.work.service;

import com.github.pagehelper.PageInfo;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;

public interface OrderService {

    int create(Order order) throws MyException;

    int deleteByUser(long id) throws MyException;

    int deleteByDriver(long id) throws MyException;

    int update(Order order) throws MyException;

    PageInfo<Order> selectByUserId(long userId, int pageNum, int pageSize) throws MyException;

    PageInfo<Order> selectByDriverId(long driverId, int pageNum, int pageSize) throws MyException;

    Order selectByOrderId(long id) throws MyException;

    Order selectByOrderIdWithUser(long id) throws MyException;

    Order selectByOrderIdWithDriver(long id) throws MyException;

    Order selectNotSolve(long userId) throws MyException;

    Order selectNotSolveByDriver(long driverId) throws MyException;

    int cancelOrder(long id) throws MyException;


}
