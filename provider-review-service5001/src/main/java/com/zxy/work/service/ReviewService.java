package com.zxy.work.service;

import com.zxy.work.entities.Review;

public interface ReviewService {

    Object create(Review review);

    Object delete(Review review);

    Object selectByOrderId(Integer orderId);

    Object selectById(Integer id);

}
