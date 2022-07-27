package com.scy.netty.mq;

import com.scy.core.rest.ResponseResult;

/**
 * @author : shichunyang
 * Date    : 2022/7/27
 * Time    : 2:42 下午
 * ---------------------------------------
 * Desc    : MqMessageService
 */
public interface MqMessageService {

    /**
     * 推送消息
     */
    ResponseResult<Long> push(MqMessage mqMessage);
}
