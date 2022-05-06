package com.scy.netty.job;

import com.scy.core.StringUtil;
import com.scy.core.format.MessageUtil;
import com.scy.core.rest.ResponseResult;
import com.scy.netty.server.http.HttpServerHandler;

import java.util.Objects;

/**
 * @author : shichunyang
 * Date    : 2022/5/1
 * Time    : 11:36 下午
 * ---------------------------------------
 * Desc    : Executor
 */
public class Executor {

    public static ResponseResult<Boolean> beat() {
        return ResponseResult.success(Boolean.TRUE);
    }

    public static ResponseResult<Boolean> idleBeat(JobParam jobParam) {
        Job job = HttpServerHandler.loadJob(jobParam.getJobId());
        if (Objects.nonNull(job) && job.isRunningOrHasQueue()) {
            return ResponseResult.error(JobContext.CODE_FAIL, "job is running or has trigger queue", Boolean.FALSE);
        }

        return ResponseResult.success(Boolean.TRUE);
    }

    public static ResponseResult<Boolean> kill(JobParam jobParam) {
        Job job = HttpServerHandler.loadJob(jobParam.getJobId());
        if (Objects.nonNull(job)) {
            HttpServerHandler.removeJob(jobParam.getJobId(), "scheduling center kill job");
            return ResponseResult.success(Boolean.TRUE);
        }

        return ResponseResult.error(JobContext.CODE_SUCCESS, "job thread already killed", Boolean.FALSE);
    }

    public static ResponseResult<Boolean> run(JobParam jobParam) {
        int jobType = jobParam.getJobType();
        JobTypeEnum jobTypeEnum = JobTypeEnum.getByType(jobType);
        if (Objects.isNull(jobTypeEnum)) {
            return ResponseResult.error(JobContext.CODE_FAIL, MessageUtil.format("job type is not valid", "jobType", jobType), Boolean.FALSE);
        }

        return bean(jobParam);
    }

    private static ResponseResult<Boolean> bean(JobParam jobParam) {
        JobHandler jobHandler = JobConfig.JOB_HANDLER_MAP.get(jobParam.getExecutorHandler());
        if (Objects.isNull(jobHandler)) {
            return ResponseResult.error(JobContext.CODE_FAIL,
                    MessageUtil.format("executorHandler not valid", "executorHandler", jobParam.getExecutorHandler()), Boolean.FALSE);
        }

        Job job = HttpServerHandler.loadJob(jobParam.getJobId());
        if (Objects.isNull(job)) {
            job = HttpServerHandler.registerJob(jobParam.getJobId(), jobHandler, StringUtil.EMPTY);
            return job.pushTriggerQueue(jobParam);
        }

        ExecutorBlockStrategyEnum executorBlockStrategyEnum = ExecutorBlockStrategyEnum.getByType(jobParam.getExecutorBlockStrategy());
        if (Objects.equals(ExecutorBlockStrategyEnum.DISCARD_LATER, executorBlockStrategyEnum) && job.isRunningOrHasQueue()) {
            return ResponseResult.error(JobContext.CODE_FAIL, ExecutorBlockStrategyEnum.DISCARD_LATER.name(), Boolean.FALSE);
        }

        String removeOldReason = getRemoveOldReason(jobParam, job, jobHandler, executorBlockStrategyEnum);
        if (!StringUtil.isEmpty(removeOldReason)) {
            job = HttpServerHandler.registerJob(jobParam.getJobId(), jobHandler, removeOldReason);
        }

        return job.pushTriggerQueue(jobParam);
    }

    private static String getRemoveOldReason(JobParam jobParam, Job job, JobHandler jobHandler, ExecutorBlockStrategyEnum executorBlockStrategyEnum) {
        if (!Objects.equals(job.getHandler(), jobHandler)) {
            return "change job handler";
        }

        if (Objects.equals(ExecutorBlockStrategyEnum.COVER_EARLY, executorBlockStrategyEnum) && job.isRunningOrHasQueue()) {
            return ExecutorBlockStrategyEnum.COVER_EARLY.name();
        }

        return null;
    }
}
