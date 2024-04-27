package com.zxy.work.service;

import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Review;

public interface ReviewService {

    int create(Review review) throws MyException;

    int delete(long orderId) throws MyException;

    Review selectByOrderId(long orderId) throws MyException;

}
