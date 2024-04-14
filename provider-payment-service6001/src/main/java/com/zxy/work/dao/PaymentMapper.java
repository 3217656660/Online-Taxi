package com.zxy.work.dao;

import com.zxy.work.entities.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {

    int create(Payment payment);

    int delete(@Param("orderId")Integer orderId);

    int update(Payment payment);

    Payment selectByOrderId(@Param("orderId") Integer orderId);

}
