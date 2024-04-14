package com.zxy.work.service.impl;

import com.zxy.work.dao.PaymentMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentService;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentMapper paymentMapper;

    @Resource
    private CacheUtil redisUtil;

    /**
     * 设置通用缓存TTL(30分钟)
     */
    private static final int cacheTTL = 30 * 60;

    /**
     * 设置缓存通用key前缀
     */
    private static final String commonKey = "payment:orderId:";


    /**
     * 创建支付
     * @param payment 传来的支付json
     * @return  创建支付结果
     */
    @Transactional
    @Override
    public int create(Payment payment) throws MyException {
        String key = commonKey + payment.getOrderId();
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
                Payment tempPayment = paymentMapper.selectByOrderId(payment.getOrderId());
                redisUtil.set(key, tempPayment, cacheTTL);
                log.info("key={}支付创建成功并放入缓存", key);
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
    public int delete(Integer orderId) throws MyException{
        String key = commonKey + orderId;
        try{
            int result = paymentMapper.delete(orderId);
            if (result == 1){
                redisUtil.del(key);
                log.info("支付删除成功，并清除了key={}的缓存", key);
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
        String key = commonKey + payment.getOrderId();
        try{
            int result = paymentMapper.update(payment);
            Payment select = paymentMapper.selectByOrderId(payment.getOrderId());
            redisUtil.set(key, select ,cacheTTL);
            log.info("key={}产生了更新，并重新设置进缓存", key);
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
    @Override
    public Payment selectByOrderId(Integer orderId) throws MyException{
        String key = commonKey + orderId;
        Object tempPayment = redisUtil.get(key);
        if (tempPayment != null){
            //增加缓存时间
            redisUtil.set(key, tempPayment, cacheTTL);
            return (Payment) tempPayment;
        }

        try{
            Payment payment = paymentMapper.selectByOrderId(orderId);
            if (payment != null)
                redisUtil.set(key, payment, cacheTTL);
            return payment;
        }catch (Exception e){
            log.info("支付查询出现异常msg={}", e.getMessage());
            throw new MyException("支付查询出现异常");
        }
    }

}
