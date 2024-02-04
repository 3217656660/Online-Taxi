package com.zxy.work.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Date;
/**
 * 用户创建订单请求json集
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserCreateOrderVo {

    private Integer id;

    @NotNull
    private Integer userId;

    private Integer driverId;

    private String startAddress;

    private Double startAddressLongitude;//开始地点经度

    private Double startAddressLatitude;//开始地点纬度

    private String endAddress;

    private Double endAddressLongitude;//结束地点经度

    private Double endAddressLatitude;//结束地点纬度

    private Integer status;//订单状态（0-待接单；1-待出发；2-行驶中；3-待支付；4-已完成；5-已取消）

    private Float price;

    private Double distance;

    private Date endTime;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;

}
