package com.scy.netty.job;

import com.scy.core.format.NumberUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author : shichunyang
 * Date    : 2022/4/28
 * Time    : 11:45 上午
 * ---------------------------------------
 * Desc    : Job
 */
@Slf4j
@Getter
@Setter
@ToString
public class Job implements Runnable {

    private int jobId;

    private JobHandler handler;

    private LinkedBlockingQueue<JobParam> triggerQueue;

    private Set<Long> triggerLogIdSet;

    private volatile boolean toStop = Boolean.FALSE;

    private String stopReason;

    private volatile boolean running = Boolean.FALSE;

    private volatile int idleTimes = NumberUtil.ZERO.intValue();

    public Job(int jobId, JobHandler handler) {
        this.jobId = jobId;

        this.handler = handler;

        this.triggerQueue = new LinkedBlockingQueue<>();

        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public void run() {
        Thread.currentThread().setName("JobThread-" + jobId + "-" + System.currentTimeMillis());
    }
}
