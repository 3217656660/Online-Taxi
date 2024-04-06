package com.zxy.work.entities;

public class MyException extends Exception{
    private final String errorMessage;

    public MyException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
