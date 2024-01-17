package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public enum StatusCode implements Serializable {
    SUCCESS(200,"Success"),
    FAILURE(600,"Failure"),
    NOTFOUND(404,"Not Found");

    private final int code;
    private final String message;

    /**
     * 构造函数，用于构造与数据库交互失败的返回信息
     * @param code 编码
     */
    StatusCode(int code) {
        this.code = code;
        this.message = null;
    }


}
