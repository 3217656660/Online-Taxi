package com.zxy.work.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 交流消息实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ChatMessageVo implements Serializable {
    private Long orderId;

    private Long userId;

    private Long driverId;

    private String sender;

    private String message;
}
