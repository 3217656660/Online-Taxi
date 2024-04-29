package com.zxy.work.service.impl;

import com.zxy.work.dao.PaymentMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentService;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentMapper paymentMapper;

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
    private static final String commonKey = "payment:orderId:";

    /**
     * kafka topic name
     */
    private static final String TOPIC_NAME = "payments";

    /**
     * 设置缓存消息key
     */
    private static final String MQ_SET_CACHE_KEY = "setCache";

    /**
     * 移除缓存消息key
     */
    private static final String MQ_REMOVE_CACHE_KEY = "removeCache";

    /**
     * 用于不需要指定顺序的消息随机分区
     */
    private static final Random random = new Random();


    /**
     * 创建支付
     * @param payment 传来的支付json
     * @return  创建支付结果
     */
    @Transactional
    @Override
    public int create(Payment payment) throws MyException {
        Payment select;
        try{
            select = paymentMapper.selectByOrderId(payment.getOrderId());
        }catch (Exception e){
            log.info("支付查询出现异常msg={}", e.getMessage());
            throw new MyException("支付查询出现异常");
        }
        if (select != null)
            throw new MyException("该订单的支付已经创建过了");

        try{
            int result = paymentMapper.create(payment);
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(payment.getOrderId()));
            }
            return result;
        }catch (Exception e){
            log.info("支付创建出现异常msg={}", e.getMessage());
            throw new MyException("支付创建出现异常");
        }
    }


    /**
     * 删除支付
     * @param orderId 传来的订单id
     * @return 支付删除结果
     */
    @Transactional
    @Override
    public int delete(long orderId) throws MyException{
        try{
            int result = paymentMapper.delete(orderId);
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_REMOVE_CACHE_KEY, String.valueOf(orderId));
            }
            return result;
        }catch (Exception e){
            log.info("支付删除出现异常msg={}", e.getMessage());
            throw new MyException("支付删除出现异常");
        }
    }


    /**
     * 更新支付信息
     * @param payment 传来的支付json信息
     * @return  更新结果
     */
    @Transactional
    @Override
    public int update(Payment payment) throws MyException{
        try{
            int result = paymentMapper.update(payment);
            if (result == 1){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(payment.getOrderId()));
            }
            return result;
        }catch (Exception e){
            log.info("支付更新出现异常msg={}", e.getMessage());
            throw new MyException("支付更新出现异常");
        }
    }


    /**
     * 通过订单id获取支付信息
     * @param orderId   订单id
     * @return 查询结果
     */
    @Transactional(readOnly = true)
    @Override
    public Payment selectByOrderId(long orderId) throws MyException{
        String key = commonKey + orderId;
        Object tempPayment = redisUtil.get(key);
        if (tempPayment != null){
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(orderId));
            return (Payment) tempPayment;
        }

        try{
            Payment payment = paymentMapper.selectByOrderId(orderId);
            if (payment != null){
                kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_SET_CACHE_KEY, String.valueOf(orderId));
            }
            return payment;
        }catch (Exception e){
            log.info("支付查询出现异常msg={}", e.getMessage());
            throw new MyException("支付查询出现异常");
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
            long orderId = Long.parseLong(record.value());
            Payment payment = paymentMapper.selectByOrderId(orderId);
            String key = commonKey + orderId;
            redisUtil.set(key, payment, cacheTTL);
            log.info("key={}已经放入缓存", key);
        }else if (Objects.equals(record.key(), MQ_REMOVE_CACHE_KEY)){//移除缓存
            long orderId = Long.parseLong(record.value());
            String key = commonKey + orderId;
            redisUtil.del(key);
            log.info("key={}已经移除缓存", key);
        }
        //手动提交
        ack.acknowledge();
        log.info("offset={}手动提交成功", record.offset());
    }


}
