package com.scy.netty.job;

import com.scy.core.thread.ThreadLocalUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author : shichunyang
 * Date    : 2022/4/30
 * Time    : 8:18 下午
 * ---------------------------------------
 * Desc    : JobContext
 */
@Getter
@Setter
public class JobContext {

    public static final int CODE_SUCCESS = 0;

    public static final int CODE_FAIL = 500;

    public static final int CODE_TIMEOUT = 507;

    public static final String JOB_CONTEXT = "job_context";

    private long jobId;

    private long logId;

    private String jobParam;

    private int shardIndex;

    private int shardTotal;

    private int code;

    private String msg;

    private String jobLogFileName;

    public JobContext(long jobId, long logId, String jobParam, int shardIndex, int shardTotal) {
        this.jobId = jobId;

        this.logId = logId;

        this.jobParam = jobParam;

        this.shardIndex = shardIndex;

        this.shardTotal = shardTotal;

        this.code = CODE_SUCCESS;
    }

    public static void setJobContext(JobContext jobContext) {
        ThreadLocalUtil.put(JOB_CONTEXT, jobContext);
    }

    public static JobContext getJobContext() {
        return (JobContext) ThreadLocalUtil.get(JOB_CONTEXT);
    }

    public static void clearJobContext() {
        ThreadLocalUtil.remove(JOB_CONTEXT);
    }

    public static long queryJobId() {
        JobContext jobContext = getJobContext();
        if (Objects.isNull(jobContext)) {
            return -1;
        }

        return jobContext.getJobId();
    }

    public static String queryJobParam() {
        JobContext jobContext = getJobContext();
        if (Objects.isNull(jobContext)) {
            return null;
        }

        return jobContext.getJobParam();
    }

    public static int queryShardIndex() {
        JobContext jobContext = getJobContext();
        if (Objects.isNull(jobContext)) {
            return -1;
        }

        return jobContext.getShardIndex();
    }

    public static int queryShardTotal() {
        JobContext jobContext = getJobContext();
        if (Objects.isNull(jobContext)) {
            return -1;
        }

        return jobContext.getShardTotal();
    }

    public static boolean handleResult(int code, String msg) {
        JobContext jobContext = getJobContext();
        if (Objects.isNull(jobContext)) {
            return Boolean.FALSE;
        }

        jobContext.setCode(code);

        jobContext.setMsg(msg);

        return Boolean.TRUE;
    }

    public static String queryJobLogFileName() {
        JobContext jobContext = getJobContext();
        if (Objects.isNull(jobContext)) {
            return null;
        }

        return jobContext.getJobLogFileName();
    }
}
