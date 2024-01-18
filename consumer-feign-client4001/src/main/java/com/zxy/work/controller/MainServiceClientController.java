package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zxy.work.entities.Order;
import com.zxy.work.service.*;
import com.zxy.work.util.DistanceCalculator;
import com.zxy.work.util.MyString;
import com.zxy.work.util.RedisUtil;
import com.zxy.work.vo.DriverActionTakeOrderVo;
import com.zxy.work.vo.UserCreateOrderVo;
import lombok.extern.slf4j.Slf4j;


import org.springframework.http.HttpStatus;
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
    private RedisUtil redisUtil;


    @PostMapping("/createOrder")
    public ResponseEntity<String> createOrder(@RequestBody UserCreateOrderVo userCreateOrderVo){
        //用 用户id到redis索引是否已经下了订单，如果有那么返回请先处理那个订单
        String key = "order:" + userCreateOrderVo.getUserId();
        if ( redisUtil.get(key) != null )
            return new ResponseEntity<>(MyString.ORDER_NOT_SOLVED,HttpStatus.OK);

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
            setOrderInRedis = redisUtil.hset("orderHash", key, userCreateOrderVo,300);//5分钟自动过期
        } while ( !setOrderInRedis && ++i <= 3);

        //设置次数超过
        if (i == 4)
            return new ResponseEntity<>(MyString.ORDER_CREATE_ERROR,HttpStatus.INTERNAL_SERVER_ERROR);

        //订单对象放入redis中，有效时间5分钟，5分钟内订单没有被司机接单或者用户主动取消订单，则redis会自动移除它
        //使用用户Id作为临时订单Id，用户不可一次下两单
        return redisUtil.set(key, userCreateOrderVo, 300)
                ? new ResponseEntity<>(MyString.ORDER_CREATE_SUCCESS,HttpStatus.OK)
                : new ResponseEntity<>(MyString.ORDER_CREATE_ERROR,HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping("/cancelOrder")
    public ResponseEntity<String> cancelOrder(@RequestBody Order order){
        //1.用户主动取消订单，将订单对象从redis中直接移除
        String key = "order:" + order.getUserId();
        redisUtil.del(key);
        //2.orderHash中也移除
        redisUtil.hdel("orderHash",key);
        //如果是待出发状态取消的订单，那么持久化该订单
        if (order.getStatus() == 1)
           orderServiceClient.create(order);
        else if (order.getStatus() > 1)
            return ResponseEntity.ok(MyString.ORDER_CANCEL_ERROR);

        return ResponseEntity.ok("订单取消成功");
    }


    @GetMapping("/getAbleOrderList")
    public ResponseEntity< List<UserCreateOrderVo> > getAbleOrderList(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.从redis中取出orderHash
        Map<Object, Object> orderHash = redisUtil.hmget("orderHash");
        //没有用户下单
        if ( orderHash.isEmpty() )
            return new ResponseEntity<>(null,HttpStatus.OK);
        //2.遍历map，匹配其中开始位置经纬度与司机位置经纬度得出的距离小于10公里的订单列表，并返回
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
            if (dist <= 10) userCreateOrderVoList.add(userCreateOrderVo);
        }
        //将符合条件的返回
        return new ResponseEntity<>(userCreateOrderVoList,HttpStatus.OK);
    }


    @PostMapping("/takeOrder")
    public ResponseEntity<String> takeOrder(@RequestBody Order order){
        //1.司机前端选择一个接单，从redis中拿出该订单
        String key = "order:" + order.getUserId();
        Order redisOrder = (Order) redisUtil.get(key);
        if (redisOrder == null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("订单不存在");

        //将被接单的订单对应的UserCreateOrderVo从orderHash中移除
        redisUtil.hdel("orderHash",key);

        //修改订单的更新时间、司机id，订单状态到待出发
        redisOrder.setDriverId(order.getDriverId())
                .setUpdateTime(new Date())
                .setStatus(1);  //表示待出发

        //2.将修改好的订单存入redis中，这次有效时间不做要求
        if ( !redisUtil.set(key, redisOrder) )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新订单失败");
        //3.将修改好的订单存入数据库中
        orderServiceClient.create(order);

        return ResponseEntity.ok("接单成功");
    }







}
