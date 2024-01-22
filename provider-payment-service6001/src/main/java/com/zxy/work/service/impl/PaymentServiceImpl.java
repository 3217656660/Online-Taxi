package com.zxy.work.service.impl;

import com.zxy.work.dao.PaymentMapper;
import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Payment;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.PaymentService;
import com.zxy.work.util.MyString;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentMapper paymentMapper;


    @Override
    public Object create(Payment payment) {
        Date now = new Date();
        payment.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);
        return paymentMapper.create(payment) == 0
                ? MyString.PAYMENT_SUCCESS
                : MyString.PAYMENT_ERROR;
    }


    @Override
    public Object delete(Payment payment) {
        Date now = new Date();
        payment.setUpdateTime(now)
                .setIsDeleted(1);
        return paymentMapper.delete(payment) == 0
                ? MyString.DELETE_ERROR
                : MyString.DELETE_SUCCESS;
    }


    @Override
    public Object selectByOrderId(Integer orderId) {
        Payment payment = paymentMapper.selectByOrderId(orderId);
        return payment == null
                ? MyString.FIND_ERROR
                : payment;
    }


    @Override
    public Object selectById(Integer id) {
        Payment payment = paymentMapper.selectById(id);
        return payment == null
                ? MyString.FIND_ERROR
                : payment;
    }


    @Override
    public Object update(Payment payment) {
        return paymentMapper.update(payment) == 0
                ? MyString.UPDATE_ERROR
                : MyString.UPDATE_SUCCESS;
    }


}
