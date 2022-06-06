package com.scy.netty.job.callback;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author : shichunyang
 * Date    : 2022/6/6
 * Time    : 1:22 下午
 * ---------------------------------------
 * Desc    : CallbackParam
 */
@Getter
@Setter
@ToString
public class CallbackParam {

    private long logId;

    private long logDateTime;

    private int code;

    private String msg;

    public CallbackParam() {
    }

    public CallbackParam(long logId, long logDateTime, int code, String msg) {
        this.logId = logId;
        this.logDateTime = logDateTime;
        this.code = code;
        this.msg = msg;
    }
}
