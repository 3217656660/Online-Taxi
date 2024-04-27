package com.zxy.work.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Review implements Serializable {

    private long id;

    private long orderId;

    private long userId;

    @Min(value = 1, message = "最低1分")
    @Max(value = 5, message = "最高5分")
    private Integer rating;//评分（1-5分）

    @Size(max = 2*50, message = "评论长度应在50字以内")
    private String comment;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;
}
