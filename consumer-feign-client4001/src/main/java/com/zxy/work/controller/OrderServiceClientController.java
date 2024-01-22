package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/taxi/order")
@SaCheckLogin
public class OrderServiceClientController {
    @Resource
    private OrderServiceClient orderServiceClient;


    @PostMapping("/create")
    @SaIgnore
    ResponseEntity<String> create(@RequestBody Order order){
        return orderServiceClient.create(order);
    }


    @DeleteMapping("/delete")
    ResponseEntity<String> delete(@RequestBody Order order){
        return orderServiceClient.delete(order);
    }


    @PutMapping("/message")
    ResponseEntity<String> updateMessage(@RequestBody Order order){
        return orderServiceClient.update(order);
    }


    @GetMapping("/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id")Integer id){
        return orderServiceClient.getById(id);
    }


    @GetMapping("/getByUserId/{userId}")
    ResponseEntity<String> getByUserId(@PathVariable("userId")Integer userId){
        return orderServiceClient.getByUserId(userId);
    }


    @GetMapping("/getByDriverId/{driverId}")
    ResponseEntity<String> getByDriverId(@PathVariable("driverId")Integer driverId){
        return orderServiceClient.getByDriverId(driverId);
    }


}
