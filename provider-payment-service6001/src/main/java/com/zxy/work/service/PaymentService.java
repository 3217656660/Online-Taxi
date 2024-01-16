package com.zxy.work.service;

import com.zxy.work.entities.Payment;
import org.apache.ibatis.annotations.Param;

public interface PaymentService {

    int create(Payment payment);

    int delete(Payment payment);

    Payment selectByOrderId(Integer orderId);

}
