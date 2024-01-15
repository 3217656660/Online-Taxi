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
public class User implements Serializable {

    private Integer id;

    private String username;

    private String password;

    private String mobile;

    private String email;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Integer isDeleted;
}
