package com.scy.netty.server.handler;

import com.scy.core.exception.BusinessException;
import com.scy.core.exception.ExceptionUtil;
import com.scy.core.format.MessageUtil;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.core.trace.TraceUtil;
import com.scy.netty.model.rpc.RpcRequest;
import com.scy.netty.model.rpc.RpcResponse;
import com.scy.netty.rpc.provider.Provider;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : shichunyang
 * Date    : 2022/2/22
 * Time    : 1:29 下午
 * ---------------------------------------
 * Desc    : RpcRequestHandler
 */
@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        try {
            init(rpcRequest);

            String threadPoolName = Provider.getThreadPoolName(rpcRequest.getClassName(), rpcRequest.getVersion());
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.THREAD_POOLS.get(threadPoolName);
            if (Objects.isNull(threadPoolExecutor)) {
                throw new BusinessException(MessageUtil.format("no server thread pool", "threadPoolName", threadPoolName));
            }

            threadPoolExecutor.execute(() -> {
            });
        } catch (Exception e) {
            RpcResponse<?> rpcResponse = new RpcResponse<>();
            rpcResponse.setSuccess(Boolean.FALSE);
            rpcResponse.setRequestId(rpcRequest.getRequestId());
            rpcResponse.setMessage(ExceptionUtil.getExceptionMessageWithTraceId(e));
            rpcResponse.setData(null);
            ctx.writeAndFlush(rpcResponse);
        } finally {
            TraceUtil.clearTrace();
        }
    }

    private void init(RpcRequest rpcRequest) {
        TraceUtil.setTraceId(rpcRequest.getTraceId());
    }
}
