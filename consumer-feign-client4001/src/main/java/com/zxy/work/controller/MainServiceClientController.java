package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxy.work.entities.NotificationMessage;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.*;
import com.zxy.work.util.DistanceCalculator;
import com.zxy.work.util.MyNotify;
import com.zxy.work.util.MyString;
import com.zxy.work.util.cache.CacheUtil;
import com.zxy.work.vo.DriverActionTakeOrderVo;
import com.zxy.work.vo.UserCreateOrderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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

    @Resource
    private SimpMessagingTemplate messagingTemplate;


    /**
     * 创建订单，并放到缓存里
     * @param userCreateOrderVo 传来的要保存的信息
     * @return  创建结果
     */
    @PostMapping("/createOrder")
    @CrossOrigin
    public ResponseEntity<Object> createOrder(@RequestBody UserCreateOrderVo userCreateOrderVo){
        //1.添加必要字段,检查用户的订单，如果有订单未处理，让用户处理该订单，不返回订单详情
        //2.如果订单创建成功，那么返回该订单，并设置到redis中相关字段
        //3.设置到redis中：1.Order   2.UserCreateOrderVo
        //4.orderHash存UserCreateOrderVo
        //5.order: 存order
        Date now = new Date();
        userCreateOrderVo.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0)
                .setStatus(0);  //表示待接单
        Order order = new Order()
                .setDistance(userCreateOrderVo.getDistance())
                .setStartAddress(userCreateOrderVo.getStartAddress())
                .setEndAddress(userCreateOrderVo.getEndAddress())
                .setUserId(userCreateOrderVo.getUserId());
        String orderJson = orderServiceClient.create(order).getBody();
        Order tempOrder;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            tempOrder = objectMapper.readValue(orderJson, Order.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok(MyString.SERVE_ERROR);
        }
        if (tempOrder.getStatus() != 0) return ResponseEntity.ok(MyString.ORDER_NOT_SOLVED);

        String key = "order:" + tempOrder.getId();
        userCreateOrderVo.setId(tempOrder.getId());
        redisUtil.hset("orderHash", key, userCreateOrderVo,5 * 60);//5分钟自动过期
        return redisUtil.set(key, order, 5 * 60)
                ? ResponseEntity.ok(order)
                : ResponseEntity.ok(MyString.ORDER_CREATE_ERROR);
    }


    /**
     * 用户取消订单
     * @param order 传来的订单信息
     * @return  取消结果
     */
    @PostMapping("/cancelOrder")
    @MyNotify("待改善：1.推送消息后，注意在前端websocket区分")
    public ResponseEntity<String> cancelOrder(@RequestBody Order order){
        //1.用户主动取消订单，如果是司机已经到达起始地点或以后，不允许用户再取消订单
        //2.将order,orderHash移除
        //3.数据库中也更新订单状态
        //4.如果取消成功，且status为1，则websocket推送到司机端
        String key = "order:" + order.getId();
        if (order.getStatus() > 1 || Objects.equals( orderServiceClient.delete(order).getBody(), MyString.ORDER_CANCEL_ERROR ))
            return ResponseEntity.ok(MyString.ORDER_CANCEL_ERROR);
        redisUtil.del(key);
        redisUtil.hdel("orderHash",key);
        if (order.getStatus() == 1) {
            NotificationMessage message = new NotificationMessage();
            message.setType("cancelOrder")
                    .setContent(MyString.ORDER_CANCEL_SUCCESS)
                    .setUserId(order.getDriverId());
            //推送到指定客户端
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(order.getDriverId()),
                    "/queue/cancelOrder/notifications",
                    message
            );
        }
        return ResponseEntity.ok(MyString.ORDER_CANCEL_SUCCESS);
    }


    /**
     * 司机获得可接单的列表
     * @param driverActionTakeOrderVo 传来的信息
     * @return  可以接单的列表
     */
    @GetMapping("/getAbleOrderList")
    @MyNotify("待改善:1.距离计算不精确。 2.此方法太耗时，每次都要遍历全部订单，最好转换到redis中进行重构")
    public ResponseEntity<Object> getAbleOrderList(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.取出orderHash
        //2.遍历，匹配其中开始位置经纬度与司机位置经纬度得出的距离小于10公里且处于未被接单的订单列表，并返回
        Map<Object, Object> orderHash = redisUtil.hmget("orderHash");
        if ( orderHash.isEmpty() )
            return ResponseEntity.ok(MyString.NO_ACCEPTABLE_ORDER);
        UserCreateOrderVo userCreateOrderVo;
        Double driverLatitude = driverActionTakeOrderVo.getNowAddressLatitude();
        Double driverLongitude = driverActionTakeOrderVo.getNowAddressLongitude();
        List<UserCreateOrderVo> userCreateOrderVoList = new ArrayList<>();
        for ( Object key : orderHash.keySet() ) {
            userCreateOrderVo = (UserCreateOrderVo) orderHash.get(key);
            Double userLongitude = userCreateOrderVo.getStartAddressLongitude();
            Double userLatitude = userCreateOrderVo.getStartAddressLatitude();
            double dist = DistanceCalculator.distance(driverLatitude, driverLongitude, userLatitude, userLongitude);
            if (dist <= 10 && userCreateOrderVo.getStatus() == 0) userCreateOrderVoList.add(userCreateOrderVo);
        }
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
    @CrossOrigin
    @MyNotify("待改善：1.司机接单后成功后，对用户进行消息推送，给用户司机信息")
    public ResponseEntity<String> takeOrder(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.更新数据库中的订单
        //2.从缓存中拿出该订单
        //3.将修改好的订单存入redis中，这次有效时间为两小时
        //4.将被接单的订单对应的UserCreateOrderVo在orderHash中修改信息并增加有效时间为两小时
        //5.设置司机当前位置,有效时间5分钟
        //6.司机接单成功时，将订单信息推送给用户
        String key = "order:" + driverActionTakeOrderVo.getId();
        Object a = redisUtil.get(key);
        if (a == null) return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);

        Order redisOrder = (Order) a;
        redisOrder.setDriverId(driverActionTakeOrderVo.getDriverId())
                .setUpdateTime(new Date())
                .setStatus(1);  //表示待出发

        String json = orderServiceClient.update(redisOrder).getBody();
        if (Objects.equals(json, MyString.UPDATE_ERROR)) return ResponseEntity.ok(MyString.ORDER_TAKE_ERROR);

        redisUtil.set(key, redisOrder,2 * 60 * 60);
        Object b = redisUtil.hget("orderHash", key);
        if (b == null) return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);

        UserCreateOrderVo userCreateOrderVo = (UserCreateOrderVo)b;
        userCreateOrderVo.setStatus(1).setDriverId(driverActionTakeOrderVo.getDriverId());
        redisUtil.hset("orderHash", key, userCreateOrderVo,2 * 60 * 60);
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey,driverActionTakeOrderVo,5 * 60);

        NotificationMessage message = new NotificationMessage();
        message.setType("orderAccept")
                .setContent(driverActionTakeOrderVo)
                .setUserId(driverActionTakeOrderVo.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(driverActionTakeOrderVo.getUserId()),
                "/queue/orderAccept/notifications",
                message
        );
        return ResponseEntity.ok(MyString.ORDER_TAKE_SUCCESS);
    }


    /**
     * 司机到达指定开始地点
     * @param driverActionTakeOrderVo 司机传来的信息
     * @return  订单信息
     */
    @PostMapping("/arriveStartAddress")
    @MyNotify("待改善：1.司机到达开始地点后，对乘客进行推送")
    public ResponseEntity<Object> arriveStartAddress(@Valid @RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.司机到达预定开始位置修改数据库中订单状态
        //2.将orderHash中的也移除，避免存在死订单
        String key = "order:" + driverActionTakeOrderVo.getId();
        Object a = redisUtil.get(key);
        if (a == null){
            redisUtil.hdel("orderHash",key);
            return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        }
        Order redisOrder = (Order) a;
        redisOrder.setStatus(2).setUpdateTime(new Date());
        String json = orderServiceClient.update(redisOrder).getBody();
        if (Objects.equals(json, MyString.UPDATE_ERROR)) return ResponseEntity.ok("设置到达状态失败");

        redisUtil.set(key, redisOrder,2 * 60 * 60);
        String driverKey = "driver:" + driverActionTakeOrderVo.getDriverId();
        redisUtil.set(driverKey, driverActionTakeOrderVo,5 * 60);

        NotificationMessage message = new NotificationMessage();
        message.setType("arrivalNotice")
                .setContent("司机到达开始位置")
                .setUserId(driverActionTakeOrderVo.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(driverActionTakeOrderVo.getUserId()),
                "/queue/arrivalNotice/notifications",
                message
        );
        return ResponseEntity.ok(redisOrder);
    }


    /**
     * 设置司机的位置到缓存中
     * @param driverActionTakeOrderVo 司机传来的信息
     * @return  设置结果
     */
    @PutMapping("/setDriverAddress")
    @MyNotify("待改善：1.实时更新司机位置推送给用户。 2.可以考虑直接使用websocket实现")
    public ResponseEntity<Object> setDriverAddress(@Valid @RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //更新司机当前位置
        String key = "order:" + driverActionTakeOrderVo.getId();
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
     * 从缓存中获取司机的位置
     * @param order 传来的订单信息
     * @return  查询结果
     */
    @GetMapping("/getDriverAddress")
    @MyNotify("待改善：1.如果使用websocket此方法可以直接搁置")
    public ResponseEntity<Object> getDriverAddress(@RequestBody Order order){
        //用户获取司机当前位置
        String key = "order:" + order.getId();
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
    @MyNotify("待改善：1.司机到达目的地后，提交订单发起收款后，系统推送消息给用户，提醒用户支付")
    public ResponseEntity<Object> requestPayment(@RequestBody Order order){
        //把完成了的订单UserCreateOrderVo移除
        //设置完整的订单并更新到redis和数据库中
        //创建支付，方式为待支付
        //支付表存入redis,有效期3分钟
        String key = "order:" + order.getId();
        Object a = redisUtil.get(key);
        if (a == null) return ResponseEntity.ok(MyString.SERVE_ERROR);

        Order redisOrder = (Order) a;
        Date now = new Date();
        redisOrder.setUpdateTime(now)
                .setStatus(3)
                .setEndTime(now);
        orderServiceClient.update(redisOrder);
        redisUtil.set(key,redisOrder,3 * 60);
        redisUtil.hdel("orderHash", key);
        Payment payment = new Payment();
        payment.setAmount(order.getPrice())
                .setOrderId(order.getId())
                .setPaymentMethod("待支付");
        String paymentJson = paymentServiceClient.create(payment).getBody();
        if (Objects.equals(paymentJson, MyString.PAYMENT_ERROR)) return ResponseEntity.ok("发起支付失败");

        ObjectMapper objectMapper = new ObjectMapper();
        Payment tempPayment;
        try {
            tempPayment = objectMapper.readValue(paymentJson, Payment.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok(MyString.SERVE_ERROR);
        }
        String paymentKey = "payment:" + tempPayment.getId();
        redisUtil.set(paymentKey, tempPayment,3 * 60);

        NotificationMessage message = new NotificationMessage();
        message.setType("paymentNotice")
                .setContent(payment)
                .setUserId(order.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getUserId()),
                "/queue/paymentNotice/notifications",
                message
        );
        return ResponseEntity.ok(MyString.REQUEST_PAYMENT_SUCCESS);
    }

    //进行支付
    //完成支付



}
