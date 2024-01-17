package com.zxy.work.controller;

import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/consumer/order")
public class OrderServiceClientController {
    @Resource
    private OrderServiceClient orderServiceClient;


    @PostMapping("/update/create")
    Map<String,Object> create(@RequestBody Order order){
        return orderServiceClient.create(order);
    }


    @DeleteMapping("/update/delete")
    Map<String,Object> delete(@RequestBody Order order){
        return orderServiceClient.delete(order);
    }


    @PutMapping("/update/message")
    Map<String,Object> update(@RequestBody Order order){
        return orderServiceClient.update(order);
    }


    @GetMapping("/get/{id}")
    Map<String,Object> getById(@PathVariable("id")Integer id){
        return orderServiceClient.getById(id);
    }



    @GetMapping("/get/user/history/{userId}")
    Map<String,Object>getByUserId(@PathVariable("userId")Integer userId){
        return orderServiceClient.getByUserId(userId);
    }



    @GetMapping("/get/driver/history/{driverId}")
    Map<String,Object> getByDriverId(@PathVariable("driverId")Integer driverId){
        return orderServiceClient.getByDriverId(driverId);
    }


}
