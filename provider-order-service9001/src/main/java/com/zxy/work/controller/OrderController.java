package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.OrderService;
import com.zxy.work.util.MyString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
        log.info( "********订单创建服务9001：*********" );
        return ResponseEntity.ok( orderService.create(order) );
    }


    /**
     * 取消订单，逻辑删除
     * @param order 传来的订单json
     * @return  取消结果
     */
    @DeleteMapping("/update/delete")
    public ResponseEntity<Object> deleteOrder(@RequestBody Order order){
        log.info( "********取消服务9001：*********" );
        return ResponseEntity.ok( orderService.delete(order) );
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/update/message")
    public ResponseEntity<Object> updateUser(@RequestBody Order order){
        log.info( "********更新信息服务9001：*********" );
        return ResponseEntity.ok( orderService.update(order) );
    }


    /**
     * 通过订单状态和用户id更新订单
     * @param order 传来的订单信息
     */
    @PostMapping("/updateByStatusAndUserId")
    public void updateByStatusAndUserId(@RequestBody Order order){
        orderService.updateByStatusAndUserId(order);
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable("id")Integer id){
        log.info( "********查询服务9001：*********" );
        return ResponseEntity.ok( orderService.selectByOrderId(id) );
    }


    /**
     * 根据用户Id获取历史订单信息
     * @param userId    传来的用户Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/user/history/{userId}")
    public ResponseEntity<Object> getOrderByUserId(@PathVariable("userId")Integer userId){
        log.info( "********用户历史订单查询服务9001：*********" );
        return ResponseEntity.ok( orderService.selectByUserId(userId) );
    }


    /**
     * 根据司机Id获取历史订单信息
     * @param driverId    传来的司机Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/driver/history/{driverId}")
    public ResponseEntity<Object> getOrderByDriverId(@PathVariable("driverId")Integer driverId){
        log.info( "********司机历史订单查询服务9001：*********" );
        return ResponseEntity.ok( orderService.selectByDriverId(driverId) );
    }


    /**
     * 通过用户订单状态获得订单信息
     * @param order 传来的订单信息
     * @return  获取到的信息
     */
    @PostMapping("/getByUserOrderStatus")
    public ResponseEntity<Object> getByUserOrderStatus(@RequestBody Order order){
        return ResponseEntity.ok( orderService.selectByUserOrderStatus(order) );
    }






}
