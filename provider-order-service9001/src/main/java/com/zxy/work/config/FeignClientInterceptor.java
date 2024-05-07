package com.zxy.work.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * openfeign请求拦截器
 */
public class FeignClientInterceptor implements RequestInterceptor {

    /**
     * 处理请求头token丢失问题
     */
    @Override
    public void apply(RequestTemplate template) {
        // 获取前一个请求的HttpServletRequest对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 从前一个请求的头部获取您想要的信息，例如token
            String token = request.getHeader("X-Token");
            // 将获取的信息设置到新的请求头中
            template.header("X-Token", token);
        }
    }

}

