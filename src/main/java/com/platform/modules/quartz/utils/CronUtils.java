package com.platform.modules.quartz.utils;

import cn.hutool.core.date.DateUtil;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * cron表达式工具类
 */
public class CronUtils {

    /**
     * 返回一个布尔值代表一个给定的Cron表达式的有效性
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 返回一个字符串值,表示该消息无效Cron表达式给出有效性
     */
    public static String getInvalid(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (ParseException pe) {
            return pe.getMessage();
        }
    }

    /**
     * 返回下一个执行时间根据给定的Cron表达式
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(DateUtil.date());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
