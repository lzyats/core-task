package com.platform.modules.quartz.constant;

/**
 * 任务调度通用常量
 */
public interface ScheduleConstants {

    /**
     * 任务名称
     */
    String TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /**
     * 执行分组
     */
    String TASK_GROUP_NAME = "DEFAULT";

    /**
     * 执行目标key
     */
    String TASK_PROPERTIES = "TASK_PROPERTIES";

    /**
     * 一次性表达式
     */
    String TASK_CRON = "0 0 0 1 1 ? 2099";

}
