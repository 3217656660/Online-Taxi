package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Review;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.ReviewServiceClient;
import com.zxy.work.util.MyString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/consumer/review")
public class ReviewServiceClientController {

    @Resource
    private ReviewServiceClient reviewServiceClient;


    @PostMapping("/update/create")
    public Map<String,Object> create(@RequestBody Review review){
        return reviewServiceClient.create(review);
    }


    @DeleteMapping("/update/delete")
    public Map<String,Object> delete(@RequestBody Review review){
        return reviewServiceClient.delete(review);
    }


    @GetMapping("/get/{orderId}")
    public Map<String,Object> getByOrderId(@PathVariable("orderId") Integer orderId){
        return reviewServiceClient.getByOrderId(orderId);
    }


}
