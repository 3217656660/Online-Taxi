package com.zxy.work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplicationMain9001 {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplicationMain9001.class,args);
    }
}
