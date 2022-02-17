package com.scy.netty.model.rpc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author : shichunyang
 * Date    : 2022/2/17
 * Time    : 7:46 下午
 * ---------------------------------------
 * Desc    : RpcResponse
 */
@Getter
@Setter
@ToString
public class RpcResponse<T> implements Serializable {

    private boolean success;

    private String requestId;

    private String message;

    private T data;
}
