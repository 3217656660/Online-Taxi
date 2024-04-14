package com.zxy.work.service;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.Review;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "provider-review-service")
public interface ReviewServiceClient {

    @PostMapping("/review/update/create")
    ApiResponse<String> create(@RequestBody Review review);


    @DeleteMapping("/review/update/delete")
    ApiResponse<String> delete(@RequestParam("id") Integer id);


    @GetMapping("/review/getByOrderId")
    ApiResponse<Object> getByOrderId(@RequestParam("orderId") Integer orderId);

}
