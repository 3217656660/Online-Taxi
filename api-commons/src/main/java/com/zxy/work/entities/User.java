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
public class User implements Serializable {

    private Integer id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;


    public User(String mobile, String password) {
        this.mobile = mobile;
        this.password = password;

    }

}
