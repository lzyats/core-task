package com.platform.modules.quartz.utils;

import com.platform.common.enums.YesOrNoEnum;
import com.platform.modules.quartz.constant.ScheduleConstants;
import com.platform.modules.quartz.domain.QuartzJob;
import com.platform.modules.quartz.factory.QuartzJobExecution;
import org.quartz.*;

/**
 * 定时任务工具类
 */
public class ScheduleUtils {

    /**
     * 构建任务触发对象
     */
    public static TriggerKey getTriggerKey(Long jobId) {
        return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + jobId, ScheduleConstants.TASK_GROUP_NAME);
    }

    /**
     * 构建任务键对象
     */
    public static JobKey getJobKey(Long jobId) {
        return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + jobId, ScheduleConstants.TASK_GROUP_NAME);
    }

    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, QuartzJob quartzJob) throws SchedulerException {
        // 验证
        if (quartzJob == null) {
            return;
        }
        // 构建job信息
        Long jobId = quartzJob.getJobId();
        JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecution.class).withIdentity(getJobKey(jobId)).build();
        // 表达式调度构建器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(quartzJob.getCronExpression());
        // 不触发立即执行
        cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
        // 按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(jobId))
                .withSchedule(cronScheduleBuilder).build();
        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, quartzJob);
        // 判断是否存在
        if (scheduler.checkExists(getJobKey(jobId))) {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(getJobKey(jobId));
        }
        scheduler.scheduleJob(jobDetail, trigger);
        // 暂停任务
        if (YesOrNoEnum.NO.equals(quartzJob.getStatus())) {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId));
        }
    }

}