package com.zxy.work.service.impl;

import com.zxy.work.dao.OrderMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderService;
import com.zxy.work.util.cache.CacheUtil;
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

    @Resource
    private CacheUtil redisUtil;

    /**
     * 设置通用缓存TTL(30分钟)
     */
    private static final int cacheTTL = 30 * 60;

    /**
     * 设置缓存通用key前缀
     */
    private static final String commonKey = "order:id:";


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
        String key;
        Order notSolve;
        try{
            notSolve = orderMapper.selectNotSolve(order.getUserId());
        }catch (Exception e){
            log.error("订单查询异常,msg={}", e.getMessage());
            throw new MyException("未处理的订单查询异常");
        }
        if (notSolve != null){
            key = commonKey + notSolve.getId();
            redisUtil.set(key, notSolve, cacheTTL);
            log.info("key={}未处理的订单加入缓存", key);
            throw new MyException("您还有未处理的订单");
        }

        try{
            int result = orderMapper.create(order.setStatus(0));
            if (result == 1){
                Order selectNotSolve = orderMapper.selectNotSolve(order.getUserId());
                key = commonKey + selectNotSolve.getId();
                redisUtil.set(key, selectNotSolve, cacheTTL);
                log.info("key={}刚创建的订单加入缓存", key);
            }
            return result;
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
        String key = commonKey + id;
        try{
            int result = orderMapper.delete(id);
            if (result == 1){
                redisUtil.del(key);
                log.info("key={},订单从缓存中移除", key);
            }
            return result;
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
        String key = commonKey + order.getId();
        try{
            int result = orderMapper.update(order);
            if (result == 1){
                Order select = orderMapper.selectByOrderId(order.getId());
                redisUtil.set(key, select, cacheTTL);
                log.info("key={},更新信息加入缓存", key);
            }
            return result;
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
        String key = commonKey + id;
        Object select = redisUtil.get(key);
        if (select != null){
            redisUtil.set(key ,select, cacheTTL);
            return (Order) select;
        }

        try{
            Order order = orderMapper.selectByOrderId(id);
            if (order != null) {
                redisUtil.set(key, order, cacheTTL);
                log.info("key={}订单加入缓存", key);
            }
            return order;
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
            Order order = orderMapper.selectNotSolve(userId);
            if (order != null) {
                String key = commonKey + order.getId();
                redisUtil.set(key, order, cacheTTL);
                log.info("key={}的未解决订单加入缓存", key);
            }
            return order;
        }catch (Exception e){
            log.error("通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("通过订单id的订单查询异常");
        }
    }

}
