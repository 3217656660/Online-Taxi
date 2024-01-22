package com.zxy.work.service;

import com.zxy.work.entities.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "provider-order-service")
public interface OrderServiceClient {

    @PostMapping("/order/update/create")
    ResponseEntity<String> create(@RequestBody Order order);


    @DeleteMapping("/order/update/delete")
    ResponseEntity<String> delete(@RequestBody Order order);


    @PutMapping("/order/update/message")
    ResponseEntity<String> update(@RequestBody Order order);


    @GetMapping("/order/get/{id}")
    ResponseEntity<String> getById(@PathVariable("id")Integer id);


    @GetMapping("/order/get/user/history/{userId}")
    ResponseEntity<String> getByUserId(@PathVariable("userId")Integer userId);


    @GetMapping("/order/get/driver/history/{driverId}")
    ResponseEntity<String> getByDriverId(@PathVariable("driverId")Integer driverId);


    @PostMapping("/order/getByUserOrderStatus")
    ResponseEntity<String> getByUserOrderStatus(@RequestBody Order order);


    @PostMapping("/order/updateByStatusAndUserId")
    ResponseEntity<String> updateByStatusAndUserId(@RequestBody Order order);

}
