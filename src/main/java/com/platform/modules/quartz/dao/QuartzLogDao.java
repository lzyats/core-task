package com.platform.modules.quartz.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.platform.common.web.dao.BaseDao;
import com.platform.modules.quartz.domain.QuartzLog;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 删除日志
     */
    int dellogs(@Param(Constants.WRAPPER) QueryWrapper<QuartzLog> ew);

}
