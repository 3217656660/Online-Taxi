package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Order implements Serializable {

    private Integer id;

    private Integer userId;

    private Integer driverId;

    private String startAddress;

    private String endAddress;

    private Integer status;//订单状态（0-待接单；1-待出发；2-行驶中；3-待支付；4-已完成；5-已取消）

    private Float price;

    private Double distance;

    private LocalDate endTime;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Integer isDeleted;

}
