package com.scy.netty.job;

import com.scy.core.enums.ResponseCodeEnum;
import com.scy.core.exception.ExceptionUtil;
import com.scy.core.format.MessageUtil;
import com.scy.core.format.NumberUtil;
import com.scy.core.rest.ResponseResult;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.netty.server.http.HttpServerHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

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

    private int idleTimes = NumberUtil.ZERO.intValue();

    private ThreadPoolExecutor threadPoolExecutor;

    public Job(int jobId, JobHandler handler) {
        this.jobId = jobId;

        this.handler = handler;

        this.triggerQueue = new LinkedBlockingQueue<>();

        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<>());

        this.threadPoolExecutor = ThreadPoolUtil.getThreadPool("job-" + jobId + "-pool", 10, 30, 1024);
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

        Thread.currentThread().setName(tmpThreadName);
    }

    private void runJob() {
        running = Boolean.FALSE;
        idleTimes++;
        JobParam triggerParam = null;

        try {
            triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
            if (Objects.isNull(triggerParam)) {
                if (idleTimes > 60 && triggerQueue.isEmpty()) {
                    HttpServerHandler.removeJob(jobId, "executor idle times over limit");
                }
                return;
            }

            running = Boolean.TRUE;
            idleTimes = NumberUtil.ZERO.intValue();
            triggerLogIdSet.remove(triggerParam.getLogId());

            JobContext jobContext = new JobContext(triggerParam.getJobId(), triggerParam.getLogId(), triggerParam.getExecutorParams(),
                    triggerParam.getBroadcastIndex(), triggerParam.getBroadcastTotal());
            JobContext.setJobContext(jobContext);

            if (triggerParam.getExecutorTimeout() > 0) {
                try {
                    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
                        try {
                            handler.execute();
                        } catch (Exception exception) {
                            log.error(MessageUtil.format("job execute exception", exception));
                            JobContext.handleResult(JobContext.CODE_FAIL, ExceptionUtil.getExceptionMessage(exception));
                        }
                    }, threadPoolExecutor);

                    completableFuture.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    JobContext.handleResult(JobContext.CODE_TIMEOUT, "job timeout");
                }
            } else {
                handler.execute();
            }
        } catch (Throwable throwable) {
            if (toStop) {
                log.error(MessageUtil.format("job killed", throwable, "stopReason", stopReason));
            } else {
                log.error(MessageUtil.format("job exception", throwable));
            }

            JobContext.handleResult(JobContext.CODE_FAIL, ExceptionUtil.getExceptionMessage(throwable));
        } finally {
            if (Objects.nonNull(triggerParam)) {
                // TODO 回调通知
                long logId = triggerParam.getLogId();
                long logDateTime = triggerParam.getLogDateTime();
                if (!toStop) {
                } else {
                }
            }

            JobContext.clearJobContext();
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
