package com.zxy.work.dao;

import com.zxy.work.entities.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {


    int create(Payment payment);


    int delete(Payment payment);


    Payment selectByOrderId(@Param("orderId") Integer orderId);


    Payment selectById(@Param("id") Integer id);


    int update(Payment payment);


}
