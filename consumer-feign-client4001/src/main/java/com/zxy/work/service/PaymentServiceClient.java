package com.zxy.work.service;


import com.zxy.work.entities.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "provider-payment-service")
public interface PaymentServiceClient {

    @PostMapping("/payment/update/create")
    ResponseEntity<String> create(@RequestBody Payment payment);


    @DeleteMapping("/payment/update/delete")
    ResponseEntity<String> delete(@RequestBody Payment payment);


    @GetMapping("/payment/getByOrderId/{orderId}")
    ResponseEntity<String> getByOrderId(@PathVariable("orderId")Integer orderId);


    @GetMapping("/payment/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id")Integer id);


    @PutMapping("/payment/update/message")
    ResponseEntity<String> update(@RequestBody Payment payment);

}
