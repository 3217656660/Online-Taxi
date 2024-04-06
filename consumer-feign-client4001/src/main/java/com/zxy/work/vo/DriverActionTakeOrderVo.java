package com.zxy.work.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
/**
 * 司机开始接单请求传入的json集
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DriverActionTakeOrderVo implements Serializable {
    private Integer id;
    private Integer userId;
    private Integer driverId;
    private Double nowAddressLongitude;//司机地点经度
    private Double nowAddressLatitude;//司机地点纬度
}
