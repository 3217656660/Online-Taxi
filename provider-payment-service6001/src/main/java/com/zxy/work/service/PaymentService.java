package com.zxy.work.service;

import com.zxy.work.entities.Payment;

public interface PaymentService {

    Object create(Payment payment);

    Object delete(Payment payment);

    Object selectByOrderId(Integer orderId);

    Object selectById(Integer id);

    Object update(Payment payment);

}
