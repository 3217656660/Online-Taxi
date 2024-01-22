package com.zxy.work.handler;

import cn.dev33.satoken.exception.SaTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * sa-token异常捕获
     * @param e 捕获的异常
     * @return  返回前端异常信息
     */
    @ExceptionHandler(value = SaTokenException.class)
    public ResponseEntity<String> handleTokenException(SaTokenException  e) {
        // 处理 TokenException 异常
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return new ResponseEntity<>(e.getMessage(), status);
    }




}
