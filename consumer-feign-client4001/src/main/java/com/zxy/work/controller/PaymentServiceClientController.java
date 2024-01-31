package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> create(@RequestBody Payment payment){
        log.info("创建支付：" + payment);
        return paymentServiceClient.create(payment);
    }


    /**
     * 删除支付
     * @param payment 传来的支付json
     * @return 支付删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody Payment payment){
        log.info("删除支付：" + payment);
        return paymentServiceClient.delete(payment);
    }


    /**
     * 通过订单id获取支付信息
     * @param orderId   订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderId/{orderId}")
    public ResponseEntity<String> getByOrderId(@PathVariable("orderId")Integer orderId){
        log.info("通过订单id获取支付：" + orderId);
        return paymentServiceClient.getByOrderId(orderId);
    }


    /**
     * 通过id获取支付信息
     * @param id 传来的支付表id
     * @return  查询结果
     */
    @GetMapping("/getById/{id}")
    ResponseEntity<String> getById(@PathVariable("id")Integer id){
        log.info("通过id获取支付：" + id);
        return paymentServiceClient.getById(id);
    }


    /**
     * 更新支付信息
     * @param payment 传来的支付json信息
     * @return  更新结果
     */
    @PutMapping("/update")
    ResponseEntity<String> update(@RequestBody Payment payment){
        log.info("更新支付：" + payment);
        return paymentServiceClient.update(payment);
    }


}
