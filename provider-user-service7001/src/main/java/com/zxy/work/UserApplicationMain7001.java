package com.zxy.work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserApplicationMain7001 {
    public static void main(String[] args) {
        SpringApplication.run(UserApplicationMain7001.class,args);
    }
}
