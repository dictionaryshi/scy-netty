package com.scy.netty.job;

import com.scy.core.enums.ResponseCodeEnum;
import com.scy.core.format.MessageUtil;
import com.scy.core.format.NumberUtil;
import com.scy.core.rest.ResponseResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
        String tmpThreadName = Thread.currentThread().getName();

        Thread.currentThread().setName("JobThread-" + jobId + "-" + System.currentTimeMillis());

        try {
            handler.init();
        } catch (Exception e) {
            log.error(MessageUtil.format("handler init error", e));
        }

        while (runSwitch()) {
            runJob();
        }

        while (triggerQueue.size() > 0) {
            JobParam triggerParam = triggerQueue.poll();
            if (Objects.nonNull(triggerParam)) {
                // TODO 回调通知
                long logId = triggerParam.getLogId();
                long logDateTime = triggerParam.getLogDateTime();
            }
        }

        try {
            handler.destroy();
        } catch (Throwable e) {
            log.error(MessageUtil.format("handler destroy error", e));
        }

        log.info(MessageUtil.format("job stopped", "thread", Thread.currentThread().getName()));

        Thread.currentThread().setName(tmpThreadName);
    }

    private void runJob() {
        running = Boolean.FALSE;

        idleTimes++;

        JobParam triggerParam = null;

        try {
            triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
            if (Objects.isNull(triggerParam) && idleTimes > 60 && triggerQueue.isEmpty()) {
                // TODO 销毁任务
                return;
            }
        } catch (Throwable throwable) {
            if (toStop) {
                log.error(MessageUtil.format("job killed", throwable, "stopReason", stopReason));
            } else {
                log.error(MessageUtil.format("job exception", throwable));
            }
        } finally {
            if (Objects.nonNull(triggerParam)) {
                // TODO 回调通知
                long logId = triggerParam.getLogId();
                long logDateTime = triggerParam.getLogDateTime();
                if (!toStop) {
                } else {
                }
            }
        }
    }

    public ResponseResult<Boolean> pushTriggerQueue(JobParam triggerParam) {
        // avoid repeat
        if (triggerLogIdSet.contains(triggerParam.getLogId())) {
            log.info(MessageUtil.format("repeate trigger job", "logId", triggerParam.getLogId()));
            return ResponseResult.error(ResponseCodeEnum.BUSINESS_EXCEPTION.getCode(),
                    MessageUtil.format("repeate trigger job", "logId", triggerParam.getLogId()), Boolean.FALSE);
        }

        triggerLogIdSet.add(triggerParam.getLogId());

        triggerQueue.add(triggerParam);

        return ResponseResult.success(Boolean.TRUE);
    }

    public boolean runSwitch() {
        if (toStop) {
            log.info(MessageUtil.format("job killed", "jobId", jobId));
            Thread.currentThread().interrupt();
            return Boolean.FALSE;
        }

        if (Thread.currentThread().isInterrupted()) {
            log.info(MessageUtil.format("job interrupted", "jobId", jobId));
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public void toStop(String stopReason) {
        this.toStop = Boolean.TRUE;

        this.stopReason = stopReason;
    }

    public boolean isRunningOrHasQueue() {
        return running || triggerQueue.size() > 0;
    }
}
