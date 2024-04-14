package com.zxy.work.service.impl;

import com.zxy.work.dao.OrderMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;


    /**
     * 创建订单
     * @param order 传来的用户信息json
     * @return 创建结果
     */
    @Transactional
    @Override
    public int create(Order order) throws MyException {
        //1.查询数据库中是否还有待处理的订单
        //2.如果有待处理的订单，那么让用户去处理该订单
        //3.如果没有，则重新创建一个订单
        Order notSolve;
        try{
            notSolve = orderMapper.selectNotSolve(order.getUserId());
        }catch (Exception e){
            log.error("订单查询异常,msg={}", e.getMessage());
            throw new MyException("未处理的订单查询异常");
        }
        if (notSolve != null)
            throw new MyException("您还有未处理的订单");

        try{
            return orderMapper.create(order);
        }catch (Exception e){
            log.error("订单创建异常,msg={}", e.getMessage());
            throw new MyException("订单创建异常");
        }
    }


    /**
     * 取消订单
     * @param id 传来的订单id
     * @return 取消结果
     */
    @Transactional
    @Override
    public int delete(Integer id) throws MyException{
        try{
            return orderMapper.delete(id);
        }catch (Exception e){
            log.error("订单取消异常,msg={}", e.getMessage());
            throw new MyException("订单取消异常");
        }
    }


    /**
     * 更新订单信息
     * @param order 传来的订单json
     * @return  更新结果
     */
    @Transactional
    @Override
    public int update(Order order) throws MyException{
        try{
            return orderMapper.update(order);
        }catch (Exception e){
            log.error("订单更新异常,msg={}", e.getMessage());
            throw new MyException("订单更新异常");
        }
    }


    /**
     * 用户查询历史订单
     * @param userId 传来的用户id
     * @return 历史订单
     */
    @Override
    public List<Order> selectByUserId(Integer userId) throws MyException{
        List<Order> orderList;
        try{
            orderList = orderMapper.selectByUserId(userId);
        }catch (Exception e){
            log.error("通过用户id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("通过用户id的订单查询异常");
        }
        if (orderList == null)
            throw new MyException("您还未有过订单");

        return orderList;
    }


    /**
     * 司机查询历史订单
     * @param driverId 传来的司机id
     * @return 历史订单
     */
    @Override
    public List<Order> selectByDriverId(Integer driverId) throws MyException{
        List<Order> orderList;
        try{
            orderList = orderMapper.selectByDriverId(driverId);
        }catch (Exception e){
            log.error("通过司机id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("通过司机id的订单查询异常");
        }
        if (orderList == null)
            throw new MyException("您还未有过订单");

        return orderList;
    }


    /**
     * 通过订单id查询订单，一般查询正在进行中的订单
     * @param id  传来的订单id
     * @return 订单对象
     */
    @Override
    public Order selectByOrderId(Integer id) throws MyException{
        try{
            return orderMapper.selectByOrderId(id);
        }catch (Exception e){
            log.error("通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("通过订单id的订单查询异常");
        }
    }


    /**
     * 查询乘客为解决的订单
     * @param userId 乘客id
     * @return 查询结果
     */
    @Override
    public Order selectNotSolve(Integer userId) throws MyException{
        try{
            return orderMapper.selectNotSolve(userId);
        }catch (Exception e){
            log.error("通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("通过订单id的订单查询异常");
        }
    }

}
