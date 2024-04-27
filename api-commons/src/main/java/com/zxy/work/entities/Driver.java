package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Driver implements Serializable {

    private long id;

    //一个中文两位
    @Size(min = 2, max = 2*10, message = "名字应该在1-10位")
    private String username;

    @Pattern(regexp = "^1[3456789]\\d{9}$", message = "手机号格式错误")
    private String mobile;

    private String password;

    private String email;

    private String carType;

    private String carNumber;

    private Date createTime;

    private Date updateTime;

    private long version;

    private Integer isDeleted;
}
