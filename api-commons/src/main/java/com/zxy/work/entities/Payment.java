package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Payment implements Serializable {

    private long id;

    private long orderId;

    private long userId;

    private String paymentMethod;

    private Float amount;

    private Date createTime;

    private Date updateTime;

    private long version;

    private Integer isDeleted;

}

