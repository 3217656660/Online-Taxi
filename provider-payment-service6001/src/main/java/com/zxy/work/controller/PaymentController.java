package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Payment;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.PaymentService;
import com.zxy.work.util.MyString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/update/create")
    public ResponseEntity<Object> createPayment(@RequestBody Payment payment){
        log.info( "********支付创建服务6001：*********" );
        return ResponseEntity.ok( paymentService.create(payment) );
    }


    @DeleteMapping("/update/delete")
    public ResponseEntity<Object> deletePayment(@RequestBody Payment payment){
        log.info( "********删除支付信息服务6001：*********" );
        return ResponseEntity.ok( paymentService.delete(payment) );
    }


    @GetMapping("/getByOrderId/{orderId}")
    public ResponseEntity<Object> getPaymentByOrderId(@PathVariable("orderId")Integer orderId){
        log.info( "********查询支付服务6001：*********" );
        return ResponseEntity.ok( paymentService.selectByOrderId(orderId) );
    }


    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id")Integer id){
        log.info( "********查询支付服务6001：*********" );
        return ResponseEntity.ok( paymentService.selectById(id) );
    }


    @PutMapping("/update/message")
    public ResponseEntity<Object> update(@RequestBody Payment payment){
        log.info( "********更新支付服务6001：*********" );
        return ResponseEntity.ok( paymentService.update(payment) );
    }



}
