package com.scy.netty.server.handler;

import com.scy.core.ObjectUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.exception.ExceptionUtil;
import com.scy.core.format.MessageUtil;
import com.scy.core.reflect.MethodUtil;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.core.trace.TraceUtil;
import com.scy.netty.model.rpc.RpcRequest;
import com.scy.netty.model.rpc.RpcResponse;
import com.scy.netty.rpc.provider.Provider;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
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

    public static final int TIME_OUT = 180_000;

    public static final RpcRequestHandler INSTANCE = new RpcRequestHandler();

    private RpcRequestHandler() {
    }

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
                RpcResponse<Object> rpcResponse = invoke(rpcRequest);
                ctx.writeAndFlush(rpcResponse);
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

    private RpcResponse<Object> invoke(RpcRequest rpcRequest) {
        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        rpcResponse.setRequestId(rpcRequest.getRequestId());

        String serviceKey = Provider.getServiceKey(rpcRequest.getClassName(), rpcRequest.getVersion());
        Object serviceBean = Provider.getServiceMap().get(serviceKey);
        if (ObjectUtil.isNull(serviceBean)) {
            rpcResponse.setSuccess(Boolean.FALSE);
            rpcResponse.setMessage(MessageUtil.format("serviceKey not found", "serviceKey", serviceKey));
            rpcResponse.setData(null);
            return rpcResponse;
        }

        if (System.currentTimeMillis() - rpcRequest.getCreateTime() > TIME_OUT) {
            rpcResponse.setSuccess(Boolean.FALSE);
            rpcResponse.setMessage(MessageUtil.format("request timeout"));
            rpcResponse.setData(null);
            return rpcResponse;
        }

        try {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = rpcRequest.getMethodName();
            Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
            Object[] parameters = rpcRequest.getParameters();

            Method method = MethodUtil.getMethod(serviceClass, methodName, parameterTypes);
            if (ObjectUtil.isNull(method)) {
                rpcResponse.setSuccess(Boolean.FALSE);
                rpcResponse.setMessage(MessageUtil.format("request method not found", "methodName", methodName));
                rpcResponse.setData(null);
                return rpcResponse;
            }

            Object result = method.invoke(serviceBean, parameters);

            rpcResponse.setSuccess(Boolean.TRUE);
            rpcResponse.setMessage(null);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (Throwable throwable) {
            rpcResponse.setSuccess(Boolean.FALSE);
            rpcResponse.setMessage(ExceptionUtil.getExceptionMessageWithTraceId(throwable));
            rpcResponse.setData(null);
            return rpcResponse;
        }
    }

    private void init(RpcRequest rpcRequest) {
        TraceUtil.setTraceId(rpcRequest.getTraceId());
    }
}
