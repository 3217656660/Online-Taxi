package com.zxy.work.service.impl;

import com.zxy.work.dao.ReviewMapper;
import com.zxy.work.entities.Payment;
import com.zxy.work.entities.Review;
import com.zxy.work.service.ReviewService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;


    @Override
    public int create(Review review) {
        Date now = new Date();
        review.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);
        return reviewMapper.create(review);
    }


    @Override
    public int delete(Review review) {
        Date now = new Date();
        review.setUpdateTime(now)
                .setIsDeleted(1);
        return reviewMapper.delete(review);
    }


    @Override
    public Review selectByOrderId(Integer orderId) {
        return reviewMapper.selectByOrderId(orderId);
    }

}
