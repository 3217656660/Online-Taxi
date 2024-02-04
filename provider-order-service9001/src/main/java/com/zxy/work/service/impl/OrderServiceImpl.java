package com.zxy.work.service.impl;

import com.zxy.work.dao.OrderMapper;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderService;
import com.zxy.work.util.MyString;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;


    /**
     * 创建订单
     * @param order 传来的用户信息json
     * @return 创建结果
     */
    @Override
    public Object create(Order order) {
        Date now = new Date();
        order.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0)
                .setStatus(0);
        //1.查询数据库中是否还有待处理的订单
        //2.如果有待处理的订单，那么让用户去处理该订单
        //3.如果没有，则重新创建一个订单
        Order notSolve = orderMapper.selectNotSolve(order.getUserId());
        if (notSolve != null) return notSolve;

        return orderMapper.create(order) == 0
                ? MyString.ORDER_CREATE_ERROR
                : order;
    }


    /**
     * 取消订单
     * @param order 传来的订单json
     * @return 取消结果
     */
    @Override
    public Object delete(Order order) {
        Date now = new Date();
        order.setUpdateTime(now)
                .setStatus(5)//已取消
                .setIsDeleted(1);
        return orderMapper.delete(order) == 0
                ? MyString.ORDER_CANCEL_ERROR
                : MyString.ORDER_CANCEL_SUCCESS;
    }


    /**
     * 更新订单信息
     * @param order 传来的订单json
     * @return  更新结果
     */
    @Override
    public Object update(Order order) {
        Date now = new Date();
        order.setUpdateTime(now);
        return orderMapper.update(order) == 0
                ? MyString.UPDATE_ERROR
                : order;
    }


    /**
     * 用户查询历史订单
     * @param userId 传来的用户id
     * @return 历史订单
     */
    @Override
    public Object selectByUserId(Integer userId) {
        List<Order> orderList = orderMapper.selectByUserId(userId);
        return orderList == null
                ? MyString.FIND_ERROR
                : orderList;
    }


    /**
     * 司机查询历史订单
     * @param driverId 传来的司机id
     * @return 历史订单
     */
    @Override
    public Object selectByDriverId(Integer driverId) {
        List<Order> orderList = orderMapper.selectByDriverId(driverId);
        return orderList == null
                ? MyString.FIND_ERROR
                : orderList;
    }


    /**
     * 通过订单id查询订单，一般查询正在进行中的订单
     * @param id  传来的订单id
     * @return 订单对象
     */
    @Override
    public Object selectByOrderId(Integer id) {
        Order order = orderMapper.selectByOrderId(id);
        return order == null
                ? MyString.FIND_ERROR
                : order;
    }

}
