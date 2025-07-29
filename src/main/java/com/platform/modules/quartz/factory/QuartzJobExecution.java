package com.platform.modules.quartz.factory;

import com.platform.modules.quartz.utils.JobInvokeUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（不允许并发执行）
 */
@DisallowConcurrentExecution
public class QuartzJobExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, String invokeTarget) throws Exception {
        JobInvokeUtils.invokeMethod(invokeTarget);
    }
}
