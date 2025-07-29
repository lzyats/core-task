package com.platform.modules.quartz.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.web.domain.BaseEntity;
import com.platform.common.web.domain.JsonDateDeserializer;
import lombok.Data;

import java.util.Date;

/**
 * 定时任务调度日志表 quartz_log
 */
@Data
@TableName("quartz_log")
public class QuartzLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long logId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 调用目标字符串
     */
    private String invokeTarget;

    /**
     * 日志信息
     */
    private String message;

    /**
     * 执行状态（Y正常 N失败）
     */
    private YesOrNoEnum status;

    /**
     * 执行时间
     */
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Date createTime;

}
