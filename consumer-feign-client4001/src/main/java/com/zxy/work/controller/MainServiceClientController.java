package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.*;
import com.zxy.work.util.DistanceCalculator;
import com.zxy.work.util.MyString;
import com.zxy.work.util.cache.CacheUtil;
import com.zxy.work.vo.DriverActionTakeOrderVo;
import com.zxy.work.vo.UserCreateOrderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 主要操作控制器，用于用户、订单、司机、支付、评价，协同通信，完成业复杂业务等
 */
@RestController
@Slf4j
@RequestMapping("/taxi/main")
@SaCheckLogin
public class MainServiceClientController {

    @Resource
    private UserServiceClient userServiceClient;

    @Resource
    private ReviewServiceClient reviewServiceClient;

    @Resource
    private PaymentServiceClient paymentServiceClient;

    @Resource
    private OrderServiceClient orderServiceClient;

    @Resource
    private DriverServiceClient driverServiceClient;


    @Resource
    private CacheUtil redisUtil;//抽象缓存工具类，以便框架替换


    /**
     * 创建虚订单，并不直接插入到数据库中，而是放到缓存里
     * @param userCreateOrderVo 传来的要保存的信息
     * @return  创建结果
     */
    @PostMapping("/createOrder")
    public ResponseEntity<String> createOrder(@RequestBody UserCreateOrderVo userCreateOrderVo){
        //用 用户id到redis索引是否已经下了订单，如果有那么返回请先处理那个订单
        //orderHash存UserCreateOrderVo
        //order: 存order
        String key = "order:" + userCreateOrderVo.getUserId();
        if ( redisUtil.get(key) != null )
            return ResponseEntity.ok(MyString.ORDER_NOT_SOLVED);

        //添加必要字段：创建时间、修改时间、逻辑删除字段、订单状态
        Date now = new Date();
        userCreateOrderVo.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0)
                .setStatus(0);  //表示待接单

        //把这个order对象添加到redis的orderHash中，以便司机开始接单后可以检索到
        //如果设置失败，那么重新设置，最大次数为三次
        int i = 0;
        boolean setOrderInRedis;
        do {
            setOrderInRedis = redisUtil.hset("orderHash", key, userCreateOrderVo,5 * 60);//5分钟自动过期
        } while ( !setOrderInRedis && ++i <= 3);

        //设置次数超过
        if (i == 4)
            return ResponseEntity.ok(MyString.ORDER_CREATE_ERROR);

        //订单对象放入redis中，有效时间5分钟，5分钟内订单没有被司机接单或者用户主动取消订单，则redis会自动移除它
        //使用用户Id作为临时订单Id，用户不可一次下两单
        Order order = new Order();
        order.setCreateTime(userCreateOrderVo.getCreateTime())
                .setUpdateTime(userCreateOrderVo.getUpdateTime())
                .setStatus(userCreateOrderVo.getStatus())
                .setIsDeleted(userCreateOrderVo.getIsDeleted())
                .setDistance(userCreateOrderVo.getDistance())
                .setStartAddress(userCreateOrderVo.getStartAddress())
                .setEndAddress(userCreateOrderVo.getEndAddress())
                .setUserId(userCreateOrderVo.getUserId());
        return redisUtil.set(key, order, 5 * 60)
                ? ResponseEntity.ok(MyString.ORDER_CREATE_SUCCESS)
                : ResponseEntity.ok(MyString.ORDER_CREATE_ERROR);
    }


    /**
     * 用户取消订单
     * @param order 传来的订单信息
     * @return  取消结果
     */
    @PostMapping("/cancelOrder")
    public ResponseEntity<String> cancelOrder(@RequestBody Order order){
        //1.用户主动取消订单，将订单对象从redis中直接移除
        String key = "order:" + order.getUserId();
        redisUtil.del(key);
        //2.orderHash中也移除
        redisUtil.hdel("orderHash",key);
        //如果是待出发状态取消的订单，那么持久化该订单
        if (order.getStatus() == 1){
            order.setStatus(5);
            orderServiceClient.create(order);
        }
        else if (order.getStatus() > 1) //如果是司机已经到达起始地点或以后，不允许用户再取消订单
            return ResponseEntity.ok(MyString.ORDER_CANCEL_ERROR);

        return ResponseEntity.ok(MyString.ORDER_CANCEL_SUCCESS);
    }


    /**
     * 司机获得可接单的列表
     * @param driverActionTakeOrderVo 传来的信息
     * @return  可以接单的列表
     */
    @GetMapping("/getAbleOrderList")
    public ResponseEntity< Object > getAbleOrderList(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.从redis中取出orderHash
        Map<Object, Object> orderHash = redisUtil.hmget("orderHash");

        //没有用户下单
        if ( orderHash.isEmpty() )
            return ResponseEntity.ok(MyString.NO_ACCEPTABLE_ORDER);
        //2.遍历map，匹配其中开始位置经纬度与司机位置经纬度得出的距离小于10公里且处于未被接单的订单列表，并返回
        Set<Object> orderHashSet = orderHash.keySet();
        UserCreateOrderVo userCreateOrderVo;
        Double driverLatitude = driverActionTakeOrderVo.getNowAddressLatitude();
        Double driverLongitude = driverActionTakeOrderVo.getNowAddressLongitude();
        List<UserCreateOrderVo> userCreateOrderVoList = new ArrayList<>();
        for ( Object key : orderHashSet ) {
            userCreateOrderVo = (UserCreateOrderVo) orderHash.get(key);
            Double userLongitude = userCreateOrderVo.getStartAddressLongitude();
            Double userLatitude = userCreateOrderVo.getStartAddressLatitude();
            double dist = DistanceCalculator.distance(driverLatitude, driverLongitude, userLatitude, userLongitude);
            if (dist <= 10 && userCreateOrderVo.getStatus() == 0) userCreateOrderVoList.add(userCreateOrderVo);
        }

        //将符合条件的订单返回
        return userCreateOrderVoList.isEmpty()
                ? ResponseEntity.ok(MyString.NO_ACCEPTABLE_ORDER)
                : ResponseEntity.ok(userCreateOrderVoList);
    }


    /**
     * 司机接单
     * @param driverActionTakeOrderVo 传来的信息
     * @return  接单结果
     */
    @PostMapping("/takeOrder")
    public ResponseEntity<String> takeOrder(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.司机前端选择一个接单，从redis中拿出该订单
        String key = "order:" + driverActionTakeOrderVo.getUserId();
        Object a = redisUtil.get(key);
        if (a == null)
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        Order redisOrder = (Order) a;
        //修改订单的更新时间、司机id，订单状态到待出发
        redisOrder.setDriverId(driverActionTakeOrderVo.getDriverId())
                .setUpdateTime(new Date())
                .setStatus(1);  //表示待出发

        //2.将修改好的订单存入redis中，这次有效时间为两小时,次数三次
        int i = 0;
        boolean success;
        do {
            success = redisUtil.set(key, redisOrder,2 * 60 * 60);
        }
        while ( !success && ++i <= 3 );
        if (i == 4)
            return ResponseEntity.ok(MyString.ORDER_TAKE_ERROR);

        //将被接单的订单对应的UserCreateOrderVo在orderHash中修改信息并增加有效时间为两小时,次数三次
        Object b = redisUtil.hget("orderHash", key);
        if (b == null)
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        UserCreateOrderVo userCreateOrderVo = (UserCreateOrderVo)b;
        userCreateOrderVo.setUpdateTime(new Date())
                .setStatus(1)
                .setDriverId(driverActionTakeOrderVo.getDriverId());
        i = 0;
        do {
            success = redisUtil.hset("orderHash", key,userCreateOrderVo,2 * 60 * 60);
        }while ( !success && ++i <= 3 );
        if (i == 4)
            return ResponseEntity.ok(MyString.ORDER_TAKE_ERROR);

        //设置司机当前位置,有效时间5分钟
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey,driverActionTakeOrderVo,5 * 60);

        //3.将修改好的订单存入数据库中
        orderServiceClient.create(redisOrder);
        return ResponseEntity.ok(MyString.ORDER_TAKE_SUCCESS);
    }


    /**
     * 通过用户id获取redis中虚订单的状态
     * @param userId 传来的用户id
     * @return  查询结果
     */
    @GetMapping("/getRedisOrderStatus/{userId}")
    public ResponseEntity<Object> getRedisOrderStatus(@PathVariable Integer userId){
        //获取redis订单状态
        String key = "order:" + userId;
        Object a = redisUtil.get(key);
        if (a == null){
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash",key);
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        }

        Order order = (Order) a;
        if ( order.getStatus() == 0 )//未被接单
            return ResponseEntity.ok(MyString.ORDER_NOT_TAKEN);

        Object userCreateOrderVo = redisUtil.hget("orderHash", key);
        //如果已经有司机接单，要将司机的信息和订单信息也要返回以便前端显示
        return userCreateOrderVo == null
                ?  ResponseEntity.ok(MyString.ORDER_NOT_EXIST)
                :  ResponseEntity.ok(userCreateOrderVo);
    }


    /**
     * 司机到达指定开始地点
     * @param driverActionTakeOrderVo 司机传来的信息
     * @return  订单信息
     */
    @PostMapping("/arriveStartAddress")
    public ResponseEntity<Object> arriveStartAddress(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //司机到达预定开始位置
        String key = "order:" + driverActionTakeOrderVo.getUserId();
        Object a = redisUtil.get(key);
        if (a == null){
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash",key);
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        }
        Order redisOrder = (Order) a;
        redisOrder.setStatus(2).setUpdateTime(new Date());
        redisUtil.set(key, redisOrder,2 * 60 * 60);
        //修改订单状态
        orderServiceClient.updateByStatusAndUserId(redisOrder);

        //设置司机当前位置
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey,driverActionTakeOrderVo,5 * 60);

        return ResponseEntity.ok(redisOrder);
    }


    /**
     * 设置司机的位置到缓存中
     * @param driverActionTakeOrderVo 司机传来的信息
     * @return  设置结果
     */
    @PutMapping("/setDriverAddress")
    public ResponseEntity<Object> setDriverAddress(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //更新司机当前位置
        String key = "order:" + driverActionTakeOrderVo.getUserId();
        Object a = redisUtil.get(key);
        if (a == null) {
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash", key);
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        }

        Object b = redisUtil.hget("orderHash",key);
        if (b == null)
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);

        //设置司机当前位置
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey,driverActionTakeOrderVo,3 * 60);
        return  ResponseEntity.ok(MyString.MODIFY_ADDRESS_SUCCESS);
    }


    /**
     * 获取司机的位置
     * @param order 传来的订单信息
     * @return  查询结果
     */
    @GetMapping("/getDriverAddress")
    public ResponseEntity<Object> getDriverAddress(@RequestBody Order order){
        //用户获取司机当前位置
        String key = "order:" + order.getUserId();
        Object a = redisUtil.get(key);
        if (a == null) {
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash", key);
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        }

        Object b = redisUtil.hget("orderHash",key);
        if (b == null)
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);

        //获取司机当前位置
        String driverKey = "driver:" + order.getDriverId();
        Object driverActionTakeOrderVo = redisUtil.get(driverKey);
        return driverActionTakeOrderVo == null
                ? ResponseEntity.ok(MyString.GET_ADDRESS_ERROR)
                : ResponseEntity.ok(driverActionTakeOrderVo);
    }


    /**
     * 司机到达终点后，发起收款请求，并创建支付信息表，以及放到缓存中
     * @param order 传来的订单信息
     * @return  处理结果
     */
    @PostMapping("/requestPayment")
    public ResponseEntity<Object> requestPayment(@RequestBody Order order){
        //司机发起收款
        String key = "order:" + order.getUserId();
        String paymentKey = "payment:" + order.getUserId();
        //把完成了的订单UserCreateOrderVo移除
        redisUtil.hdel("orderHash",key);
        Object a = redisUtil.get(key);
        if (a == null)
            return ResponseEntity.ok(MyString.SERVE_ERROR);
        //通过数据库获取创建好的订单id,注：用户只能有一个在进行的订单，数据库中也是如此
        //也就是数据库中每一个用户的订单最终status状态只有5或者4，只要有3就再下单的时候提醒支付，不然无法下单
        //1（只有一个）、2（只有一个）、3（只有一个），并且1、2、3互斥。4、5可以有多个，0只存在于redis中（相对于一个用户Id来说）
        //反序列化
        String orderJson = orderServiceClient.getByUserOrderStatus(order).getBody();
        if (orderJson == null)  return ResponseEntity.ok(MyString.SERVE_ERROR);

        Order order1;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            order1 = objectMapper.readValue(orderJson, Order.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok(MyString.SERVE_ERROR);
        }

        Integer orderId =  order1.getId();
        //设置完整的订单并更新到redis和数据库中
        Order redisOrder = (Order) a;
        Date now = new Date();
        redisOrder.setUpdateTime(now)
                .setStatus(3)
                .setEndTime(now)
                .setId(orderId);
        //有效期3分钟,并将订单信息更新到数据库
        redisUtil.set(key,redisOrder,3 * 60);
        orderServiceClient.update(redisOrder);
        //创建支付，方式为待支付
        Payment payment = new Payment();
        payment.setIsDeleted(0)
                .setCreateTime(now)
                .setUpdateTime(now)
                .setAmount(order.getPrice())
                .setOrderId(orderId)
                .setPaymentMethod("待支付");

        //支付表持久化, 并返回对应的paymentId
        paymentServiceClient.create(payment);
        String paymentJson = paymentServiceClient.getByOrderId(orderId).getBody();
        if (paymentJson == null) return ResponseEntity.ok(MyString.SERVE_ERROR);

        Payment payment1;
        try {
            payment1 = objectMapper.readValue(paymentJson, Payment.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok(MyString.SERVE_ERROR);
        }

        payment.setId(payment1.getId());
        //支付表存入redis,有效期3分钟
        redisUtil.set(paymentKey,payment,3 * 60);
        return ResponseEntity.ok(MyString.REQUEST_PAYMENT_SUCCESS);
    }


    /**
     * 获得支付表
     * @param order 传来的订单信息
     * @return 查询结果
     */
    @GetMapping("/goToPayment")
    public ResponseEntity<Object> goToPayment(@RequestBody Order order){
        String paymentKey = "payment:" + order.getUserId();
        Object payment = redisUtil.get(paymentKey);
        //支付表不在缓存里，就到数据库查询
        return payment == null ? ResponseEntity.ok( paymentServiceClient.getByOrderId( order.getId() ) ) : ResponseEntity.ok( payment );
    }

    //进行支付
    //完成支付



}
