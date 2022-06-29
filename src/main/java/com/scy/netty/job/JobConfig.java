package com.scy.netty.job;

import com.scy.core.CollectionUtil;
import com.scy.core.ObjectUtil;
import com.scy.core.StringUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageFormatUtil;
import com.scy.core.format.MessageUtil;
import com.scy.core.net.NetworkInterfaceUtil;
import com.scy.core.spring.ApplicationContextUtil;
import com.scy.netty.job.annotation.Job;
import com.scy.netty.job.callback.CallbackTask;
import com.scy.netty.server.ServerConfig;
import com.scy.netty.server.http.NettyHttpServer;
import lombok.Getter;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * @author : shichunyang
 * Date    : 2022/4/24
 * Time    : 4:01 下午
 * ---------------------------------------
 * Desc    : JobConfig
 */
@Getter
public class JobConfig implements SmartInitializingSingleton {

    private NettyHttpServer nettyHttpServer;

    private int port = 9097;

    private String ip;

    private String address;

    public static final String ADDRESS_TEMPLATE = "http://{0}";

    public static final ConcurrentMap<String, JobHandler> JOB_HANDLER_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = ApplicationContextUtil.getApplicationContext().getBeanNamesForType(Object.class, Boolean.FALSE, Boolean.TRUE);
        Stream.of(beanNames).forEach(beanName -> {
            Object bean = ApplicationContextUtil.getBean(beanName, null);

            Map<Method, Job> jobMethodMap = MethodIntrospector.selectMethods(bean.getClass(),
                    (MethodIntrospector.MetadataLookup<Job>) method -> AnnotatedElementUtils.findMergedAnnotation(method, Job.class));
            if (CollectionUtil.isEmpty(jobMethodMap)) {
                return;
            }

            jobMethodMap.forEach((method, job) -> register(bean, method, job));
        });

        CallbackTask.getInstance().start();

        nettyHttpServer = new NettyHttpServer();

        ip = NetworkInterfaceUtil.getIp();
        if (StringUtil.isEmpty(ip)) {
            throw new BusinessException("NettyHttpServer error 获取ip失败");
        }

        address = MessageFormatUtil.format(ADDRESS_TEMPLATE, NetworkInterfaceUtil.getIpPort(ip, port));

        nettyHttpServer.setStartedCallback(() -> {
            CallbackTask.getInstance().startRegistry(ApplicationContextUtil.getProperty(ApplicationContextUtil.APPLICATION_NAME), address);
        });

        nettyHttpServer.setBeforeStopCallback(() -> {
            CallbackTask.getInstance().removeRegistry(ApplicationContextUtil.getProperty(ApplicationContextUtil.APPLICATION_NAME), address);
        });

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(port);

        nettyHttpServer.start(serverConfig);
    }

    private void register(Object bean, Method method, Job job) {
        String handlerName = job.value();
        if (!ObjectUtil.isNull(JOB_HANDLER_MAP.get(handlerName))) {
            throw new BusinessException(MessageUtil.format("handlerName 重复", "handlerName", handlerName));
        }

        method.setAccessible(Boolean.TRUE);

        Class<?> clazz = bean.getClass();

        Method initMethod = null;

        if (!StringUtil.isEmpty(job.init())) {
            try {
                initMethod = clazz.getDeclaredMethod(job.init());
                initMethod.setAccessible(Boolean.TRUE);
            } catch (NoSuchMethodException e) {
                throw new BusinessException(MessageUtil.format("handler init method 不存在", "handlerName", handlerName));
            }
        }

        Method destroyMethod = null;

        if (!StringUtil.isEmpty(job.destroy())) {
            try {
                destroyMethod = clazz.getDeclaredMethod(job.destroy());
                destroyMethod.setAccessible(Boolean.TRUE);
            } catch (NoSuchMethodException e) {
                throw new BusinessException(MessageUtil.format("handler destroy method 不存在", "handlerName", handlerName));
            }
        }

        JOB_HANDLER_MAP.put(handlerName, new MethodJobHandler(bean, initMethod, method, destroyMethod));
    }
}
