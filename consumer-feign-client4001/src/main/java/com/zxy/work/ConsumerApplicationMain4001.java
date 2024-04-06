package com.zxy.work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 服务消费者4001
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableTransactionManagement
public class ConsumerApplicationMain4001 {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplicationMain4001.class,args);
    }
}
