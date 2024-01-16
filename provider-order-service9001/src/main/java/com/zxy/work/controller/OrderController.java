package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;

import com.zxy.work.entities.Order;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.OrderService;
import lombok.extern.slf4j.Slf4j;
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
    public CommonResult createOrder(@RequestBody Order order){

        log.info( "********订单创建服务9001：*********" );

        //开始创建
        int result = orderService.create( order );

        if (result > 0){
            log.info( order + "订单创建成功" );
            return new CommonResult<>( StatusCode.SUCCESS,"订单创建成功" );
        }

        log.info( order + "订单创建失败" );
        return new CommonResult<>( StatusCode.FAILURE,"订单创建失败" );
    }


    /**
     * 取消订单，逻辑删除
     * @param order 传来的订单json
     * @return  取消结果
     */
    @DeleteMapping("/update/delete")
    public CommonResult deleteOrder(@RequestBody Order order){

        log.info( "********取消服务9001：*********" );

        int result = orderService.delete(order);

        if (result > 0){
            log.info( order + "取消成功" );
            return new CommonResult<>( StatusCode.SUCCESS,order.getId() );
        }

        log.info( order + "取消失败" );
        return new CommonResult<>( StatusCode.FAILURE,"取消失败" );
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/update/message")
    public CommonResult updateUser(@RequestBody Order order){

        log.info( "********更新信息服务9001：*********" );

        int result = orderService.update(order);

        if (result > 0){
            log.info(order + "信息更新成功");
            return new CommonResult<>(StatusCode.SUCCESS,order);
        }

        log.info(order + "信息更新失败");
        return new CommonResult<>(StatusCode.FAILURE,"修改失败");
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/{id}")
    public CommonResult getOrderById(@PathVariable("id")Integer id){

        log.info( "********查询服务9001：*********" );

        Order order = orderService.selectByOrderId(id);

        if (order == null){
            log.info( "查找失败" );
            return new CommonResult<>(StatusCode.FAILURE,"查找失败");
        }

        log.info( order + "查找成功" );
        return new CommonResult<>(StatusCode.SUCCESS,order);

    }


    /**
     * 根据用户Id获取订单信息
     * @param userId    传来的用户Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/user/history/{userId}")
    public CommonResult getOrderByUserId(@PathVariable("userId")Integer userId){

        log.info( "********用户历史订单查询服务9001：*********" );

        List<Order> orderList = orderService.selectByUserId(userId);

        if (orderList == null){
            log.info( "查找失败" );
            return new CommonResult<>(StatusCode.FAILURE,"查找失败");
        }

        log.info( orderList + "查找成功" );
        return new CommonResult<>(StatusCode.SUCCESS,orderList);

    }


    /**
     * 根据司机Id获取订单信息
     * @param driverId    传来的司机Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/driver/history/{driverId}")
    public CommonResult getOrderByDriverId(@PathVariable("driverId")Integer driverId){

        log.info( "********司机历史订单查询服务9001：*********" );

        List<Order> orderList = orderService.selectByDriverId(driverId);

        if (orderList == null){
            log.info( "查找失败" );
            return new CommonResult<>(StatusCode.FAILURE,"查找失败");
        }

        log.info( orderList + "查找成功" );
        return new CommonResult<>(StatusCode.SUCCESS,orderList);

    }









}
