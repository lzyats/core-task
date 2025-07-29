package com.platform.modules.quartz.factory;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.modules.quartz.constant.ScheduleConstants;
import com.platform.modules.quartz.domain.QuartzJob;
import com.platform.modules.quartz.domain.QuartzLog;
import com.platform.modules.quartz.service.QuartzJobService;
import com.platform.modules.quartz.service.QuartzLogService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.BeanUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * 抽象quartz调用
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job {

    /**
     * 线程本地变量
     */
    private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context) {
        QuartzJob quartzJob = new QuartzJob();
        Object object = context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES);
        BeanUtils.copyProperties(object, quartzJob);
        try {
            before();
            if (quartzJob != null) {
                doExecute(context, quartzJob.getInvokeTarget());
            }
            after(quartzJob, null);
        } catch (Exception e) {
            log.error("任务执行异常  - ：", e);
            after(quartzJob, e);
        } finally {
            // 一次性定时任务
            if (ScheduleConstants.TASK_CRON.equals(quartzJob.getCronExpression())) {
                QuartzJobService quartzJobService = SpringUtil.getBean(QuartzJobService.class);
                quartzJobService.deleteJob(quartzJob.getJobId());
            }
        }
    }

    /**
     * 执行前
     */
    protected void before() {
        threadLocal.set(DateUtil.date());
    }

    /**
     * 执行后
     */
    protected void after(QuartzJob quartzJob, Exception e) {
        Date startTime = threadLocal.get();
        threadLocal.remove();
        final QuartzLog quartzLog = new QuartzLog();
        quartzLog.setJobName(quartzJob.getJobName());
        quartzLog.setInvokeTarget(quartzJob.getInvokeTarget());
        quartzLog.setCreateTime(startTime);
        long runMs = DateUtil.between(startTime, DateUtil.date(), DateUnit.MS);
        quartzLog.setMessage("总共耗时：" + runMs + "毫秒");
        if (e != null) {
            quartzLog.setStatus(YesOrNoEnum.NO);
            String errorMsg = StrUtil.sub(getExceptionMessage(e), 0, 2000);
            quartzLog.setMessage(errorMsg);
        } else {
            quartzLog.setStatus(YesOrNoEnum.YES);
        }
        // 写入数据库当中
        SpringUtil.getBean(QuartzLogService.class).add(quartzLog);
    }

    /**
     * 获取exception的详细错误信息。
     */
    private static String getExceptionMessage(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }

    /**
     * 执行方法，由子类重载
     */
    protected abstract void doExecute(JobExecutionContext context, String invokeTarget) throws Exception;
}
