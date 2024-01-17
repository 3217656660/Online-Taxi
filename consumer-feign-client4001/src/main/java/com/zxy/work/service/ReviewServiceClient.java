package com.zxy.work.service;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Review;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "provider-review-service")
public interface ReviewServiceClient {

    @PostMapping("/review/update/create")
    Map<String,Object> create(@RequestBody Review review);


    @DeleteMapping("/review/update/delete")
    Map<String,Object> delete(@RequestBody Review review);


    @GetMapping("/review/get/{orderId}")
    Map<String,Object> getByOrderId(@PathVariable("orderId") Integer orderId);

}
