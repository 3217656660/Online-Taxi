package com.zxy.work.service;

import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;

import java.util.List;


public interface OrderService {

    int create(Order order) throws MyException;

    int delete(Integer id) throws MyException;

    int update(Order order) throws MyException;

    List<Order> selectByUserId(Integer userId) throws MyException;

    List<Order> selectByDriverId(Integer driverId) throws MyException;

    Order selectByOrderId(Integer id) throws MyException;

    Order selectNotSolve(Integer userId) throws MyException;

}
