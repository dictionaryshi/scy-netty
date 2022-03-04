package com.scy.netty.model.rpc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.Future;

/**
 * @author : shichunyang
 * Date    : 2022/3/4
 * Time    : 3:11 下午
 * ---------------------------------------
 * Desc    : RpcResult
 */
@Getter
@Setter
@ToString
public class RpcResult<T> {

    private T data;

    private Future<T> future;
}
