package com.zxy.work.controller;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
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
    public ApiResponse<String> createOrder(@RequestBody Order order) throws MyException {
        log.info("创建订单服务提供者:" + order);
        return orderService.create(order) == 1
                ? ApiResponse.success("订单创建成功")
                : ApiResponse.error(600, "订单创建失败");
    }


    /**
     * 用户删除订单，逻辑删除
     * @param id 传来的订单id
     * @return  删除结果
     */
    @DeleteMapping("/update/deleteByUser")
    public ApiResponse<String> deleteByUser(@RequestParam("id") long id) throws MyException {
        log.info("取消订单服务提供者:" + "id=" + id);
        return orderService.deleteByUser(id) == 1
                ? ApiResponse.success("订单删除成功")
                : ApiResponse.error(600, "订单删除失败");
    }


    /**
     * 司机删除订单，逻辑删除
     * @param id 传来的订单id
     * @return  删除结果
     */
    @DeleteMapping("/update/deleteByDriver")
    public ApiResponse<String> deleteByDriver(@RequestParam("id") long id) throws MyException {
        log.info("取消订单服务提供者:" + "id=" + id);
        return orderService.deleteByDriver(id) == 1
                ? ApiResponse.success("订单删除成功")
                : ApiResponse.error(600, "订单删除失败");
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/update/message")
    public ApiResponse<String> update(@RequestBody Order order) throws MyException {
        log.info("更新订单服务提供者:" + order);
        return orderService.update(order) == 1
                ? ApiResponse.success("订单更新成功")
                : ApiResponse.error(600, "订单更新失败");
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/getById")
    public ApiResponse<Object> getOrderById(@RequestParam("id") long id) throws MyException {
        log.info("通过id获取订单服务提供者:" + id);
        Order order = orderService.selectByOrderId(id);
        return order != null
                ? ApiResponse.success(order)
                : ApiResponse.error(600, "订单未查询到");
    }


    /**
     * 根据用户Id获取历史订单信息
     * @param userId    传来的用户Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/user/history")
    public ApiResponse< List<Order> > getOrderByUserId(@RequestParam("userId")long userId) throws MyException {
        log.info("根据用户Id获取历史订单服务提供者:" + userId);
        return ApiResponse.success(orderService.selectByUserId(userId));
    }


    /**
     * 根据司机Id获取历史订单信息
     * @param driverId   传来的司机Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/driver/history")
    public ApiResponse< List<Order> > getOrderByDriverId(@RequestParam("driverId")long driverId) throws MyException {
        log.info("根据司机Id获取历史订单服务提供者:" + driverId);
        return ApiResponse.success(orderService.selectByDriverId(driverId));
    }


    /**
     * 用户通过订单id查询订单
     * @param id 订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderIdWithUser")
    public ApiResponse<Order> selectByOrderIdWithUser(@RequestParam("id") long id) throws MyException{
        return ApiResponse.success(orderService.selectByOrderIdWithUser(id));
    }


    /**
     * 司机通过订单id查询订单
     * @param id 订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderIdWithDriver")
    public ApiResponse<Order> selectByOrderIdWithDriver(@RequestParam("id") long id) throws MyException{
        return ApiResponse.success(orderService.selectByOrderIdWithDriver(id));
    }


    /**
     * 检查未解决的订单
     * @param userId 传来的乘客id
     * @return 处理结果
     */
    @GetMapping("/checkOrder")
    public ApiResponse<Order> checkOrder(@RequestParam("userId") long userId) throws MyException {
        log.info("检查是否有未解决的订单userId={}", userId);
        return ApiResponse.success(orderService.selectNotSolve(userId));
    }

}
