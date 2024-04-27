package com.zxy.work.dao;

import com.zxy.work.entities.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {

    int create(Payment payment);

    int delete(@Param("orderId")long orderId);

    int update(Payment payment);

    Payment selectByOrderId(@Param("orderId")long orderId);

}
