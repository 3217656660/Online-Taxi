package com.zxy.work.service;

import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Review;

public interface ReviewService {

    int create(Review review) throws MyException;

    int delete(Integer id) throws MyException;

    Review selectByOrderId(Integer orderId) throws MyException;

}
