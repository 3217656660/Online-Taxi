package com.zxy.work.service.impl;

import com.zxy.work.dao.PaymentMapper;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentService;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.Date;

public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentMapper paymentMapper;


    @Override
    public int create(Payment payment) {
        Date now = new Date();
        payment.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);
        return paymentMapper.create(payment);
    }


    @Override
    public int delete(Payment payment) {
        Date now = new Date();
        payment.setUpdateTime(now)
                .setIsDeleted(1);
        return paymentMapper.delete(payment);
    }


    @Override
    public Payment selectByOrderId(Integer orderId) {
        return paymentMapper.selectByOrderId(orderId);
    }


}
