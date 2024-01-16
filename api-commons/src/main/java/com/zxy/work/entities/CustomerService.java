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
public class CustomerService implements Serializable {

    private Integer id;

    private String name;

    private String mobile;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;
}
