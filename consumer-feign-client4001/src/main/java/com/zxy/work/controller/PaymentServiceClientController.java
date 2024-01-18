package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/taxi/payment")
@SaCheckLogin
public class PaymentServiceClientController {

    @Resource
    private PaymentServiceClient paymentServiceClient;

    @PostMapping("/create")
    @SaIgnore
    public Map<String,Object> create(@RequestBody Payment payment){
        return paymentServiceClient.create(payment);
    }


    @DeleteMapping("/delete")
    public Map<String,Object> delete(@RequestBody Payment payment){
        return paymentServiceClient.delete(payment);
    }


    @GetMapping("/getByOrderId/{orderId}")
    public Map<String,Object> getByOrderId(@PathVariable("orderId")Integer orderId){
        return paymentServiceClient.getByOrderId(orderId);
    }

}
