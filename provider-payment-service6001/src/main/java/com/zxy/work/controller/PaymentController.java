package com.zxy.work.controller;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Payment;
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

    /**
     * 创建支付
     * @param payment 传来的支付json
     * @return  创建支付结果
     */
    @PostMapping("/update/create")
    public ApiResponse<String> createPayment(@RequestBody Payment payment) throws MyException {
        log.info("创建支付服务提供者：" + payment);
        return paymentService.create(payment) == 1
                ? ApiResponse.success("支付创建成功")
                : ApiResponse.error(600, "支付创建失败");
    }


    /**
     * 删除支付
     * @param orderId 传来的订单id
     * @return 支付删除结果
     */
    @DeleteMapping("/update/delete")
    public ApiResponse<String> deletePayment(@RequestParam("orderId") Integer orderId) throws MyException {
        log.info("删除支付服务提供者：" + orderId);
        return paymentService.delete(orderId) == 1
                ? ApiResponse.success("支付删除成功")
                : ApiResponse.error(600, "支付删除失败");
    }

    /**
     * 更新支付信息
     * @param payment 传来的支付json信息
     * @return  更新结果
     */
    @PutMapping("/update/message")
    public ApiResponse<String> update(@RequestBody Payment payment) throws MyException {
        log.info("更新支付服务提供者：" + payment);
        return paymentService.update(payment) == 1
                ? ApiResponse.success("支付更新成功")
                : ApiResponse.error(600, "支付更新失败");
    }

    /**
     * 通过订单id获取支付信息
     * @param orderId   订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderId")
    public ApiResponse<Object> getPaymentByOrderId(@RequestParam("orderId")Integer orderId) throws MyException {
        log.info("通过订单id获取支付服务提供者：" + orderId);
        Payment payment = paymentService.selectByOrderId(orderId);
        return payment != null
                ? ApiResponse.success(payment)
                : ApiResponse.error(600, "支付查询失败");
    }

}
