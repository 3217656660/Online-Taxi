package com.zxy.work.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxy.work.entities.NotificationMessage;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.OrderService;
import com.zxy.work.service.PaymentServiceClient;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class KafkaConsumer {

    @Resource
    private OrderService orderService;

    @Resource
    private CacheUtil redisUtil;

    private final Map<Long, ScheduledExecutorService> orderTimeout = new ConcurrentHashMap<>();

    @Resource
    private SimpMessagingTemplate messagingTemplate;


    @Resource
    private PaymentServiceClient paymentServiceClient;


    /**
     * 随机数对象
     */
    private static final Random random = new Random();


    /**
     * kafka topic name
     */
    private static final String TOPIC_NAME = "main";

    /**
     * 创建订单后处理消息key
     */
    private static final String MQ_CREATE_ORDER_KEY = "createOrder";

    /**
     * 取消订单后处理消息key
     */
    private static final String MQ_CANCEL_ORDER_KEY = "cancelOrder";

    /**
     * 司机接单后处理消息key
     */
    private static final String MQ_ACCEPT_ORDER_KEY = "acceptOrder";

    /**
     * 司机到达指定开始地点后处理消息key
     */
    private static final String MQ_ARRIVE_START_ADDRESS_KEY = "arriverStartAddress";

    /**
     * 开始驾驶到终点后处理消息key
     */
    private static final String MQ_TO_END_ADDRESS_KEY = "toEndAddress";

    /**
     * 到终点后处理消息key
     */
    private static final String MQ_ARRIVE_END_ADDRESS_KEY = "arriveEndAddress";


    /**
     * 消费者监听器
     * @param record 生产者传来的数据
     * @param ack 回复
     */
    @KafkaListener(topics = TOPIC_NAME, groupId = "myGroup1")
    public void listenMain(ConsumerRecord<String, String> record, Acknowledgment ack){
        //消息分类
        if (Objects.equals(record.key(), MQ_CREATE_ORDER_KEY)){//创建订单
            long userId = Long.parseLong(record.value());
            Order order = orderService.selectNotSolve(userId);
            //1.倒计时
            boolean timeout = setOrderTimeout(order.getId());
            //防止重复倒计时
            if (timeout)
                return;
            //2.后置信息处理
            setProperties(order);
        }else if (Objects.equals(record.key(), MQ_CANCEL_ORDER_KEY)){//取消订单
            long id = Long.parseLong(record.value());
            //1.清楚倒计时
            cancelOrderTimer(id);
            //2.判断是否需要通知司机
            notifyDriver(id);
        }else if (Objects.equals(record.key(), MQ_ACCEPT_ORDER_KEY)){//司机接单后置处理
            long orderId = Long.parseLong(record.value());
            //1.停止倒计时并重新设置缓存
            closeTimeOut(orderId);
        } else if (Objects.equals(record.key(), MQ_ARRIVE_START_ADDRESS_KEY)) {//到达开始位置后置处理
            long orderId = Long.parseLong(record.value());
            arriveStartAddressHandler(orderId);
        } else if (Objects.equals(record.key(), MQ_TO_END_ADDRESS_KEY)) {//验证成功后开始出发的后置处理
            long orderId = Long.parseLong(record.value());
            veritySuccessHandler(orderId);
        } else if (Objects.equals(record.key(), MQ_ARRIVE_END_ADDRESS_KEY)) {//到达终点的后置处理
            long orderId = Long.parseLong(record.value());
            arriveEndAddressHandler(orderId);
        }else if (Objects.equals(record.key(), "paySuccess")) {//支付成功的后置处理
            long orderId = Long.parseLong(record.value());
            paySuccessHandler(orderId);
        }

        //手动提交
        ack.acknowledge();
        //log.info("offset={}手动提交成功", record.offset());
    }

    /**
     * 订单倒计时
     * @param orderId 订单id
     * @return 是否已经有该订单的倒计数，true为有。false为无
     */
    private boolean setOrderTimeout(Long orderId) {
        //重复倒计时直接忽略
        if (orderTimeout.get(orderId) != null){
            log.info("orderId={}重复倒计时被阻止", orderId);
            return true;
        }
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.schedule(() -> {
            String key = "order:id:" + orderId;
            //超时处理
            //订单状态设置为已取消5
            orderService.cancelOrder(orderId);
            //位置移除缓存
            redisUtil.geodelete("position", key);
            //订单信息移出缓存
            redisUtil.del(key);
            //将倒计数移除
            orderTimeout.remove(orderId);
            log.info("key={} 倒计数结束订单超时,已自动取消",key);
        }, 10, TimeUnit.MINUTES);//10
        orderTimeout.put(orderId, timer);
        return false;
    }

    /**
     * 设置创建订单后的属性
     * @param order 订单信息
     */
    private void setProperties(Order order){
        String key = "order:id:" + order.getId();
        //订单放入缓存
        redisUtil.set(key, order, 10*60);
        //开始地点经纬度放入缓存
        redisUtil.geoadd("position", order.getStartLongitude(), order.getStartLatitude(), key);
        log.info("order={}创建后置属性设置成功", order);
    }

    /**
     * 取消订单
     * @param orderId 订单id
     */
    private void cancelOrderTimer(Long orderId) {
        // 如果订单被接单，则取消对应的倒计时器
        ScheduledExecutorService timer = orderTimeout.get(orderId);
        if (timer != null) {
            timer.shutdownNow();
            orderTimeout.remove(orderId);
            log.info("orderId={}倒计时取消成功",orderId);
        }
        String key = "order:id:" + orderId;
        redisUtil.del(key);
        redisUtil.geodelete("position", key);
        log.info("orderId={}取消成功",orderId);
    }

    /**
     * 判断是否需要给司机发送订单被取消通知
     * @param orderId 订单id
     */
    private void notifyDriver(Long orderId){
        Order order = orderService.selectByOrderIdWithDriver(orderId);
        //没有司机接单
        if (order.getDriverId() == 0)
            return;
        NotificationMessage message = new NotificationMessage();
        message.setType("cancelOrder").setContent("订单已被用户取消").setUserId(order.getDriverId());
        //推送到指定客户端
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getDriverId()),
                "/queue/cancelOrder/notifications",
                message
        );
    }

    /**
     * 司机接单后停止倒计时
     * @param orderId 订单id
     */
    private void closeTimeOut(Long orderId){
        ScheduledExecutorService timer = orderTimeout.get(orderId);
        if (timer != null) {
            timer.shutdownNow();
            orderTimeout.remove(orderId);
            log.info("orderId={}倒计时取消成功",orderId);
        }
        //重新设置缓存
        String key = "order:id:" + orderId;
        redisUtil.geodelete("position", key);
        //将已经接了的订单设置到另一个区域
        String actionKey = "order:action:id:" + orderId;
        Order order = orderService.selectByOrderId(orderId);
        redisUtil.set(actionKey, order, 30*60);
    }

    /**
     * 司机到达起始位置，给用户推送信息
     * @param orderId 订单id
     */
    private void arriveStartAddressHandler(Long orderId){
        //1.设置属性
        String key = "order:action:id:" + orderId;
        Object result = redisUtil.get(key);
        Order order;
        if (result == null){//查数据库
            order = orderService.selectByOrderId(orderId);
        }else {
            order = (Order) result;
        }
        //2.生成随机数，通知用户
        int randomNumber = random.nextInt(9000) + 1000;
        log.info("randomNumber={}",randomNumber);
        redisUtil.set("order:verity:id:" + orderId, randomNumber, 3 * 60 * 60);
        NotificationMessage message = new NotificationMessage();
        message.setType("arrivalNotice")
                .setContent(randomNumber)
                .setUserId(order.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getUserId()),
                "/queue/arrivalNotice/notifications",
                message
        );
    }

    /**
     * 验证成功后开始出发的后置处理
     * @param orderId 订单id
     */
    private void veritySuccessHandler(Long orderId){
        //1.移除缓存
        redisUtil.del("order:verity:id:" + orderId);
        //2.更新状态
        String actionKey = "order:action:id:" + orderId;
        Object result = redisUtil.get(actionKey);
        Order order;
        if (result == null){//查数据库
            order = orderService.selectByOrderId(orderId);
        }else {
            order = (Order) result;
        }
        //3.通知用户
        NotificationMessage message = new NotificationMessage();
        message.setType("toEndAddressNotice")
                .setContent("正在前往终点")
                .setUserId(order.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getUserId()),
                "/queue/toEndAddressNotice/notifications",
                message
        );
    }

    /**
     * 司机到达终点位置，给用户推送信息
     * @param orderId 订单id
     */
    private void arriveEndAddressHandler(Long orderId){
        //1.订单移除缓存
        String key = "order:id:" + orderId;
        String actionKey = "order:action:id:" + orderId;
        String messageKey = "message:order:id:" + orderId;
        Object result = redisUtil.get(actionKey);
        Order order;
        if (result == null){//查数据库
            order = orderService.selectByOrderId(orderId);
        }else {
            order = (Order) result;
        }
        redisUtil.del(key,actionKey);
        redisUtil.geodelete("position", key);


        try{//2.创建支付,远程调用
            paymentServiceClient.create(
                    new Payment()
                            .setOrderId(orderId)
                            .setUserId(order.getUserId())
                            .setPaymentMethod("未支付")
                            .setAmount(order.getPrice())
            );
        }catch (Exception e){
            log.error("paymentServiceClient远程调用出错，msg={}", e.getMessage());
            paymentServiceClient.create(
                    new Payment()
                            .setOrderId(orderId)
                            .setUserId(order.getUserId())
                            .setPaymentMethod("未支付")
                            .setAmount(order.getPrice())
            );
        }

        //3.推送给用户
        NotificationMessage message = new NotificationMessage();
        message.setType("arriverEndNotice")
                .setContent("到达终点，请尽快支付")
                .setUserId(order.getUserId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getUserId()),
                "/queue/arriverEndNotice/notifications",
                message
        );
    }

    /**
     * 用户完成支付后置处理
     * @param orderId 订单id
     */
    private void paySuccessHandler(Long orderId){
        //1.更新订单并设置缓存
        Order order = orderService.selectByOrderId(orderId);
        Map<String, String> data= (Map<String, String>) paymentServiceClient.getByOrderId(orderId).getData();
        ObjectMapper objectMapper = new ObjectMapper();
        Payment payment = objectMapper.convertValue(data, Payment.class);
        int i = 0;
        int update;
        do {
            update = orderService.update(order.setStatus(4).setEndTime(new Date()).setPrice(payment.getAmount()));
        }while (update == 0 && i++ < 3);
        redisUtil.set("order:complete:id:" + orderId, order, 20*60);
        redisUtil.set("payment:order:" + orderId, payment,20*60);
    }

}



