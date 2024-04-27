package com.zxy.work.service.impl;

import com.zxy.work.dao.OrderMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.User;
import com.zxy.work.service.OrderService;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CacheUtil redisUtil;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;


    /**
     * 设置通用缓存TTL(30分钟)
     */
    private static final int cacheTTL = 30 * 60;

    /**
     * 设置缓存通用key前缀
     */
    private static final String commonKey = "order:id:";

    /**
     * kafka topic name
     */
    private static final String TOPIC_NAME = "orders";

    /**
     * 设置缓存消息key
     */
    private static final String MQ_SET_CACHE_KEY = "setCache";

    /**
     * 移除缓存消息key
     */
    private static final String MQ_REMOVE_CACHE_KEY = "removeCache";

    /**
     * 创建订单后处理消息key
     */
    private static final String MQ_CREATE_ORDER_KEY = "createOrder";

    /**
     * 用于不需要指定顺序的消息随机分区
     */
    private static final Random random = new Random();


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
        if (notSolve != null){
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(order.getId()));
            throw new MyException("您还有未处理的订单");
        }

        try{
            int result = orderMapper.create(order.setStatus(0));
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_CREATE_ORDER_KEY, String.valueOf(order.getUserId()));
            }
            return result;
        }catch (Exception e){
            log.error("订单创建异常,msg={}", e.getMessage());
            throw new MyException("订单创建异常");
        }
    }


    /**
     * 用户删除订单
     * @param id 传来的订单id
     * @return 取消结果
     */
    @Transactional
    @Override
    public int deleteByUser(long id) throws MyException{
        Order order;
        try{
            order = orderMapper.selectByOrderIdWithUser(id);
        }catch (Exception e){
            log.error("通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("通过订单id的订单查询异常");
        }
        if (order != null && order.getStatus() < 4) {
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(id));
            throw new MyException("该订单还未处理完,不可删除");
        }

        try{
            int result = orderMapper.deleteByUser(id);
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_REMOVE_CACHE_KEY, String.valueOf(id));
            }
            return result;
        }catch (Exception e){
            log.error("订单取消异常,msg={}", e.getMessage());
            throw new MyException("订单取消异常");
        }
    }


    /**
     * 司机删除订单
     * @param id 传来的订单id
     * @return 取消结果
     */
    @Transactional
    @Override
    public int deleteByDriver(long id) throws MyException{
        Order order;
        try{
            order = orderMapper.selectByOrderIdWithDriver(id);
        }catch (Exception e){
            log.error("通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("通过订单id的订单查询异常");
        }
        if (order != null && order.getStatus() < 4) {
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(id));
            throw new MyException("该订单还未处理完,不可删除");
        }

        try{
            int result = orderMapper.deleteByDriver(id);
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_REMOVE_CACHE_KEY, String.valueOf(id));
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
        try{
            int result = orderMapper.update(order);
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(order.getId()));
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
    @Transactional(readOnly = true)
    @Override
    public List<Order> selectByUserId(long userId) throws MyException{
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
    @Transactional(readOnly = true)
    @Override
    public List<Order> selectByDriverId(long driverId) throws MyException{
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
     * 通过订单id查询
     * @param id 订单id
     * @return 查询结果
     */
    @Transactional(readOnly = true)
    @Override
    public Order selectByOrderId(long id) throws MyException {
        try{
            return orderMapper.selectByOrderId(id);
        }catch (Exception e){
            log.error("用户通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("用户通过订单id的订单查询异常");
        }
    }


    /**
     * 用户通过订单id查询订单
     * @param id 订单id
     * @return 查询结果
     */
    @Transactional(readOnly = true)
    @Override
    public Order selectByOrderIdWithUser(long id) throws MyException {
        String key = commonKey + id;
        Object select = redisUtil.get(key);
        if (select != null && ((Order)select).getUserDeleted() == 0){
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(id));
            return (Order) select;
        }

        try{
            Order order = orderMapper.selectByOrderIdWithUser(id);
            if (order != null && order.getUserDeleted() == 0) {
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(id));
            }
            return order;
        }catch (Exception e){
            log.error("用户通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("用户通过订单id的订单查询异常");
        }
    }


    /**
     * 司机通过订单id查询订单
     * @param id 订单id
     * @return 查询结果
     */
    @Transactional(readOnly = true)
    @Override
    public Order selectByOrderIdWithDriver(long id) throws MyException {
        String key = commonKey + id;
        Object select = redisUtil.get(key);
        if (select != null && ((Order)select).getDriverDeleted() == 0){
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(id));
            return (Order) select;
        }

        try{
            Order order = orderMapper.selectByOrderIdWithDriver(id);
            if (order != null && order.getDriverDeleted() == 0) {
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(id));
            }
            return order;
        }catch (Exception e){
            log.error("司机通过订单id的订单查询异常,msg={}", e.getMessage());
            throw new MyException("司机通过订单id的订单查询异常");
        }
    }


    /**
     * 查询乘客为解决的订单
     * @param userId 乘客id
     * @return 查询结果
     */
    @Transactional(readOnly = true)
    @Override
    public Order selectNotSolve(long userId) throws MyException{
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


    /**
     * 消费者监听器
     * @param record 生产者传来的数据
     * @param ack 回复
     */
    @KafkaListener(topics = TOPIC_NAME, groupId = "myGroup")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack){
        //消息分类
        if (Objects.equals(record.key(), MQ_SET_CACHE_KEY)){//设置缓存
            long id = Long.parseLong(record.value());
            String key = commonKey + id;
            Order order = orderMapper.selectByOrderId(id);
            redisUtil.set(key, order, cacheTTL);
            log.info("key={}已经放入缓存", key);
        }else if (Objects.equals(record.key(), MQ_REMOVE_CACHE_KEY)){//移除缓存
            long id = Long.parseLong(record.value());
            String key = commonKey + id;
            redisUtil.del(key);
            log.info("key={}已经移除缓存", key);
        }else if (Objects.equals(record.key(), MQ_CREATE_ORDER_KEY)){//创建订单后置处理
            long userId = Long.parseLong(record.value());
            log.info("userId={}", userId);
            Order order = orderMapper.selectNotSolve(userId);
            String key = commonKey + order.getId();
            redisUtil.set(key, order, cacheTTL);
            log.info("key={}已经设置进缓存", key);
        }

        //手动提交
        ack.acknowledge();
        log.info("offset={}手动提交成功", record.offset());
    }

}
