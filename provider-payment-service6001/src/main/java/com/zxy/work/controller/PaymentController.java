package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.Payment;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/update/create")
    public CommonResult createPayment(@RequestBody Payment payment){
        log.info( "********支付创建服务6001：*********" );

        //开始创建
        int result = paymentService.create( payment );

        if (result > 0){
            log.info( payment + "支付创建成功" );
            return new CommonResult<>( StatusCode.SUCCESS,"支付创建成功" );
        }

        log.info( payment + "支付创建失败" );
        return new CommonResult<>( StatusCode.FAILURE,"支付创建失败" );
    }


    @DeleteMapping("/update/delete")
    public CommonResult deletePayment(@RequestBody Payment payment){

        log.info( "********删除支付信息服务6001：*********" );

        int result = paymentService.delete(payment);

        if (result > 0){
            log.info( payment + "删除成功" );
            return new CommonResult<>( StatusCode.SUCCESS,payment.getId() );
        }

        log.info( payment + "删除失败" );
        return new CommonResult<>( StatusCode.FAILURE,"删除失败" );
    }


    @GetMapping("/get/{orderId}")
    public CommonResult getPaymentByOrderId(@PathVariable("orderId")Integer orderId){

        log.info( "********查询支付服务6001：*********" );

        Payment payment = paymentService.selectByOrderId(orderId);

        if (payment == null){
            log.info( "查找失败" );
            return new CommonResult<>(StatusCode.FAILURE,"查找失败");
        }

        log.info( payment + "查找成功" );
        return new CommonResult<>(StatusCode.SUCCESS,payment);
    }


}
