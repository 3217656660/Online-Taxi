package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Review {

    private Integer id;

    private Integer orderId;

    private Integer rating;//评分（1-5分）

    private String comment;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Integer isDeleted;
}
