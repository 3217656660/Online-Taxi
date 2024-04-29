package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * websocket的通知消息类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class NotificationMessage implements Serializable {
    // 消息类型，如"orderAccept", "locationUpdate", "arrivalNotice", "orderFinish","cancelOrder","paymentNotice"
    private String type;
    // 消息内容
    private Object content;
    // 目标用户ID
    private Long userId;
}
