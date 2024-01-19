package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.*;
import com.zxy.work.util.DistanceCalculator;
import com.zxy.work.util.MyString;
import com.zxy.work.util.RedisUtil;
import com.zxy.work.vo.DriverActionTakeOrderVo;
import com.zxy.work.vo.UserCreateOrderVo;
import lombok.Data;
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
            setOrderInRedis = redisUtil.hset("orderHash", key, userCreateOrderVo,5 * 60);//5分钟自动过期
        } while ( !setOrderInRedis && ++i <= 3);

        //设置次数超过
        if (i == 4)
            return new ResponseEntity<>(MyString.ORDER_CREATE_ERROR,HttpStatus.INTERNAL_SERVER_ERROR);

        //订单对象放入redis中，有效时间5分钟，5分钟内订单没有被司机接单或者用户主动取消订单，则redis会自动移除它
        //使用用户Id作为临时订单Id，用户不可一次下两单
        return redisUtil.set(key, userCreateOrderVo, 5 * 60)
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
        else if (order.getStatus() > 1) //如果是司机已经到达起始地点或以后，不允许用户再取消订单
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
        return new ResponseEntity<>(userCreateOrderVoList,HttpStatus.OK);
    }


    @PostMapping("/takeOrder")
    public ResponseEntity<String> takeOrder(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.司机前端选择一个接单，从redis中拿出该订单
        String key = "order:" + driverActionTakeOrderVo.getUserId();
        Object a = redisUtil.get(key);
        if (a == null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_TAKE_ERROR);

        //将被接单的订单对应的UserCreateOrderVo在orderHash中修改信息并增加有效时间为两小时,次数三次
        Object b = redisUtil.hget("orderHash", key);
        if (b == null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);
        UserCreateOrderVo userCreateOrderVo = (UserCreateOrderVo)b;
        userCreateOrderVo.setUpdateTime(new Date())
                .setStatus(1)
                .setDriverId(driverActionTakeOrderVo.getDriverId());
        i = 0;
        do {
            success = redisUtil.hset("orderHash", key,userCreateOrderVo,2 * 60 * 60);
        }while ( !success && ++i <= 3 );
        if (i == 4)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_TAKE_ERROR);

        //设置司机当前位置,有效时间5分钟
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey,driverActionTakeOrderVo,5 * 60);

        //3.将修改好的订单存入数据库中
        orderServiceClient.create(redisOrder);
        return ResponseEntity.ok(MyString.ORDER_TAKE_SUCCESS);
    }


    @GetMapping("/getRedisOrderStatus/{userId}")
    public ResponseEntity<Object> getRedisOrderStatus(@PathVariable Integer userId){
        //获取redis订单状态
        String key = "order:" + userId;
        Object a = redisUtil.get(key);
        if (a == null){
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash",key);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);
        }

        Order order = (Order) a;
        if ( order.getStatus() == 0 )//未被接单
            return ResponseEntity.status(HttpStatus.OK).body(MyString.ORDER_NOT_TAKEN);

        Object userCreateOrderVo = redisUtil.hget("orderHash", key);
        //如果已经有司机接单，要将司机的信息和订单信息也要返回以便前端显示
        return userCreateOrderVo == null
                ?  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST)
                :  ResponseEntity.status(HttpStatus.OK).body(userCreateOrderVo);
    }


    @PostMapping("/arriveStartAddress")
    public ResponseEntity<Object> arriveStartAddress(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //司机到达预定开始位置
        String key = "order:" + driverActionTakeOrderVo.getUserId();
        Object a = redisUtil.get(key);
        if (a == null){
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash",key);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);
        }
        Order redisOrder = (Order) a;
        redisOrder.setStatus(2).setUpdateTime(new Date());
        redisUtil.set(key, redisOrder,2 * 60 * 60);
        //设置司机当前位置
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey,driverActionTakeOrderVo);
        return ResponseEntity.status(HttpStatus.OK).body(redisOrder);
    }


    @PutMapping("/setDriverAddress")
    public ResponseEntity<Object> setDriverAddress(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //更新司机当前位置
        String key = "order:" + driverActionTakeOrderVo.getUserId();
        Object a = redisUtil.get(key);
        if (a == null) {
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash", key);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);
        }

        Object b = redisUtil.hget("orderHash",key);
        if (b == null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);

        //设置司机当前位置
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey,driverActionTakeOrderVo);
        return  ResponseEntity.ok(MyString.MODIFY_ADDRESS_SUCCESS);
    }


    @GetMapping("/getDriverAddress")
    public ResponseEntity<Object> getDriverAddress(@RequestBody Order order){
        //用户获取司机当前位置
        String key = "order:" + order.getUserId();
        Object a = redisUtil.get(key);
        if (a == null) {
            //将orderHash中的也移除，避免存在死订单
            redisUtil.hdel("orderHash", key);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);
        }

        Object b = redisUtil.hget("orderHash",key);
        if (b == null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.ORDER_NOT_EXIST);

        //获取司机当前位置
        String driverKey = "driver:" + order.getDriverId();
        Object driverActionTakeOrderVo = redisUtil.get(driverKey);
        return driverActionTakeOrderVo == null
                ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MyString.GET_ADDRESS_ERROR)
                : ResponseEntity.ok(driverActionTakeOrderVo);
    }


    @PostMapping("/requestPayment")
    public ResponseEntity<Object> requestPayment(@RequestBody Order order){
        //司机发起收款
        String key = "order:" + order.getUserId();
        String paymentKey = "payment:" + order.getUserId();
        //把完成了的订单UserCreateOrderVo移除
        redisUtil.hdel("orderHash",key);
        //通过数据库获取创建好的订单id
        Map<String, Object> result = orderServiceClient.getByUserId(order.getUserId());
        //设置完整的订单并更新到redis和数据库中
        Date now = new Date();
        order.setUpdateTime(now)
                .setStatus(3)
                .setEndTime(now)
                .setId(  (  (Order) result.get("data")  ).getId()  );
        //有效期3分钟
        redisUtil.set(key,order,3 * 60);
        orderServiceClient.update(order);
        //创建支付，方式为待支付
        Payment payment = new Payment();
        payment.setIsDeleted(0)
                .setCreateTime(now)
                .setUpdateTime(now)
                .setAmount(order.getPrice())
                .setOrderId(order.getId())
                .setPaymentMethod("待支付");
        //支付表存入redis,有效期3分钟
        redisUtil.set(paymentKey,payment,3 * 60);
        //支付表持久化
        paymentServiceClient.create(payment);
        return ResponseEntity.ok(MyString.REQUEST_PAYMENT_SUCCESS);
    }


    @GetMapping("/getPayment")
    public ResponseEntity<Object> getPayment(@RequestBody Order order){
        String paymentKey = "payment:" + order.getUserId();
        Object payment = redisUtil.get(paymentKey);
        //支付表不在缓存里，就到数据库查询
        return payment == null ? ResponseEntity.ok( paymentServiceClient.getByOrderId( order.getId() ) ) : ResponseEntity.ok( payment );
    }







}
