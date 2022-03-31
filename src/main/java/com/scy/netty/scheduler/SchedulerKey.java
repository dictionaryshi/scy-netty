package com.scy.netty.scheduler;

import lombok.Data;

/**
 * @author : shichunyang
 * Date    : 2022/3/31
 * Time    : 7:55 下午
 * ---------------------------------------
 * Desc    : SchedulerKey
 */
@Data
public class SchedulerKey {

    public enum Type {

        /**
         * ping 超时
         */
        PING_TIMEOUT,
        ;
    }

    private final Type type;

    private final Object key;

    public SchedulerKey(Type type, Object key) {
        this.type = type;
        this.key = key;
    }
}
