package com.zxy.work.dao;


import com.zxy.work.entities.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {

    int create(Review review);

    int delete(Review review);

    Review selectByOrderId(@Param("orderId") Integer orderId);

    Review selectById(@Param("id") Integer id);


}
