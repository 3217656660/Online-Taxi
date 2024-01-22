package com.zxy.work.controller;

import com.zxy.work.entities.Review;
import com.zxy.work.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/review")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    /**
     * 创建评论
     * @param review 评论信息
     * @return  创建结果
     */
    @PostMapping("/update/create")
    public ResponseEntity<Object> createReview(@RequestBody Review review){
        log.info( "********评价创建服务5001：*********" );
        return ResponseEntity.ok( reviewService.create(review) );
    }


    /**
     * 删除评论
     * @param review    评论信息
     * @return  评论删除结果
     */
    @DeleteMapping("/update/delete")
    public ResponseEntity<Object> deleteReview(@RequestBody Review review){
        log.info( "********删除评价信息服务5001：*********" );
        return ResponseEntity.ok( reviewService.delete(review) );
    }


    /**
     * 通过订单id获得评论
     * @param orderId   传来的订单id
     * @return  查询结果
     */
    @GetMapping("/getByOrderId/{orderId}")
    public ResponseEntity<Object> getReviewByOrderId(@PathVariable("orderId") Integer orderId){
        log.info( "********order id查询评价服务5001：*********" );
        return ResponseEntity.ok( reviewService.selectByOrderId(orderId) );
    }


    /**
     * 通过id查询评论
     * @param id    评论id
     * @return  查询结果
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id") Integer id){
        log.info( "********id查询评价服务5001：*********" );
        return ResponseEntity.ok( reviewService.selectById(id) );
    }



}
