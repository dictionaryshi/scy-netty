package com.scy.netty.mq;

import com.scy.core.format.MessageUtil;
import com.scy.core.net.NetworkInterfaceUtil;
import com.scy.core.rest.ResponseResult;
import com.scy.netty.rpc.consumer.RpcReference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author : shichunyang
 * Date    : 2022/7/27
 * Time    : 5:30 下午
 * ---------------------------------------
 * Desc    : MqService
 */
public class MqService {

    @RpcReference(version = "1.0", timeout = 6000)
    private MqMessageService mqMessageService;

    public Long send(MqMessage mqMessage) {
        mqMessage.setLog(MessageUtil.format("message product", "ip", NetworkInterfaceUtil.getIp()));
        Future<ResponseResult<Long>> responseResultFuture = mqMessageService.push(mqMessage).getResponseResultFuture();
        try {
            return responseResultFuture.get(5000, TimeUnit.MILLISECONDS).getData();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<MqMessage> pull(String topic, String group, int consumerRank, int consumerTotal) {
        Future<ResponseResult<List<MqMessage>>> responseResultFuture = mqMessageService.pull(topic, group, consumerRank, consumerTotal).getResponseResultFuture();
        try {
            return responseResultFuture.get(5000, TimeUnit.MILLISECONDS).getData();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
