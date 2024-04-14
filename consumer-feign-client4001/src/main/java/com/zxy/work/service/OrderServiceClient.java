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

    @DeleteMapping("/order/update/delete")
    ApiResponse<String> delete(@RequestParam("id") Integer id);

    @PutMapping("/order/update/message")
    ApiResponse<String> update(@RequestBody Order order);

    @GetMapping("/order/getById")
    ApiResponse<Object> getById(@RequestParam("id") Integer id);

    @GetMapping("/order/get/user/history")
    ApiResponse< List<Order> > getByUserId(@RequestParam("userId")Integer userId);

    @GetMapping("/order/get/driver/history")
    ApiResponse< List<Order> > getByDriverId(@RequestParam("driverId")Integer driverId);

    @GetMapping("/order/checkOrder")
    ApiResponse<Order> checkOrder(@RequestParam("userId") Integer userId);

}
