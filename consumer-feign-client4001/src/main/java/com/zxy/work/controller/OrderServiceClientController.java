package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;
import com.zxy.work.service.OrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    public ApiResponse<String> create(@RequestBody Order order) throws MyException {
        log.info("创建订单:" + order);
        try{
            return orderServiceClient.create(order);
        }catch (Exception e){
            log.info("msg={}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 取消订单，逻辑删除
     * @param id 传来的订单id
     * @return  取消结果
     */
    @DeleteMapping("/delete")
    public ApiResponse<String> delete(@RequestParam("id") Integer id) throws MyException {
        log.info("取消订单:" + "取消id=" + id);
        try{
            return orderServiceClient.delete(id);
        }catch (Exception e){
            log.info("msg={}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/message")
    public ApiResponse<String> updateMessage(@RequestBody Order order) throws MyException {
        log.info("更新订单:" + order);
        try{
            return orderServiceClient.update(order);
        }catch (Exception e){
            log.info("msg={}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/getById")
    public ApiResponse<Object> getById(@RequestParam("id")Integer id) throws MyException {
        log.info("通过id获取订单:" + id);
        try{
            return orderServiceClient.getById(id);
        }catch (Exception e){
            log.info("msg={}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 根据用户Id获取历史订单信息
     * @param userId    传来的用户Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByUserId")
    public ApiResponse<List<Order>>  getByUserId(@RequestParam("userId")Integer userId) throws MyException {
        log.info("根据用户Id获取历史订单:" + userId);
        try{
            return orderServiceClient.getByUserId(userId);
        }catch (Exception e){
            log.info("msg={}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 根据司机Id获取历史订单信息
     * @param driverId    传来的司机Id
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByDriverId")
    public ApiResponse< List<Order> >  getByDriverId(@RequestParam("driverId")Integer driverId) throws MyException {
        log.info("根据司机Id获取历史订单:" + driverId);
        try{
            return orderServiceClient.getByDriverId(driverId);
        }catch (Exception e){
            log.info("msg={}", e.getMessage());
            throw new MyException(e.getMessage());
        }
    }

}
