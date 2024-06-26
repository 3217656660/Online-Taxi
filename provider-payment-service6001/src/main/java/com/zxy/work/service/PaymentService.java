package com.zxy.work.service;

import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Payment;

public interface PaymentService {

    int create(Payment payment) throws MyException;

    int delete(long orderId) throws MyException;

    int update(Payment payment) throws MyException;

    Payment selectByOrderId(long orderId) throws MyException;

}
