package com.scy.netty.job.util;

import com.scy.core.IOUtil;
import com.scy.core.StringUtil;
import com.scy.core.SystemUtil;
import com.scy.core.format.DateUtil;
import com.scy.netty.job.JobContext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

/**
 * @author : shichunyang
 * Date    : 2022/6/2
 * Time    : 7:00 下午
 * ---------------------------------------
 * Desc    : JobLogUtil
 */
public class JobLogUtil {

    public static void log(String message) {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String logMessage = getLogMessage(stackTraceElement, message);

        Optional.ofNullable(JobContext.getJobContext()).map(JobContext::getJobLogFileName)
                .ifPresent(logFileName -> {
                    try {
                        IOUtil.writeLines(new File(logFileName), SystemUtil.CHARSET_UTF_8_STR, Collections.singleton(logMessage), Boolean.TRUE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void log(long logDateTime, long logId, String message) {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String logMessage = getLogMessage(stackTraceElement, message);
        try {
            IOUtil.writeLines(new File(getJobLogFileName(logDateTime, logId)), SystemUtil.CHARSET_UTF_8_STR, Collections.singleton(logMessage), Boolean.TRUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLogMessage(StackTraceElement stackTraceElement, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(DateUtil.date2Str(DateUtil.getCurrentDate(), DateUtil.PATTERN_MILLISECOND)).append(StringUtil.SPACE)
                .append("[").append(Thread.currentThread().getName()).append("]")
                .append("[").append(stackTraceElement.getClassName()).append(StringUtil.POINT).append(stackTraceElement.getMethodName()).append("]")
                .append("[").append(stackTraceElement.getLineNumber()).append("]")
                .append(StringUtil.SPACE)
                .append(message);
        return sb.toString();
    }

    public static String getJobLogFileName(long logDateTime, long logId) {
        return IOUtil.getFile(IOUtil.getUserDirectory(), "job_log",
                DateUtil.date2Str(new Date(logDateTime), DateUtil.PATTERN_DAY), String.valueOf(logId).concat(".log")).getAbsolutePath();
    }
}
