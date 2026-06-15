package com.thor.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("thor_app_system")
public class ThorAppSystem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appCode;
    private String appName;
    private Integer status;
    private String description;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;
}