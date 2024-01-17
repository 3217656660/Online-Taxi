package com.zxy.work.service;

import com.zxy.work.entities.Review;

public interface ReviewService {

    int create(Review review);

    int delete(Review review);

    Review selectByOrderId(Integer orderId);

}
