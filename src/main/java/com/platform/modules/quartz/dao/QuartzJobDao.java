package com.platform.modules.quartz.dao;

import com.platform.modules.quartz.domain.QuartzJob;
import com.platform.common.web.dao.BaseDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 调度任务信息 数据层
 */
@Repository
public interface QuartzJobDao extends BaseDao<QuartzJob> {

    /**
     * 查询调度任务日志集合
     */
    List<QuartzJob> queryList(QuartzJob quartzJob);

}
