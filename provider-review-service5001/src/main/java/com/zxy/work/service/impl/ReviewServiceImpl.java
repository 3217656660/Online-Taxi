package com.zxy.work.service.impl;

import com.zxy.work.dao.ReviewMapper;
import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Review;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.ReviewService;
import com.zxy.work.util.MyString;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;


    @Override
    public Object create(Review review) {        //开始创建
        Date now = new Date();
        review.setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);
        return reviewMapper.create(review) == 0
                ? MyString.REVIEW_ERROR
                :MyString.REVIEW_SUCCESS;
    }


    @Override
    public Object delete(Review review) {
        Date now = new Date();
        review.setUpdateTime(now)
                .setIsDeleted(1);
        return reviewMapper.delete(review) == 0
                ? MyString.DELETE_ERROR
                :MyString.DELETE_SUCCESS;
    }


    @Override
    public Object selectByOrderId(Integer orderId) {
        Review review = reviewMapper.selectByOrderId(orderId);
        return review == null
                ? MyString.FIND_ERROR
                : review;
    }


    @Override
    public Object selectById(Integer id) {
        Review review = reviewMapper.selectById(id);
        return review == null
                ? MyString.FIND_ERROR
                : review;
    }


}
