package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.zxy.work.entities.Review;
import com.zxy.work.service.ReviewServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/taxi/review")
@SaCheckLogin
public class ReviewServiceClientController {

    @Resource
    private ReviewServiceClient reviewServiceClient;

    /**
     * 创建评论
     * @param review 传来的评论信息
     * @return  评论结果
     */
    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody Review review){
        log.info("创建评论：" + review);
        return reviewServiceClient.create(review);
    }


    /**
     * 删除评论
     * @param review    评论信息
     * @return  评论删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody Review review){
        log.info("删除评论：" + review.getId());
        return reviewServiceClient.delete(review);
    }


    /**
     * 通过订单id获得评论
     * @param orderId   传来的订单id
     * @return  查询结果
     */
    @GetMapping("/getByOrderId/{orderId}")
    public ResponseEntity<String> getByOrderId(@PathVariable("orderId") Integer orderId){
        log.info("通过订单id获得评论：" + orderId);
        return reviewServiceClient.getByOrderId(orderId);
    }


    /**
     * 通过id查询评论
     * @param id    评论id
     * @return  查询结果
     */
    @GetMapping("/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id") Integer id){
        log.info("通过id查询评论：" + id);
        return reviewServiceClient.getById(id);
    }


}
