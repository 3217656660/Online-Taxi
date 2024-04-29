package com.zxy.work.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Payment;
import com.zxy.work.service.PaymentService;
import com.zxy.work.service.impl.AlipayService;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Value("${alipay.alipayPublicKey}")
    private String publicKey;

    @Resource
    private AlipayService alipayService;

    @Resource
    private CacheUtil redisUtil;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

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
    public ApiResponse<String> deletePayment(@RequestParam("orderId") long orderId) throws MyException {
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
     * @param orderId  订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderId")
    public ApiResponse<Object> getPaymentByOrderId(@RequestParam("orderId")long orderId) throws MyException {
        log.info("通过订单id获取支付服务提供者：" + orderId);
        Payment payment = paymentService.selectByOrderId(orderId);
        return payment != null
                ? ApiResponse.success(payment)
                : ApiResponse.error(600, "支付查询失败");
    }

    /**
     * 用户去支付
     * @param orderId 订单id
     * @return 支付结果，成功返回支付的html信息
     */
    @GetMapping("/doPay")
    public ApiResponse<String> doPay(@RequestParam("id") Long orderId){
        Object result = redisUtil.get("payment:order:" + orderId);
        Payment payment;
        if (result == null)
            payment = paymentService.selectByOrderId(orderId);
        else
            payment = (Payment) result;
        redisUtil.set("payment:order:" + orderId, payment, 30*10);
        BigDecimal bigDecimal = BigDecimal.valueOf(payment.getAmount());
        String order = alipayService.createOrder(
                String.valueOf(orderId),
                bigDecimal,
                "打车费用支付",
                "打车费用支付"
        );
        return order != null
                ? ApiResponse.success(order)
                : ApiResponse.error(600, "支付失败,请稍后重试");
    }

    /**
     * 支付宝同步回调接口
     * @return 返回支付结果
     */
    @GetMapping("/return")
    //@ResponseBody
    public ApiResponse<String> handleReturn(
            @RequestParam("out_trade_no") String out_trade_no,
            @RequestParam("total_amount") String total_amount,
            @RequestParam("trade_no") String trade_no,
            @RequestParam("sign") String sign,
            @RequestParam("sign_type") String sign_type,
            @RequestParam("charset")String charset,
            @RequestParam("method")String method,
            @RequestParam("auth_app_id")String auth_app_id,
            @RequestParam("version")String version,
            @RequestParam("app_id") String app_id,
            @RequestParam("seller_id") String seller_id,
            @RequestParam("timestamp") String timestamp
    ) throws AlipayApiException {
        Map<String, String> params = new HashMap<>();
        params.put("sign", sign);
        params.put("out_trade_no", out_trade_no);
        params.put("total_amount", total_amount);
        params.put("trade_no", trade_no);
        params.put("sign_type", sign_type);
        params.put("charset", charset);
        params.put("method", method);
        params.put("auth_app_id", auth_app_id);
        params.put("version", version);
        params.put("app_id", app_id);
        params.put("seller_id",seller_id);
        params.put("timestamp", timestamp);
        //验证支付宝的签名，确保通知的真实性
        boolean isValid = AlipaySignature.verifyV1(params, publicKey, "UTF-8", "RSA2");
        if (!isValid)
            return ApiResponse.error(600, "支付失败或交易关闭");

        //支付成功，执行相关操作，例如更新订单状态
        long orderId = Long.parseLong(out_trade_no);
        Object result = redisUtil.get("payment:order:" + orderId);
        Payment payment;
        if (result == null)
            payment = paymentService.selectByOrderId(orderId);
        else
            payment = (Payment) result;
        int update = paymentService.update(payment.setPaymentMethod("支付宝支付"));
        if (update == 0)
            return ApiResponse.error(600, "支付失败或当前订单已支付");
        kafkaTemplate.send("main", new Random().nextInt(3), "paySuccess", out_trade_no);
        return ApiResponse.success("支付成功 order: " + orderId);
    }

    /**
     * 支付宝异步回调接口
     * @param params 参数
     * @return 支付
     * @throws AlipayApiException 支付接口异常
     */
    @PostMapping("/notify")
    public ApiResponse<String> handleNotify(@RequestBody Map<String, String> params) throws AlipayApiException {
        // 验证支付宝的签名，确保通知的真实性
        boolean isValid = AlipaySignature.verifyV1(params, publicKey, "UTF-8", "RSA2");

        if (isValid) {
            // 获取相关参数
            String outTradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));
            log.info("trade_no={},tradeStatus={},totalAmount={}", outTradeNo, tradeStatus, totalAmount);
            // 根据支付状态处理订单
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 支付成功，更新订单状态
                return ApiResponse.success("Payment notification received for order: " + outTradeNo);
            } else {
                // 支付失败或交易关闭
                return ApiResponse.error(600, "Payment notification failed or closed for order: " + outTradeNo);
            }
        } else {
            // 签名验证失败，拒绝处理
            return ApiResponse.error(600, "Invalid payment notification.");
        }
    }

}
