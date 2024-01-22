package com.zxy.work.service;

import com.zxy.work.entities.Review;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "provider-review-service")
public interface ReviewServiceClient {

    @PostMapping("/review/update/create")
    ResponseEntity<String> create(@RequestBody Review review);


    @DeleteMapping("/review/update/delete")
    ResponseEntity<String> delete(@RequestBody Review review);


    @GetMapping("/review/getByOrderId/{orderId}")
    ResponseEntity<String> getByOrderId(@PathVariable("orderId") Integer orderId);


    @GetMapping("/review/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id") Integer id);

}
