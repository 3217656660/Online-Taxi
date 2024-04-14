package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zxy.work.entities.Order;
import com.zxy.work.service.*;
import com.zxy.work.util.MyNotify;
import com.zxy.work.util.MyString;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 主要操作控制器，用于用户、订单、司机、支付、评价，协同通信，完成业复杂业务等
 */
@RestController
@Slf4j
@RequestMapping("/taxi/main")
@SaCheckLogin
@MyNotify("待改善：1.实现token用户一对一")
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
     * 检查用户登录状态 ,如果未登录，则抛出异常：NotLoginException
     */
    @GetMapping("/checkLogin")
    public void checkLogin(){
        StpUtil.checkLogin();
    }


/*    *//**
     * 检查乘客是否有未解决的订单
     * @param userId 传来的乘客id
     * @return 检查结果
     *//*
    @GetMapping("/checkOrder/{userId}")
    public ResponseEntity<Object> checkOrder(@PathVariable Integer userId){
        String checkJson = orderServiceClient.checkOrder(userId).getBody();
        log.info("检查乘客是否有未解决的订单，checkJson={}", checkJson);
        if ( checkJson == null ) return ResponseEntity.ok("没有待解决的订单");
        Order tempOrder;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            tempOrder = objectMapper.readValue(checkJson, Order.class);
            Integer status = tempOrder.getStatus();
            if (status == 0){
                //查询redis中订单是否过期
                String noAcceptedKey = "order:noAccepted:" + tempOrder.getId();
                if (redisUtil.get(noAcceptedKey) == null)  {
                    //删除订单
                    orderServiceClient.delete(tempOrder);
                    return ResponseEntity.ok("没有待解决的订单");
                }
            }
            return ResponseEntity.ok(tempOrder);
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok(MyString.SERVE_ERROR);
        }
    }*/


/*
    */
/**
     * 创建订单，并放到缓存里
     * @param userCreateOrderVo 传来的要保存的信息
     * @return  创建结果
     *//*

    @PostMapping("/createOrder")
    public ResponseEntity<Object> createOrder(@RequestBody UserCreateOrderVo userCreateOrderVo){
        //1.添加必要字段,检查用户的订单，如果有订单未处理，让用户处理该订单，不返回订单详情
        //2.如果订单创建成功，那么返回该订单，并设置到redis中
        log.info("创建订单,userCreateOrderVo={}", userCreateOrderVo);
        Order order = new Order()
                .setDistance(userCreateOrderVo.getDistance())
                .setStartAddress(userCreateOrderVo.getStartAddress())
                .setEndAddress(userCreateOrderVo.getEndAddress())
                .setUserId(userCreateOrderVo.getUserId());
        String orderJson = orderServiceClient.create(order).getBody();
        log.info("创建结果：={}", orderJson);
        Order tempOrder;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            tempOrder = objectMapper.readValue(orderJson, Order.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.ok(MyString.SERVE_ERROR);
        }
        if (tempOrder.getStatus() != 0) return ResponseEntity.ok(MyString.ORDER_NOT_SOLVED);
        //未被接单
        String noAcceptedKey = "order:noAccepted:" + tempOrder.getId();
        String key = "order:" + tempOrder.getId();
        redisUtil.geoadd(
                "position",
                userCreateOrderVo.getStartAddressLongitude(),
                userCreateOrderVo.getStartAddressLatitude(),
                key
        );
        log.info("乘客位置[{},{}]加入缓存中", userCreateOrderVo.getStartAddressLongitude(), userCreateOrderVo.getStartAddressLatitude());
        return redisUtil.set(noAcceptedKey, tempOrder, 5 * 60)
                ? ResponseEntity.ok(tempOrder)
                : ResponseEntity.ok(MyString.ORDER_CREATE_ERROR);
    }
*/


/*    *//**
     * 获取订单剩余时间
     * @param id 订单id
     * @return 查询结果
     *//*
    @GetMapping("/getOrderTime/{id}")
    public ResponseEntity<Object> getOrderTime(@PathVariable("id") Integer id){
        String noAcceptedKey = "order:noAccepted:" + id;
        long expire = redisUtil.getExpire(noAcceptedKey);
        log.info("查询过期时间,id={},expire={}", id, expire);
        if (expire == -2)    orderServiceClient.delete(new Order().setId(id));
        return ResponseEntity.ok(expire);
    }*/


/*    *//**
     * 用户取消订单
     * @param order 传来的订单信息
     * @return  取消结果
     *//*
    @PostMapping("/cancelOrder")
    @MyNotify("待改善：1.")
    public ResponseEntity<String> cancelOrder(@RequestBody Order order){
        //1.用户主动取消订单，如果是司机已经到达起始地点或以后，不允许用户再取消订单
        //2.将order从redis移除
        //3.数据库中也更新订单状态
        //4.如果取消成功，且status为1（司机已接单），则websocket推送到司机端
        log.info("取消订单,order={}", order);
        String goingKey = "order:going:" + order.getId();
        Object temp = redisUtil.get(goingKey);
        if (temp != null || Objects.equals( orderServiceClient.delete(order).getBody(), MyString.ORDER_CANCEL_ERROR ))
            return ResponseEntity.ok(MyString.ORDER_CANCEL_ERROR);
        String key = "order:" + order.getId();
        String noAcceptedKey = "order:noAccepted:" + order.getId();
        String acceptedKey = "order:accepted:" + order.getId();
        //已被接单
        if (redisUtil.get(acceptedKey) != null) {
            log.info("订单已被接单的时候取消订单,order={}", redisUtil.get(acceptedKey));
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
        redisUtil.del(noAcceptedKey, acceptedKey);
        redisUtil.geodelete("position", key);
        return ResponseEntity.ok(MyString.ORDER_CANCEL_SUCCESS);
    }*/


    /**
     * 司机获得可接单的列表
     * @param nowAddressLongitude 司机所在经度
     * @param nowAddressLatitude  司机所在纬度
     * @return 可以接单的列表
     */
    @GetMapping("/getAbleOrderList")
    @MyNotify("待改善:1.")
    public ResponseEntity<Object> getAbleOrderList(@RequestParam("nowAddressLongitude") Double nowAddressLongitude,
                                                   @RequestParam("nowAddressLatitude") Double nowAddressLatitude){
        //1.从redis中查询距离以司机中心距离小于20km的位置
        //2.遍历并转换为json字符串并返回前端
        log.info("司机获得接单列表，司机位置[{},{}]", nowAddressLongitude, nowAddressLatitude);
        List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> geoResults = redisUtil.georadius(
                "position",
                nowAddressLongitude,
                nowAddressLatitude,
                20 * 1000
        );
        if (geoResults == null) return ResponseEntity.ok(MyString.NO_ACCEPTABLE_ORDER);
        List<Order> orderList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>>  geoResult: geoResults) {
            String name = (String) geoResult.getContent().getName();
            String idStr = name.split(":")[1];
            //拿出未被接单的订单
            Object temp = redisUtil.get("order:noAccepted:" + idStr);
            if (temp == null) continue;
            orderList.add((Order) temp);
        }
        log.info("获取的订单列表={}", orderList);
        return orderList.size() == 0
                ? ResponseEntity.ok(MyString.NO_ACCEPTABLE_ORDER)
                : ResponseEntity.ok(orderList);
    }


/*    *//**
     * 司机接单
     * @param driverActionTakeOrderVo 传来的信息
     * @return  接单结果
     *//*
    @PostMapping("/takeOrder")
    @MyNotify("待改善：1.")
    public ResponseEntity<String> takeOrder(@RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        //1.更新数据库中的订单
        //2.从缓存中拿出该订单,将修改好的订单存入redis中，有效时间为两小时
        //3.设置司机当前位置,有效时间5分钟
        //4.司机接单成功时，将订单信息推送给用户
        log.info("司机接单driverActionTakeOrderVo={}", driverActionTakeOrderVo);
        String noAcceptKey = "order:noAccepted:" + driverActionTakeOrderVo.getId();
        Object a = redisUtil.get(noAcceptKey);
        if (a == null) return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        Order redisOrder = (Order) a;
        redisOrder.setDriverId(driverActionTakeOrderVo.getDriverId())
                .setUpdateTime(new Date())
                .setStatus(1);
        String json = orderServiceClient.update(redisOrder).getBody();
        if (Objects.equals(json, MyString.UPDATE_ERROR)) return ResponseEntity.ok(MyString.ORDER_TAKE_ERROR);
        //设置到接单行列,并删除未被接单,删除该订单的起始位置信息
        String acceptedKey = "order:accepted:" + driverActionTakeOrderVo.getId();
        String key = "order:" + driverActionTakeOrderVo.getId();
        redisUtil.set(acceptedKey, redisOrder);
        redisUtil.del(noAcceptKey);
        redisUtil.geodelete("position", key);
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
    }*/


/*
    */
/**
     * 司机到达指定开始地点
     * @param driverActionTakeOrderVo 司机传来的信息
     * @return  订单信息
     *//*

    @PostMapping("/arriveStartAddress")
    @MyNotify("待改善：1.")
    public ResponseEntity<Object> arriveStartAddress(@Valid @RequestBody DriverActionTakeOrderVo driverActionTakeOrderVo){
        String acceptedKey = "order:accepted:" + driverActionTakeOrderVo.getId();
        String goingKey = "order:going:" + driverActionTakeOrderVo.getId();
        Object a = redisUtil.get(acceptedKey);
        if (a == null) return ResponseEntity.ok(MyString.ORDER_NOT_EXIST);
        Order redisOrder = (Order) a;
        redisOrder.setStatus(2).setUpdateTime(new Date());
        String json = orderServiceClient.update(redisOrder).getBody();
        if (Objects.equals(json, MyString.UPDATE_ERROR)) return ResponseEntity.ok("设置到达状态失败");
        //设置到出发,并删除已被接单
        redisUtil.set(goingKey, redisOrder);
        redisUtil.del(acceptedKey);
        NotificationMessage message = new NotificationMessage();
        message.setType("arrivalNotice")
                .setContent("司机到达开始位置")
                .setUserId(driverActionTakeOrderVo.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(driverActionTakeOrderVo.getUserId()),
                "/queue/arrivalNotice/notifications",
                message
        );
        //乘客客户端推送终点坐标
        return ResponseEntity.ok(redisOrder);
    }
*/


/*    *//**
     * 司机到达终点后，发起收款请求，并创建支付信息表，以及放到缓存中
     * @param order 传来的订单信息
     * @return  处理结果
     *//*
    @PostMapping("/requestPayment")
    @MyNotify("待改善：1.司机到达目的地后，提交订单发起收款后，系统推送消息给用户，提醒用户支付")
    public ResponseEntity<Object> requestPayment(@RequestBody Order order){
        //把完成了的订单UserCreateOrderVo移除
        //设置完整的订单并更新到redis和数据库中
        //创建支付，方式为待支付
        //支付表存入redis,有效期3分钟
        String goingKey = "order:going:" + order.getId();
        String key = "order:" +  order.getId();
        Object a = redisUtil.get(goingKey);
        if (a == null) return ResponseEntity.ok(MyString.SERVE_ERROR);
        Order redisOrder = (Order) a;
        Date now = new Date();
        redisOrder.setUpdateTime(now).setStatus(3).setEndTime(now);
        orderServiceClient.update(redisOrder);
        redisUtil.set(key, redisOrder,3 * 60);
        Payment payment = new Payment();
        payment.setAmount(order.getPrice()).setOrderId(order.getId()).setPaymentMethod("待支付");
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
    }*/

    //进行支付
    //完成支付



}
