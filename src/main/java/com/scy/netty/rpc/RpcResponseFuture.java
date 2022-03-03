package com.scy.netty.rpc;

import com.scy.core.ObjectUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.netty.model.rpc.RpcRequest;
import com.scy.netty.model.rpc.RpcResponse;

import java.util.concurrent.*;

/**
 * @author : shichunyang
 * Date    : 2022/2/17
 * Time    : 8:35 下午
 * ---------------------------------------
 * Desc    : RpcResponseFuture
 */
public class RpcResponseFuture<T> implements Future<T> {

    private volatile Throwable throwable;

    private volatile RpcRequest rpcRequest;

    private volatile RpcResponse<T> rpcResponse;

    private volatile boolean cancelled = Boolean.FALSE;

    private volatile boolean done = Boolean.FALSE;

    private final Object lock = new Object();

    public RpcResponseFuture(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;

        RpcResponseFutureUtil.addRpcResponseFuture(rpcRequest.getRequestId(), this);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public RpcRequest getRpcRequest() {
        return rpcRequest;
    }

    public void setRpcRequest(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }

    public RpcResponse<T> getRpcResponse() {
        return rpcResponse;
    }

    public void setRpcResponse(RpcResponse<T> rpcResponse) {
        this.rpcResponse = rpcResponse;

        synchronized (lock) {
            done = Boolean.TRUE;
            lock.notifyAll();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled = Boolean.TRUE;

        synchronized (lock) {
            done = Boolean.TRUE;
            lock.notifyAll();
        }

        return cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        throw new BusinessException("prohibited methods");
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            if (!done) {
                synchronized (lock) {
                    long timeoutMillis = (TimeUnit.MILLISECONDS == unit) ? timeout : TimeUnit.MILLISECONDS.convert(timeout, unit);
                    lock.wait(timeoutMillis);
                }
            }

            if (!done) {
                throw new BusinessException(MessageUtil.format("rpc timeout", "rpcRequest", rpcRequest));
            }

            if (ObjectUtil.isNull(throwable)) {
                throw new BusinessException(MessageUtil.format("rpc fail", "rpcRequest", rpcRequest), throwable);
            }

            if (rpcResponse.isSuccess()) {
                return rpcResponse.getData();
            }

            throw new BusinessException(rpcResponse.getMessage());
        } finally {
            RpcResponseFutureUtil.removeRpcResponseFuture(rpcRequest.getRequestId());
        }
    }
}
