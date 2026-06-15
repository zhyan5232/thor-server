package com.thor.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("thor_node_group")
public class ThorNodeGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appSystemId;
    private String groupCode;
    private String groupName;
    private Integer status;
    private String description;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;
}