package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/taxi/payment")
@SaCheckLogin
public class PaymentServiceClientController {

    @Resource
    private PaymentServiceClient paymentServiceClient;

    /**
     * 创建支付
     * @param payment 传来的支付json
     * @return  创建支付结果
     */
    @PostMapping("/create")
    public ApiResponse<String> create(@RequestBody Payment payment) throws MyException {
        log.info("创建支付：" + payment);
        try{
            return paymentServiceClient.create(payment);
        }catch (Exception e){
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 删除支付
     * @param orderId 传来的订单id
     * @return 支付删除结果
     */
    @DeleteMapping("/delete")
    public ApiResponse<String> delete(@RequestParam("orderId") long orderId) throws MyException {
        log.info("删除支付：" + orderId);
        try{
            return paymentServiceClient.delete(orderId);
        }catch (Exception e){
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 更新支付信息
     * @param payment 传来的支付json信息
     * @return  更新结果
     */
    @PutMapping("/update")
    public ApiResponse<String> update(@RequestBody Payment payment) throws MyException {
        log.info("更新支付：" + payment);
        try{
            return paymentServiceClient.update(payment);
        }catch (Exception e){
            throw new MyException(e.getMessage());
        }
    }


    /**
     * 通过订单id获取支付信息
     * @param orderId   订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderId")
    public ApiResponse<Object> getByOrderId(@RequestParam("orderId") long orderId) throws MyException {
        log.info("通过订单id获取支付：" + orderId);
        try{
            return paymentServiceClient.getByOrderId(orderId);
        }catch (Exception e){
            throw new MyException(e.getMessage());
        }
    }

}
