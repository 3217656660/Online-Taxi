package com.zxy.work.service.impl;

import com.zxy.work.dao.OrderMapper;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;


    @Override
    public int create(Order order) {
        Date now = new Date();
        order.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);

        return orderMapper.create(order);
    }


    /**
     * 用户取消订单
     * @param order 传来的订单json
     * @return 取消结果
     */
    @Override
    public int deleteByUser(Order order) {
        Date now = new Date();
        order.setUpdateTime(now)
                .setStatus(5)//已取消
                .setIsDeleted(1);

        return orderMapper.deleteByUser(order);
    }


    /**
     * 司机取消订单
     * @param order 传来的订单json
     * @return 取消结果
     */
    @Override
    public int deleteByDriver(Order order) {
        Date now = new Date();
        order.setUpdateTime(now)
                .setStatus(5)//已取消
                .setIsDeleted(1);

        return orderMapper.deleteByDriver(order);
    }


    /**
     * 更新订单信息
     * @param order 传来的订单json
     * @return  更新结果
     */
    @Override
    public int updateByOrderId(Order order) {
        Date now = new Date();
        order.setUpdateTime(now);
        return orderMapper.updateByOrderId(order);
    }


    /**
     * 用户查询历史订单
     * @param userId 传来的用户id
     * @return 历史订单
     */
    @Override
    public List<Order> selectByUserId(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }


    /**
     * 司机查询历史订单
     * @param driverId 传来的司机id
     * @return 历史订单
     */
    @Override
    public List<Order> selectByDriverId(Integer driverId) {
        return orderMapper.selectByDriverId(driverId);
    }


    /**
     * 通过订单id查询订单，一般查询正在进行中的订单
     * @param id  传来的订单id
     * @return 订单对象
     */
    @Override
    public Order selectByOrderId(Integer id) {
        return orderMapper.selectByOrderId(id);
    }



}
