package com.platform.modules.quartz.service.impl;

import com.platform.common.web.service.impl.BaseServiceImpl;
import com.platform.modules.push.service.PushService;
import com.platform.modules.quartz.domain.QuartzJob;
import com.platform.modules.quartz.service.MessageTaskJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("messageTaskJob")
@Slf4j
public class MessageTaskJobImpl extends BaseServiceImpl<QuartzJob> implements MessageTaskJob {

    @Resource
    private PushService pushService;

    /**
     * 发送消息的方法
     * @param receiveList
     */
    public void sendMessage(String receiveList) {
        log.info("【延迟任务执行】向 {} 发送消息", receiveList);
        // 实际发送逻辑（如调用消息接口）
        pushService.pushStoredMomentMsg(receiveList,"0",1000);
    }
}
