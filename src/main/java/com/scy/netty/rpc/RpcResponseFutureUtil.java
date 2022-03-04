package com.scy.netty.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author : shichunyang
 * Date    : 2022/2/18
 * Time    : 11:08 上午
 * ---------------------------------------
 * Desc    : RpcResponseFutureUtil
 */
public class RpcResponseFutureUtil {

    private RpcResponseFutureUtil() {
    }

    private static final ConcurrentMap<String, RpcResponseFuture> RPC_RESPONSE_FUTURE_POOL = new ConcurrentHashMap<>();

    public static void addRpcResponseFuture(String requestId, RpcResponseFuture rpcResponseFuture) {
        RPC_RESPONSE_FUTURE_POOL.put(requestId, rpcResponseFuture);
    }

    public static void removeRpcResponseFuture(String requestId) {
        RPC_RESPONSE_FUTURE_POOL.remove(requestId);
    }

    public static RpcResponseFuture getRpcResponseFuture(String requestId) {
        return RPC_RESPONSE_FUTURE_POOL.get(requestId);
    }
}
