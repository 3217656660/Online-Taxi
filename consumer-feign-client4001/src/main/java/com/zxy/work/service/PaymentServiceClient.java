package com.zxy.work.service;


import com.zxy.work.entities.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "provider-payment-service")
public interface PaymentServiceClient {

    @PostMapping("/payment/update/create")
    Map<String,Object> create(@RequestBody Payment payment);


    @DeleteMapping("/payment/update/delete")
    Map<String,Object> delete(@RequestBody Payment payment);


    @GetMapping("/payment/get/{orderId}")
    Map<String,Object> getByOrderId(@PathVariable("orderId")Integer orderId);


}
