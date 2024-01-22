package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/taxi/order")
@SaCheckLogin
public class OrderServiceClientController {
    @Resource
    private OrderServiceClient orderServiceClient;

    /**
     * 创建订单
     * @param order 传来的用户信息json
     * @return 创建结果
     */
    @PostMapping("/create")
    @SaIgnore
    ResponseEntity<String> create(@RequestBody Order order){
        return orderServiceClient.create(order);
    }


    /**
     * 取消订单，逻辑删除
     * @param order 传来的订单json
     * @return  取消结果
     */
    @DeleteMapping("/delete")
    ResponseEntity<String> delete(@RequestBody Order order){
        return orderServiceClient.delete(order);
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/message")
    ResponseEntity<String> updateMessage(@RequestBody Order order){
        return orderServiceClient.update(order);
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id")Integer id){
        return orderServiceClient.getById(id);
    }


    /**
     * 根据用户Id获取历史订单信息
     * @param userId    传来的用户Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByUserId/{userId}")
    ResponseEntity<String> getByUserId(@PathVariable("userId")Integer userId){
        return orderServiceClient.getByUserId(userId);
    }


    /**
     * 根据司机Id获取历史订单信息
     * @param driverId    传来的司机Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByDriverId/{driverId}")
    ResponseEntity<String> getByDriverId(@PathVariable("driverId")Integer driverId){
        return orderServiceClient.getByDriverId(driverId);
    }


}
