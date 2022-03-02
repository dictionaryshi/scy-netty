package com.scy.netty.rpc.consumer;

import com.scy.netty.model.rpc.RpcGenericRequest;

import java.util.concurrent.Future;

/**
 * @author : shichunyang
 * Date    : 2022/3/2
 * Time    : 6:56 下午
 * ---------------------------------------
 * Desc    : RpcGenericService
 */
public interface RpcGenericService<T> {

    /**
     * 泛化调用
     */
    Future<T> invoke(RpcGenericRequest rpcGenericRequest);
}
