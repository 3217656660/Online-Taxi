package com.zxy.work.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;


/**
 * 全局过滤器：实现对除了注册、登录功能的其他功能访问的限制。
 * 只有请求头中带有token才可以访问,是第一层拦截，用于屏蔽无效请求。
 * 第二层拦截的检验用对应业务的注解实现。
 */
@Component
@Slf4j
@Order(1)
public class TokenGatewayFilter implements GlobalFilter{

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.实现对除了注册、登录功能的其他功能访问的限制，只有请求头带有有效的token的请求才放行
        String path = exchange.getRequest().getPath().value();
        String token = exchange.getRequest().getHeaders().getFirst("X-Token");

        //排除注册、登录、退出登录的路径或者token有效放行
        if ( path.endsWith("/login") ||  path.endsWith("/register")  || !Objects.equals(token, "") ){
            log.info( "放行了不需要token的请求，或放行了一条token:" + token );
            return chain.filter(exchange);
        }

        //否则，设置无权限响应401，并拦截下该请求
        log.info("拦截了一条没有token的非法请求");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);//"Unauthorized"
        return  exchange.getResponse().setComplete();
    }

}
