package com.zxy.work.service;


import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "provider-payment-service")
public interface PaymentServiceClient {

    @PostMapping("/payment/update/create")
    ApiResponse<String> create(@RequestBody Payment payment);

    @DeleteMapping("/payment/update/delete")
    ApiResponse<String> delete(@RequestParam("orderId") long orderId);

    @PutMapping("/payment/update/message")
    ApiResponse<String> update(@RequestBody Payment payment);

    @GetMapping("/payment/getByOrderId")
    ApiResponse<Object> getByOrderId(@RequestParam("orderId")long orderId);

}
