package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Review implements Serializable {

    private Integer id;

    private Integer orderId;

    private Integer rating;//评分（1-5分）

    private String comment;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;
}
