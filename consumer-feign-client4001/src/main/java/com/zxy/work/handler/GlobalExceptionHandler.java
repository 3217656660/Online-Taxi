package com.zxy.work.handler;

import cn.dev33.satoken.exception.SaTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获类
 */
@RestControllerAdvice
@Slf4j
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
        log.info("SaTokenException" + e.getMessage());
        return new ResponseEntity<>(e.getMessage(), status);
    }


    /**
     * controller参数校验异常捕获
     * @param e 捕获的异常
     * @return  返回前端异常信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.info("MethodArgumentNotValidException" + message);
        return ResponseEntity.badRequest().body(message);
    }



}
