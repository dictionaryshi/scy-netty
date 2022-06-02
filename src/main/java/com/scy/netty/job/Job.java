package com.scy.netty.job;

import com.scy.core.IOUtil;
import com.scy.core.StringUtil;
import com.scy.core.enums.JvmStatus;
import com.scy.core.enums.ResponseCodeEnum;
import com.scy.core.exception.ExceptionUtil;
import com.scy.core.format.DateUtil;
import com.scy.core.format.MessageUtil;
import com.scy.core.format.NumberUtil;
import com.scy.core.rest.ResponseResult;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.core.trace.TraceUtil;
import com.scy.netty.server.http.HttpServerHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

    private static final ThreadPoolExecutor TIME_OUT_THREAD_POOL = ThreadPoolUtil.getThreadPool("job-time-out", 10, 30, 1024);

    private static final ConcurrentHashMap<Integer, Thread> JOB_THREAD_MAP = new ConcurrentHashMap<>();

    public Job(int jobId, JobHandler handler) {
        this.jobId = jobId;

        this.handler = handler;

        this.triggerQueue = new LinkedBlockingQueue<>();

        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public void run() {
        Thread oldJobThread = JOB_THREAD_MAP.put(jobId, Thread.currentThread());
        if (Objects.nonNull(oldJobThread)) {
            oldJobThread.interrupt();
        }

        try {
            handler.init();
        } catch (Exception e) {
            log.error(MessageUtil.format("handler init error", e));
        }

        while (runSwitch()) {
            try {
                TraceUtil.setTraceId(null);
                runJob();
            } finally {
                TraceUtil.clearTrace();
            }
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
    }

    private void runJob() {
        running = Boolean.FALSE;
        idleTimes++;
        JobParam triggerParam = null;

        try {
            triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
            if (Objects.isNull(triggerParam)) {
                if (idleTimes > 60 && triggerQueue.isEmpty()) {
                    log.info(MessageUtil.format("executor idle times over limit", "jobId", jobId));
                    HttpServerHandler.removeJob(jobId, "executor idle times over limit");
                }
                return;
            }

            long startTime = System.currentTimeMillis();

            running = Boolean.TRUE;
            idleTimes = NumberUtil.ZERO.intValue();

            JobContext jobContext = new JobContext(triggerParam.getJobId(), triggerParam.getLogId(), triggerParam.getExecutorParams(),
                    triggerParam.getBroadcastIndex(), triggerParam.getBroadcastTotal());
            jobContext.setJobLogFileName(IOUtil.getFile(IOUtil.getUserDirectory(),
                    "job_log", DateUtil.date2Str(new Date(triggerParam.getLogDateTime()), DateUtil.PATTERN_DAY), String.valueOf(triggerParam.getLogId()).concat(".log")).getAbsolutePath());
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
                    }, TIME_OUT_THREAD_POOL);

                    completableFuture.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    log.info(MessageUtil.format("job timeout", "jobId", jobId, "logId", triggerParam.getLogId()));
                    JobContext.handleResult(JobContext.CODE_TIMEOUT, "job timeout");
                }
            } else {
                handler.execute();
            }

            log.info(MessageUtil.format("job end", "triggerParam", triggerParam, StringUtil.COST, System.currentTimeMillis() - startTime));
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

                triggerLogIdSet.remove(triggerParam.getLogId());
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
            return Boolean.FALSE;
        }

        if (JvmStatus.JVM_CLOSE_FLAG) {
            log.info(MessageUtil.format("jvm closing", "jobId", jobId));
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

        Thread jobThread = JOB_THREAD_MAP.get(jobId);
        if (Objects.nonNull(jobThread)) {
            jobThread.interrupt();
        }
    }

    public boolean isRunningOrHasQueue() {
        return running || triggerQueue.size() > 0;
    }
}
