package com.zxy.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AlipayService {

    @Value("${alipay.appId}")
    private String appId;

    @Value("${alipay.appPrivateKey}")
    private String privateKey;

    @Value("${alipay.alipayPublicKey}")
    private String publicKey;

    @Value("${alipay.gatewayUrl}")
    private String gatewayUrl;

    public String createOrder(String orderId, BigDecimal amount, String subject, String body) {
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, appId, privateKey, "json", "UTF-8", publicKey, "RSA2");
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl("http://localhost/taxi/payment/return");
        request.setNotifyUrl("http://localhost/taxi/payment/notify");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", orderId);
        jsonObject.put("total_amount", amount.setScale(2, BigDecimal.ROUND_HALF_UP));
        jsonObject.put("subject", subject);
        jsonObject.put("body", body);
        jsonObject.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(jsonObject.toString());

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                return response.getBody();
            } else {
                return null;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
    }
}
