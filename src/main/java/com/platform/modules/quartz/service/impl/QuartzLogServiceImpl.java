package com.platform.modules.quartz.service.impl;

import com.platform.common.web.service.impl.BaseServiceImpl;
import com.platform.modules.quartz.dao.QuartzLogDao;
import com.platform.modules.quartz.domain.QuartzLog;
import com.platform.modules.quartz.service.QuartzLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时任务调度日志信息 服务层
 */
@Service("quartzLogService")
public class QuartzLogServiceImpl extends BaseServiceImpl<QuartzLog> implements QuartzLogService {

    @Resource
    private QuartzLogDao quartzLogDao;

    @Autowired
    public void setBaseDao() {
        super.setBaseDao(quartzLogDao);
    }

    @Override
    public List<QuartzLog> queryList(QuartzLog quartzLog) {
        return quartzLogDao.queryList(quartzLog);
    }

}
