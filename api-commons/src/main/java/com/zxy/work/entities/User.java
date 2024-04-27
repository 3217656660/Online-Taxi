package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class User implements Serializable {

    private long id;

    @Size(min = 6, max = 10, message = "用户名长度应在6-10位")
    private String username;

    @Size(min = 6, max = 10, message = "密码长度应在6-10位")
    private String password;

    @Pattern(regexp = "^1[3456789]\\d{9}$", message = "手机号格式错误")
    private String mobile;

    @Email(message = "邮箱格式错误")
    private String email;

    private Date createTime;

    private Date updateTime;

    private long version;

    private Integer isDeleted;

}
