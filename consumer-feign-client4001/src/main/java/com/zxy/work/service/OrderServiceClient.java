package com.zxy.work.service;

import com.zxy.work.entities.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "provider-order-service")
public interface OrderServiceClient {

    @PostMapping("/order/update/create")
    Map<String,Object> create(@RequestBody Order order);


    @DeleteMapping("/order/update/delete")
    Map<String,Object> delete(@RequestBody Order order);


    @PutMapping("/order/update/message")
    Map<String,Object> update(@RequestBody Order order);


    @GetMapping("/order/get/{id}")
    Map<String,Object> getById(@PathVariable("id")Integer id);


    @GetMapping("/order/get/user/history/{userId}")
    Map<String,Object>getByUserId(@PathVariable("userId")Integer userId);


    @GetMapping("/order/get/driver/history/{driverId}")
    Map<String,Object> getByDriverId(@PathVariable("driverId")Integer driverId);


    @PostMapping("/order/getByUserOrderStatus")
    Map<String,Object> getByUserOrderStatus(@RequestBody Order order);


    @PostMapping("/order/updateByStatusAndUserId")
    Map<String,Object> updateByStatusAndUserId(@RequestBody Order order);

}
