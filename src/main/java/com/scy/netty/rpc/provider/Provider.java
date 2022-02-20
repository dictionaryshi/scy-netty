package com.scy.netty.rpc.provider;

import com.scy.core.ArrayUtil;
import com.scy.core.CollectionUtil;
import com.scy.core.StringUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.reflect.MethodUtil;
import com.scy.core.spring.ApplicationContextUtil;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.netty.rpc.provider.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
public class Provider implements ApplicationContextAware {

    private static final Map<String, Object> SERVICE_MAP = new ConcurrentHashMap<>();

    public static Map<String, Object> getServiceMap() {
        return SERVICE_MAP;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.setApplicationContext(applicationContext);

        Map<String, Object> serviceBeanMap = ApplicationContextUtil.getBeansWithAnnotation(RpcService.class);
        if (CollectionUtil.isEmpty(serviceBeanMap)) {
            return;
        }

        serviceBeanMap.values().forEach(serviceBean -> {
            if (ArrayUtil.isEmpty(serviceBean.getClass().getInterfaces())) {
                throw new BusinessException(MessageUtil.format("rpc服务必须继承接口", "className", serviceBean.getClass().getName()));
            }

            String interfaceName = serviceBean.getClass().getInterfaces()[0].getName();

            RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);

            String threadPoolName = getThreadPoolName(interfaceName, rpcService.version());
            ThreadPoolUtil.getThreadPool(threadPoolName, rpcService.corePoolSize(), rpcService.maximumPoolSize(), rpcService.queueSize());

            Method[] methods = serviceBean.getClass().getDeclaredMethods();
            Stream.of(methods).forEach(MethodUtil::putMethod);

            String serviceKey = getServiceKey(interfaceName, rpcService.version());
            SERVICE_MAP.put(serviceKey, serviceBean);
        });
    }

    public static String getThreadPoolName(String interfaceName, String version) {
        if (StringUtil.isEmpty(version)) {
            return interfaceName.concat("-rpc-pool");
        }
        return interfaceName.concat("-").concat(version).concat("-rpc-pool");
    }

    public static String getServiceKey(String interfaceName, String version) {
        if (StringUtil.isEmpty(version)) {
            return interfaceName;
        }
        return interfaceName.concat("-").concat(version);
    }
}
