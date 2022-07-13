package com.scy.netty.job.callback;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author : shichunyang
 * Date    : 2022/7/13
 * Time    : 3:03 下午
 * ---------------------------------------
 * Desc    : CallbackRequest
 */
@Getter
@Setter
@ToString
public class CallbackRequest {

    private List<CallbackParam> callbackParamList;
}
