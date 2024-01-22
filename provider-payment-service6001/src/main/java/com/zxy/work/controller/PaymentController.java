package com.zxy.work.controller;

import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentService;
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

    /**
     * 创建支付
     * @param payment 传来的支付json
     * @return  创建支付结果
     */
    @PostMapping("/update/create")
    public ResponseEntity<Object> createPayment(@RequestBody Payment payment){
        log.info( "********支付创建服务6001：*********" );
        return ResponseEntity.ok( paymentService.create(payment) );
    }


    /**
     * 删除支付
     * @param payment 传来的支付json
     * @return 支付删除结果
     */
    @DeleteMapping("/update/delete")
    public ResponseEntity<Object> deletePayment(@RequestBody Payment payment){
        log.info( "********删除支付信息服务6001：*********" );
        return ResponseEntity.ok( paymentService.delete(payment) );
    }


    /**
     * 通过订单id获取支付信息
     * @param orderId   订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderId/{orderId}")
    public ResponseEntity<Object> getPaymentByOrderId(@PathVariable("orderId")Integer orderId){
        log.info( "********查询支付服务6001：*********" );
        return ResponseEntity.ok( paymentService.selectByOrderId(orderId) );
    }


    /**
     * 通过id获取支付信息
     * @param id 传来的支付表id
     * @return  查询结果
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id")Integer id){
        log.info( "********查询支付服务6001：*********" );
        return ResponseEntity.ok( paymentService.selectById(id) );
    }


    /**
     * 更新支付信息
     * @param payment 传来的支付json信息
     * @return  更新结果
     */
    @PutMapping("/update/message")
    public ResponseEntity<Object> update(@RequestBody Payment payment){
        log.info( "********更新支付服务6001：*********" );
        return ResponseEntity.ok( paymentService.update(payment) );
    }



}
