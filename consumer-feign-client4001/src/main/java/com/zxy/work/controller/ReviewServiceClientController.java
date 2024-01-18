package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.zxy.work.entities.Review;
import com.zxy.work.service.ReviewServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/taxi/review")
@SaCheckLogin
public class ReviewServiceClientController {

    @Resource
    private ReviewServiceClient reviewServiceClient;


    @PostMapping("/create")
    @SaIgnore
    public Map<String,Object> create(@RequestBody Review review){
        return reviewServiceClient.create(review);
    }


    @DeleteMapping("/delete")
    public Map<String,Object> delete(@RequestBody Review review){
        return reviewServiceClient.delete(review);
    }


    @GetMapping("/getByOrderId/{orderId}")
    public Map<String,Object> getByOrderId(@PathVariable("orderId") Integer orderId){
        return reviewServiceClient.getByOrderId(orderId);
    }


}
