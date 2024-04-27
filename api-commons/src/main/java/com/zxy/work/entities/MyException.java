package com.zxy.work.entities;

/**
 * 自定义异常
 */
public class MyException extends RuntimeException{
    private final String errorMessage;

    public MyException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
