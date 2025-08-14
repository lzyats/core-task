package com.platform.modules.quartz.service;

import com.platform.common.web.service.BaseService;
import com.platform.modules.push.dto.PushFrom;
import com.platform.modules.push.dto.PushMoment;
import com.platform.modules.push.enums.PushMomentEnum;
import com.platform.modules.quartz.domain.QuartzJob;

import java.util.List;

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

    /**
     * 一次性延时任务
     */
    void addDelayedJob(String jobName, String invokeTarget, int delaySeconds);

    void triggerDelayedMessage(PushFrom pushFrom, PushMoment pushMoment, List<Long> receiveList, PushMomentEnum msgType, int delaySeconds);

}