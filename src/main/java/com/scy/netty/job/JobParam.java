package com.scy.netty.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author : shichunyang
 * Date    : 2022/4/28
 * Time    : 11:16 上午
 * ---------------------------------------
 * Desc    : JobParam
 */
@Getter
@Setter
@ToString
public class JobParam {

    private int jobId;

    private int jobType;

    private String executorHandler;

    private String executorParams;

    private int executorBlockStrategy;

    private int executorTimeout;

    private long logId;

    private long logDateTime;

    private int broadcastIndex;

    private int broadcastTotal;
}
