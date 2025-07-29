package com.platform.modules.quartz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.platform.common.enums.YesOrNoEnum;
import com.platform.common.web.service.impl.BaseServiceImpl;
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
import java.util.List;

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
}