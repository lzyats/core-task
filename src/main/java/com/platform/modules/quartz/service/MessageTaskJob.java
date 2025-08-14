package com.platform.modules.quartz.service;

import com.platform.common.web.service.BaseService;
import com.platform.modules.quartz.domain.QuartzJob;

public interface MessageTaskJob extends BaseService<QuartzJob> {


    void sendMessage(String receiveList);
}