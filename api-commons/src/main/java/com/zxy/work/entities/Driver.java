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
public class Driver {

    private Integer id;

    private String name;

    private String mobile;

    private String carType;

    private String carNumber;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Integer isDeleted;
}
