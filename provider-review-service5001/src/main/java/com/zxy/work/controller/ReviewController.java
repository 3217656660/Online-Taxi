package com.zxy.work.controller;


import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Review;
import com.zxy.work.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<String> createReview(@RequestBody Review review) throws MyException {
        log.info("创建评论服务提供者：" + review);
        return reviewService.create(review) == 1
                ? ApiResponse.success("评价成功")
                : ApiResponse.error(600, "评价失败");
    }


    /**
     * 删除评论
     * @param  orderId  传来的订单id
     * @return  评论删除结果
     */
    @DeleteMapping("/update/delete")
    public ApiResponse<String> deleteReview(@RequestParam("orderId") long orderId) throws MyException {
        log.info("删除评论服务提供者：" + orderId);
        return reviewService.delete(orderId) == 1
                ? ApiResponse.success("评价删除成功")
                : ApiResponse.error(600, "评价删除失败");
    }


    /**
     * 通过订单id获得评论
     * @param orderId   传来的订单id
     * @return  查询结果
     */
    @GetMapping("/getByOrderId")
    public ApiResponse<Object> getReviewByOrderId(@RequestParam("orderId") long orderId) throws MyException {
        log.info("通过订单id获得评论服务提供者：" + orderId);
        Review review = reviewService.selectByOrderId(orderId);
        return review != null
                ? ApiResponse.success(review)
                : ApiResponse.error(600, "根据订单id查询评价失败");
    }

}
