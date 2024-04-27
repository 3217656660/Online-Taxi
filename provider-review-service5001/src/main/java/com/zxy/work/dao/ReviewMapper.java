package com.zxy.work.dao;


import com.zxy.work.entities.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {

    int create(Review review);

    int delete(@Param("orderId") long orderId);

    Review selectByOrderId(@Param("orderId") long orderId);

}
