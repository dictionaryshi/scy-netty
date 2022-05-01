package com.scy.netty.job;

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
}
