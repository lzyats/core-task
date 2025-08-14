package com.platform.modules.quartz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.web.service.impl.BaseServiceImpl;
import com.platform.modules.push.dto.PushFrom;
import com.platform.modules.push.dto.PushMoment;
import com.platform.modules.push.enums.PushMomentEnum;
import com.platform.modules.quartz.constant.ScheduleConstants;
import com.platform.modules.quartz.dao.QuartzJobDao;
import com.platform.modules.quartz.domain.QuartzJob;
import com.platform.modules.quartz.service.QuartzJobService;
import com.platform.modules.quartz.utils.ScheduleUtils;
import lombok.SneakyThrows;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import org.quartz.*;
import org.quartz.SchedulerException;
import java.util.Date;  // 确保有这个导入用于处理日期

/**
 * 定时任务调度信息 服务层
 */
@Service("quartzJobService")
public class QuartzJobServiceImpl extends BaseServiceImpl<QuartzJob> implements QuartzJobService {

    @Resource
    private Scheduler scheduler;


    @Resource
    private QuartzJobDao jobDao;

    @Autowired
    public void setBaseDao() {
        super.setBaseDao(jobDao);
    }

    @Override
    public List<QuartzJob> queryList(QuartzJob quartzJob) {
        return jobDao.queryList(quartzJob);
    }

    /**
     * 暂停任务
     */
    @SneakyThrows
    @Override
    @Transactional
    public Integer pauseJob(Long jobId) {
        QuartzJob quartzJob = new QuartzJob()
                .setJobId(jobId)
                .setStatus(YesOrNoEnum.NO);
        Integer result = this.updateById(quartzJob);
        if (result > 0) {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId));
        }
        return result;
    }

    /**
     * 恢复任务
     */
    @SneakyThrows
    @Override
    @Transactional
    public Integer resumeJob(Long jobId) {
        QuartzJob quartzJob = new QuartzJob()
                .setJobId(jobId)
                .setStatus(YesOrNoEnum.YES);
        Integer result = this.updateById(quartzJob);
        if (result > 0) {
            scheduler.resumeJob(ScheduleUtils.getJobKey(quartzJob.getJobId()));
        }
        return result;
    }

    /**
     * 删除任务后，所对应的trigger也将被删除
     */
    @SneakyThrows
    @Override
    @Transactional
    public Integer deleteJob(Long jobId) {
        // 删除
        Integer result = this.deleteById(jobId);
        // 删除
        scheduler.deleteJob(ScheduleUtils.getJobKey(jobId));
        return result;
    }

    /**
     * 任务调度状态修改
     */
    @SneakyThrows
    @Override
    @Transactional
    public void update(QuartzJob quartzJob, String... param) {
        if (YesOrNoEnum.YES.equals(quartzJob.getStatus())) {
            resumeJob(quartzJob.getJobId());
        } else if (YesOrNoEnum.NO.equals(quartzJob.getStatus())) {
            pauseJob(quartzJob.getJobId());
        }
    }

    /**
     * 立即运行任务
     */
    @SneakyThrows
    @Override
    @Transactional
    public void run(Long jobId) {
        QuartzJob quartzJob = this.getById(jobId);
        if (quartzJob == null) {
            return;
        }
        scheduler.triggerJob(ScheduleUtils.getJobKey(quartzJob.getJobId()));
    }

    @SneakyThrows
    @Override
    @Transactional
    public void once(String jobName, String invokeTarget) {
        Long jobId = IdWorker.getId();
        QuartzJob quartzJob = new QuartzJob()
                .setJobId(jobId)
                .setJobName(jobName)
                .setInvokeTarget(invokeTarget)
                .setCronExpression(ScheduleConstants.TASK_CRON)
                .setStatus(YesOrNoEnum.YES);
        ScheduleUtils.createScheduleJob(scheduler, quartzJob);
        scheduler.triggerJob(ScheduleUtils.getJobKey(quartzJob.getJobId()));
    }

    /**
     * 新增任务
     */
    @SneakyThrows
    @Override
    @Transactional
    public Integer addJob(QuartzJob quartzJob) {
        Integer result = add(quartzJob);
        if (result > 0) {
            ScheduleUtils.createScheduleJob(scheduler, quartzJob);
        }
        return result;
    }

    /**
     * 更新任务的时间表达式
     */
    @SneakyThrows
    @Override
    @Transactional
    public Integer updateJob(QuartzJob quartzJob) {
        Integer result = jobDao.updateById(quartzJob);
        if (result > 0) {
            updateScheduler(quartzJob.getJobId());
        }
        return result;
    }

    /**
     * 更新任务
     */
    @SneakyThrows
    private void updateScheduler(Long jobId) {
        // 判断是否存在
        JobKey jobKey = ScheduleUtils.getJobKey(jobId);
        if (scheduler.checkExists(jobKey)) {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(jobKey);
        }
        QuartzJob quartzJob = getById(jobId);
        ScheduleUtils.createScheduleJob(scheduler, quartzJob);
    }

    /**
     * 添加延迟执行的一次性任务（复用现有方法实现）
     * @param jobName 任务名称
     * @param invokeTarget 调用目标（beanName.methodName(参数)）
     * @param delaySeconds 延迟秒数
     */
    @Override
    @SneakyThrows
    @Transactional
    public void addDelayedJob(String jobName, String invokeTarget, int delaySeconds) {
        // 1. 生成唯一jobId（复用once方法的ID生成逻辑）
        Long jobId = IdWorker.getId();

        // 2. 构造任务对象（复用addJob方法的入参格式）
        QuartzJob quartzJob = new QuartzJob()
                .setJobId(jobId)
                .setJobName(jobName)
                .setInvokeTarget(invokeTarget)
                // 临时使用一个无效的cron表达式（后续会被替换为延迟触发器）
                .setCronExpression(ScheduleConstants.TASK_CRON)
                .setStatus(YesOrNoEnum.YES);

        // 3. 复用现有addJob方法添加任务到数据库并创建基础调度
        this.addJob(quartzJob);

        // 4. 替换触发器为延迟执行的SimpleTrigger（核心延迟逻辑）
        // 4.1 构建延迟触发器
        Trigger delayTrigger = TriggerBuilder.newTrigger()
                .withIdentity(ScheduleUtils.getTriggerKey(jobId)) // 复用工具类的触发器键
                .startAt(new Date(System.currentTimeMillis() + delaySeconds * 1000L)) // 延迟时间
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)) // 只执行一次
                .build();

        // 4.2 替换原有触发器（复用updateScheduler方法的替换逻辑）
        scheduler.rescheduleJob(
                ScheduleUtils.getTriggerKey(jobId),
                delayTrigger
        );
    }

    /**
     * 触发一个5秒后发送的消息
     * @param pushFrom
     * @param pushMoment
     * @param receiveList
     * @param msgType
     * @param delaySeconds
     */
    @Override
    @SneakyThrows
    @Transactional
    public void triggerDelayedMessage(PushFrom pushFrom, PushMoment pushMoment, List<Long> receiveList, PushMomentEnum msgType, int delaySeconds) {
        try {
            // 1. 定义任务名称（建议包含业务标识，便于日志追踪）
            String jobName = "delayed_msg_" + System.currentTimeMillis(); // 用时间戳确保唯一

            // 2. 定义调用目标：格式为 "bean名称.方法名(参数)"
            // 注意：字符串参数必须用单引号包裹，多个参数用逗号分隔
            String invokeTarget = String.format(
                    "pushService.pushMomentSync('%s', '%s', '%s', '%s')",
                    pushFrom,  // 第一个参数：消息内容
                    pushMoment,  // 第二个参数：接收者
                    receiveList,
                    msgType

            );
            // 3. 调用延迟任务方法：5秒后执行
            this.addDelayedJob(jobName, invokeTarget, delaySeconds);
            System.out.println("延迟消息任务已创建，5秒后发送");
        } catch (Exception e) {
            // 处理任务创建失败的情况
            e.printStackTrace();
        }
    }

}