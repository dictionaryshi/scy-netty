package com.scy.netty.job;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author : shichunyang
 * Date    : 2022/5/3
 * Time    : 9:42 下午
 * ---------------------------------------
 * Desc    : JobTypeEnum
 */
@Getter
@AllArgsConstructor
public enum JobTypeEnum {

    /**
     * spring bean模式
     */
    BEAN(1),
    ;

    private final int type;

    public static JobTypeEnum getByType(int type) {
        return Stream.of(JobTypeEnum.values())
                .filter(jobTypeEnum -> Objects.equals(jobTypeEnum.getType(), type))
                .findFirst().orElse(null);
    }
}
