package com.platform.modules.quartz.dao;

import com.platform.common.web.dao.BaseDao;
import com.platform.modules.quartz.domain.QuartzLog;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 调度任务日志信息 数据层
 */
@Repository
public interface QuartzLogDao extends BaseDao<QuartzLog> {

    /**
     * 获取quartz调度器日志的计划任务
     */
    List<QuartzLog> queryList(QuartzLog quartzLog);

}
