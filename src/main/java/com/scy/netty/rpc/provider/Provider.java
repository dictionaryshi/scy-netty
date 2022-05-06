package com.scy.netty.rpc.provider;

import com.scy.core.ArrayUtil;
import com.scy.core.ObjectUtil;
import com.scy.core.StringUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.reflect.AnnotationUtil;
import com.scy.core.reflect.MethodUtil;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.netty.rpc.provider.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author : shichunyang
 * Date    : 2022/2/18
 * Time    : 1:25 下午
 * ---------------------------------------
 * Desc    : Provider
 */
public class Provider implements BeanPostProcessor {

    private final Map<String, String> SERVICE_INTERFACE_NAME = new ConcurrentHashMap<>();

    private final Map<String, Method[]> SERVICE_METHODS = new ConcurrentHashMap<>();

    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

    public static Map<String, Object> getServiceMap() {
        return SERVICE_MAP;
    }

    @Override
    public Object postProcessBeforeInitialization(Object serviceBean, String beanName) throws BeansException {
        RpcService rpcService = AnnotationUtil.findAnnotation(serviceBean.getClass(), RpcService.class);
        if (ObjectUtil.isNull(rpcService)) {
            return serviceBean;
        }

        if (ArrayUtil.isEmpty(serviceBean.getClass().getInterfaces())) {
            throw new BusinessException(MessageUtil.format("rpc服务必须继承接口", "className", serviceBean.getClass().getName()));
        }

        String interfaceName = serviceBean.getClass().getInterfaces()[0].getName();
        SERVICE_INTERFACE_NAME.put(beanName, interfaceName);

        Method[] methods = serviceBean.getClass().getDeclaredMethods();
        SERVICE_METHODS.put(beanName, methods);

        return serviceBean;
    }

    @Override
    public Object postProcessAfterInitialization(Object serviceBean, String beanName) throws BeansException {
        RpcService rpcService = AnnotationUtil.findAnnotation(serviceBean.getClass(), RpcService.class);
        if (ObjectUtil.isNull(rpcService)) {
            return serviceBean;
        }

        String interfaceName = SERVICE_INTERFACE_NAME.get(beanName);

        String threadPoolName = getThreadPoolName(interfaceName, rpcService.version());
        ThreadPoolUtil.getThreadPool(threadPoolName, rpcService.corePoolSize(), rpcService.maximumPoolSize(), rpcService.queueSize());

        String serviceKey = getServiceKey(interfaceName, rpcService.version());
        SERVICE_MAP.put(serviceKey, serviceBean);

        Method[] methods = SERVICE_METHODS.get(beanName);
        Stream.of(methods).forEach(method -> MethodUtil.getMethod(serviceBean.getClass(), method.getName(), method.getParameterTypes()));

        return serviceBean;
    }

    public static String getThreadPoolName(String interfaceName, String version) {
        if (StringUtil.isEmpty(version)) {
            return interfaceName.concat("-rpc");
        }
        return interfaceName.concat("-").concat(version).concat("-rpc");
    }

    public static String getServiceKey(String interfaceName, String version) {
        if (StringUtil.isEmpty(version)) {
            return interfaceName;
        }
        return interfaceName.concat("-").concat(version);
    }
}
