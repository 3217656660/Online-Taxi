package com.zxy.work.controller;

import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;


    /**
     * 创建订单
     * @param order 传来的用户信息json
     * @return 创建结果
     */
    @PostMapping("/update/create")
    public ResponseEntity<Object> createOrder(@RequestBody Order order){
        log.info("创建订单服务提供者:" + order);
        return ResponseEntity.ok( orderService.create(order) );
    }


    /**
     * 取消订单，逻辑删除
     * @param order 传来的订单json
     * @return  取消结果
     */
    @DeleteMapping("/update/delete")
    public ResponseEntity<Object> deleteOrder(@RequestBody Order order){
        log.info("取消订单服务提供者:" + "取消用户" + order.getUserId());
        return ResponseEntity.ok( orderService.delete(order) );
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/update/message")
    public ResponseEntity<Object> updateUser(@RequestBody Order order){
        log.info("更新订单服务提供者:" + order);
        return ResponseEntity.ok( orderService.update(order) );
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable("id")Integer id){
        log.info("通过id获取订单服务提供者:" + id);
        return ResponseEntity.ok( orderService.selectByOrderId(id) );
    }


    /**
     * 根据用户Id获取历史订单信息
     * @param userId    传来的用户Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/user/history/{userId}")
    public ResponseEntity<Object> getOrderByUserId(@PathVariable("userId")Integer userId){
        log.info("根据用户Id获取历史订单服务提供者:" + userId);
        return ResponseEntity.ok( orderService.selectByUserId(userId) );
    }


    /**
     * 根据司机Id获取历史订单信息
     * @param driverId    传来的司机Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/driver/history/{driverId}")
    public ResponseEntity<Object> getOrderByDriverId(@PathVariable("driverId")Integer driverId){
        log.info("根据司机Id获取历史订单服务提供者:" + driverId);
        return ResponseEntity.ok( orderService.selectByDriverId(driverId) );
    }


    /**
     * 检查未解决的订单
     * @param userId 传来的乘客id
     * @return 处理结果
     */
    @GetMapping("/checkOrder/{userId}")
    public ResponseEntity<Object> checkOrder(@PathVariable("userId") Integer userId){
        log.info("检查是否有未解决的订单userId={}", userId);
        return ResponseEntity.ok( orderService.selectNotSolve(userId) );
    }

}
