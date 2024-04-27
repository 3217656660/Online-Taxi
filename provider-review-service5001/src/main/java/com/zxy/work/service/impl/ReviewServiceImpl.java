package com.zxy.work.service.impl;

import com.zxy.work.dao.ReviewMapper;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Review;
import com.zxy.work.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;


    /**
     * 创建评论
     * @param review 传来的评论信息
     * @return 创建结果
     */
    @Transactional
    @Override
    public int create(Review review) throws MyException {
        Review select;
        try{
            select = reviewMapper.selectByOrderId(review.getOrderId());
        }catch (Exception e){
            log.error("查询评论出现异常，id={},orderId={}", review.getId(), review.getOrderId());
            throw new MyException("查询评论出现异常");
        }
        if (select != null)
            throw new MyException("您已经评价过了");

        try{
            return reviewMapper.create(review);
        }catch (Exception e){
            log.error("创建评论出现异常,orderId={}", review.getOrderId());
            throw new MyException("创建评论出现异常");
        }
    }

    /**
     * 删除评论
     * @param orderId 评论的订单id
     * @return 删除结果
     */
    @Transactional
    @Override
    public int delete(long orderId) throws MyException {
        try{
            return reviewMapper.delete(orderId);
        }catch (Exception e){
            log.error("删除评论出现异常,id={}", orderId);
            throw new MyException("删除评论出现异常");
        }
    }


    /**
     * 根据订单id查询评价
     * @param orderId 订单id
     * @return 查询结果
     */
    @Transactional(readOnly = true)
    @Override
    public Review selectByOrderId(long orderId) throws MyException {
        try{
            return  reviewMapper.selectByOrderId(orderId);
        }catch (Exception e){
            log.error("查询评论出现异常,orderId={}", orderId);
            throw new MyException("查询评论出现异常");
        }
    }

}
