package com.zxy.work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DriverApplicationMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(DriverApplicationMain8001.class,args);
    }
}
