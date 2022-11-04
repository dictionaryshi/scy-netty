package com.scy.netty.mq;

/**
 * @author : shichunyang
 * Date    : 2022/11/4
 * Time    : 2:53 下午
 * ---------------------------------------
 * Desc    : MqConsumer
 */
public interface MqConsumer {

    MqResult consume(String data);
}
