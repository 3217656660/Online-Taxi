package com.zxy.work.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * openfeign配置类
 */
@Configuration
public class FeignClientConfiguration {
    @Bean
    public FeignClientInterceptor FeignClientInterceptor() {
        return new FeignClientInterceptor();
    }
}
