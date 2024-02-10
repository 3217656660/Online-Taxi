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

    private Integer userId;

    private String startAddress;

    private Double startAddressLongitude;//开始地点经度

    private Double startAddressLatitude;//开始地点纬度

    private String endAddress;

    private Double distance;
}
