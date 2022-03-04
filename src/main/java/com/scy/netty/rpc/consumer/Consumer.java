package com.scy.netty.rpc.consumer;

import com.scy.core.ObjectUtil;
import com.scy.core.StringUtil;
import com.scy.core.UUIDUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.format.NumberUtil;
import com.scy.core.proxy.ProxyUtil;
import com.scy.core.reflect.AnnotationUtil;
import com.scy.core.reflect.ReflectionsUtil;
import com.scy.core.rest.ResponseResult;
import com.scy.core.trace.TraceUtil;
import com.scy.netty.client.AbstractConnectClient;
import com.scy.netty.client.ClientConfig;
import com.scy.netty.model.rpc.RpcRequest;
import com.scy.netty.model.rpc.RpcResponse;
import com.scy.netty.rpc.RpcResponseFuture;
import com.scy.netty.rpc.RpcResponseFutureUtil;
import com.scy.netty.rpc.provider.Provider;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author : shichunyang
 * Date    : 2022/3/2
 * Time    : 8:12 下午
 * ---------------------------------------
 * Desc    : Consumer
 */
@Slf4j
public class Consumer implements BeanPostProcessor {

    private final ClientConfig clientConfig;

    public Consumer(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionsUtil.doWithFields(
                bean.getClass(),
                field -> fillProxyInstance(bean, field),
                field -> !Objects.isNull(AnnotationUtil.findAnnotation(field, RpcReference.class))
        );
        return bean;
    }

    private void fillProxyInstance(Object bean, Field field) throws IllegalAccessException {
        Class<?> fieldClass = field.getType();
        if (!fieldClass.isInterface()) {
            throw new BusinessException(MessageUtil.format("rpcReference not interface",
                    "className", field.getDeclaringClass().getName(), "fieldClass", fieldClass.getName()));
        }

        RpcReference rpcReference = AnnotationUtil.findAnnotation(field, RpcReference.class);

        long timeout = rpcReference.timeout();
        if (timeout <= NumberUtil.ZERO.longValue()) {
            throw new BusinessException(MessageUtil.format("timeout <= 0",
                    "className", field.getDeclaringClass().getName(), "fieldClass", fieldClass.getName()));
        }

        String serviceKey = Provider.getServiceKey(fieldClass.getName(), rpcReference.version());

        Object serviceProxy = ProxyUtil.newProxyInstance(fieldClass, (InvocationHandler) (proxy, method, args) -> {
            String className = method.getDeclaringClass().getName();
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameters = args;
            String version = rpcReference.version();

            if (className.equals(Object.class.getName())) {
                throw new BusinessException(MessageUtil.format("method not support rpc", "className", className, "methodName", methodName));
            }

            String address = rpcReference.address();
            // TODO 注册中心获取address
            if (StringUtil.isEmpty(address)) {
                throw new BusinessException(MessageUtil.format("rpc address not found", "serviceKey", serviceKey));
            }

            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId(UUIDUtil.uuid());
            rpcRequest.setCreateTime(System.currentTimeMillis());
            rpcRequest.setClassName(className);
            rpcRequest.setMethodName(methodName);
            rpcRequest.setParameterTypes(parameterTypes);
            rpcRequest.setParameters(parameters);
            rpcRequest.setVersion(version);
            rpcRequest.setTraceId(TraceUtil.getTraceId());

            long startTime = System.currentTimeMillis();

            RpcResponseFuture rpcResponseFuture = new RpcResponseFuture(rpcRequest);

            ChannelFuture channelFuture = AbstractConnectClient.asyncSend(address, clientConfig, rpcRequest);
            channelFuture.addListener((GenericFutureListener<ChannelFuture>) future -> {
                long endTime = System.currentTimeMillis();
                if (!future.isSuccess() || !ObjectUtil.isNull(future.cause())) {
                    log.error(MessageUtil.format("rpc request fail", "address", address, "rpcRequest", rpcRequest));
                    rpcResponseFuture.setThrowable(future.cause());
                    rpcResponseFuture.cancel(Boolean.TRUE);
                }
            });

            ResponseResult<Object> responseResult = new ResponseResult<>();
            responseResult.setFuture(rpcResponseFuture);
            return responseResult;
        });

        field.setAccessible(Boolean.TRUE);
        field.set(bean, serviceProxy);

        // TODO 服务发现
    }

    public static void notifyRpcResponseFuture(RpcResponse rpcResponse) {
        try {
            RpcResponseFuture rpcResponseFuture = RpcResponseFutureUtil.getRpcResponseFuture(rpcResponse.getRequestId());
            if (ObjectUtil.isNull(rpcResponseFuture)) {
                return;
            }

            rpcResponseFuture.setRpcResponse(rpcResponse);
        } finally {
            RpcResponseFutureUtil.removeRpcResponseFuture(rpcResponse.getRequestId());
        }
    }
}
