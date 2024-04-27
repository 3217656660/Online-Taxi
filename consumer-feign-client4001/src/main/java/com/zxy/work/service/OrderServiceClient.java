package com.zxy.work.service;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "provider-order-service")
public interface OrderServiceClient {

    @PostMapping("/order/update/create")
    ApiResponse<String> create(@RequestBody Order order);

    @PutMapping("/order/update/message")
    ApiResponse<String> update(@RequestBody Order order);

    @GetMapping("/order/getById")
    ApiResponse<Object> getById(@RequestParam("id") long id);

    @GetMapping("/order/get/user/history")
    ApiResponse< List<Order> > getByUserId(@RequestParam("userId")long userId);

    @GetMapping("/order/get/driver/history")
    ApiResponse< List<Order> > getByDriverId(@RequestParam("driverId")long driverId);

    @GetMapping("/order/checkOrder")
    ApiResponse<Order> checkOrder(@RequestParam("userId") long userId);

}
