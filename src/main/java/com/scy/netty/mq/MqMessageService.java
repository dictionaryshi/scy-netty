package com.scy.netty.mq;

import com.scy.core.rest.ResponseResult;

import java.util.List;

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

    /**
     * 拉取消息
     */
    ResponseResult<List<MqMessage>> pull(String topic, String group, int consumerRank, int consumerTotal);

    ResponseResult<Integer> lockMessage(long id, String appendLog);

    ResponseResult<Integer> callbackMessage(long id, int status, String appendLog);
}
