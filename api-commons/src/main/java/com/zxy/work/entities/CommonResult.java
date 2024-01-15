package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@AllArgsConstructor
@Getter
public class CommonResult<T> implements Serializable {

    private StatusCode statusCode;
    private T data;

    public CommonResult(StatusCode statusCode){
        this.statusCode = statusCode;
        this.data = null;
    }

}
