package com.platform.modules.quartz.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.web.domain.BaseEntity;
import com.platform.common.web.domain.JsonDateDeserializer;
import com.platform.modules.quartz.utils.CronUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 定时任务调度表 quartz_job
 */
@Data
@Accessors(chain = true) // 链式调用
@TableName("quartz_job")
public class QuartzJob extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId
    private Long jobId;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 64, message = "任务名称不能超过64个字符")
    private String jobName;

    /**
     * 调用目标字符串
     */
    @NotBlank(message = "调用目标字符串不能为空")
    @Size(max = 1000, message = "调用目标字符串长度不能超过500个字符")
    private String invokeTarget;

    /**
     * cron执行表达式
     */
    @NotBlank(message = "Cron执行表达式不能为空")
    @Size(max = 255, message = "Cron执行表达式不能超过255个字符")
    private String cronExpression;

    /**
     * 任务状态（0正常 1暂停）
     */
    private YesOrNoEnum status;

    @JsonDeserialize(using = JsonDateDeserializer.class)
    public Date getNextValidTime() {
        if (StringUtils.isEmpty(cronExpression)) {
            return null;
        }
        return CronUtils.getNextExecution(cronExpression);
    }

}