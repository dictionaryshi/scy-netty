package com.scy.netty.rpc.provider;

import com.scy.core.ArrayUtil;
import com.scy.core.CollectionUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.spring.ApplicationContextUtil;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.netty.rpc.provider.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author : shichunyang
 * Date    : 2022/2/18
 * Time    : 1:25 下午
 * ---------------------------------------
 * Desc    : Provider
 */
public class Provider implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.setApplicationContext(applicationContext);

        Map<String, Object> serviceBeanMap = ApplicationContextUtil.getBeansWithAnnotation(RpcService.class);
        if (CollectionUtil.isEmpty(serviceBeanMap)) {
            return;
        }

        for (Object serviceBean : serviceBeanMap.values()) {
            if (ArrayUtil.isEmpty(serviceBean.getClass().getInterfaces())) {
                throw new BusinessException(MessageUtil.format("rpc服务必须继承接口", "className", serviceBean.getClass().getName()));
            }

            String interfaceName = serviceBean.getClass().getInterfaces()[0].getName();

            RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);

            String threadPoolName = getThreadPoolName(interfaceName, rpcService.version());
            ThreadPoolUtil.getThreadPool(threadPoolName, rpcService.corePoolSize(), rpcService.maximumPoolSize(), rpcService.queueSize());
        }
    }

    public static String getThreadPoolName(String interfaceName, String version) {
        return interfaceName.concat("-").concat(version).concat("-rpc-pool");
    }
}
