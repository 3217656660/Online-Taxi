package com.zxy.work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GateWayApplicationMain80 {
    public static void main(String[] args) {
        SpringApplication.run(GateWayApplicationMain80.class,args);
    }
}
