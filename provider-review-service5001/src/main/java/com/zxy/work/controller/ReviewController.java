package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Review;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.ReviewService;
import com.zxy.work.util.MyString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/review")
public class ReviewController {

    @Resource
    private ReviewService reviewService;


    @PostMapping("/update/create")
    public CommonResult createReview(@RequestBody Review review){
        log.info( "********评价创建服务5001：*********" );

        //开始创建
        int result = reviewService.create( review );

        if (result > 0){
            log.info( review + "评价创建成功" );
            return new CommonResult<>( StatusCode.SUCCESS, MyString.REVIEW_SUCCESS );
        }

        log.info( review + "评价创建失败" );
        return new CommonResult<>( StatusCode.FAILURE,MyString.REVIEW_ERROR );
    }


    @DeleteMapping("/update/delete")
    public CommonResult deleteReview(@RequestBody Review review){

        log.info( "********删除评价信息服务5001：*********" );

        int result = reviewService.delete(review);

        if (result > 0){
            log.info( review + "删除成功" );
            return new CommonResult<>( StatusCode.SUCCESS,review.getId() + MyString.DELETE_SUCCESS );
        }

        log.info( review + "删除失败" );
        return new CommonResult<>( StatusCode.FAILURE,MyString.DELETE_ERROR );
    }


    @GetMapping("/get/{orderId}")
    public CommonResult getReviewByOrderId(@PathVariable("orderId") Integer orderId){

        log.info( "********查询评价服务5001：*********" );

        Review review = reviewService.selectByOrderId(orderId);

        if (review == null){
            log.info( "查找失败" );
            return new CommonResult<>( StatusCode.FAILURE,MyString.FIND_ERROR );
        }

        log.info( review + "查找成功" );
        return new CommonResult<>(StatusCode.SUCCESS,review);
    }


}
