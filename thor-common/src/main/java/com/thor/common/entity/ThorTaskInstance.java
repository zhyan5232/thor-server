package com.thor.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("thor_task_instance")
public class ThorTaskInstance {
    @TableId(type = IdType.INPUT)
    private String taskId;
    private Long cfgId;
    private String fileName;
    private Long totalSize;
    private String status;
    private Date startTime;
    private Date endTime;
    private String errorMsg;
}