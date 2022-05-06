package com.scy.netty.job;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author : shichunyang
 * Date    : 2022/5/3
 * Time    : 9:52 下午
 * ---------------------------------------
 * Desc    : ExecutorBlockStrategyEnum
 */
@Getter
@AllArgsConstructor
public enum ExecutorBlockStrategyEnum {

    /**
     * 串行
     */
    SERIAL_EXECUTION(1),

    /**
     * 忽略最后的任务
     */
    DISCARD_LATER(2),

    /**
     * 覆盖已有任务
     */
    COVER_EARLY(3),
    ;

    private final int type;

    public static ExecutorBlockStrategyEnum getByType(int type) {
        return Stream.of(ExecutorBlockStrategyEnum.values())
                .filter(executorBlockStrategyEnum -> Objects.equals(executorBlockStrategyEnum.getType(), type))
                .findFirst().orElse(null);
    }
}
