package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
    public ResponseEntity<String> create(@RequestBody Order order){
        log.info("创建订单:" + order);
        return orderServiceClient.create(order);
    }


    /**
     * 取消订单，逻辑删除
     * @param order 传来的订单json
     * @return  取消结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody Order order){
        log.info("取消订单:" + "取消用户" + order.getUserId());
        return orderServiceClient.delete(order);
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/message")
    public ResponseEntity<String> updateMessage(@RequestBody Order order){
        log.info("更新订单:" + order);
        return orderServiceClient.update(order);
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<String> getById(@PathVariable("id")Integer id){
        log.info("通过id获取订单:" + id);
        return orderServiceClient.getById(id);
    }


    /**
     * 根据用户Id获取历史订单信息
     * @param userId    传来的用户Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByUserId/{userId}")
    public ResponseEntity<String> getByUserId(@PathVariable("userId")Integer userId){
        log.info("根据用户Id获取历史订单:" + userId);
        return orderServiceClient.getByUserId(userId);
    }


    /**
     * 根据司机Id获取历史订单信息
     * @param driverId    传来的司机Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByDriverId/{driverId}")
    public ResponseEntity<String> getByDriverId(@PathVariable("driverId")Integer driverId){
        log.info("根据司机Id获取历史订单:" + driverId);
        return orderServiceClient.getByDriverId(driverId);
    }


}
