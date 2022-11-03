package com.scy.netty.mq;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : shichunyang
 * Date    : 2022/11/3
 * Time    : 4:01 下午
 * ---------------------------------------
 * Desc    : MessageStatusEnum
 */
@Getter
@AllArgsConstructor
public enum MessageStatusEnum {

    /**
     * 消息状态
     */
    NEW(1),
    RUNNING(2),
    SUCCESS(3),
    FAIL(4),
    ;

    private final int status;
}
