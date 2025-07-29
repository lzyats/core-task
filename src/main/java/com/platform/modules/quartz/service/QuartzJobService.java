package com.platform.modules.quartz.service;

import com.platform.common.web.service.BaseService;
import com.platform.modules.quartz.domain.QuartzJob;

/**
 * 定时任务调度信息信息 服务层
 */
public interface QuartzJobService extends BaseService<QuartzJob> {

    /**
     * 暂停任务
     */
    Integer pauseJob(Long jobId);

    /**
     * 恢复任务
     */
    Integer resumeJob(Long jobId);

    /**
     * 删除任务后
     */
    Integer deleteJob(Long jobId);

    /**
     * 立即运行任务
     */
    void run(Long jobId);

    /**
     * 一次性任务
     */
    void once(String jobName, String invokeTarget);

    /**
     * 新增任务
     */
    Integer addJob(QuartzJob quartzJob);

    /**
     * 更新任务
     */
    Integer updateJob(QuartzJob quartzJob);

}