package com.scy.netty.rpc.consumer;

import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.format.NumberUtil;
import com.scy.core.reflect.AnnotationUtil;
import com.scy.core.reflect.ReflectionsUtil;
import com.scy.netty.client.ClientConfig;
import com.scy.netty.rpc.provider.Provider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author : shichunyang
 * Date    : 2022/3/2
 * Time    : 8:12 下午
 * ---------------------------------------
 * Desc    : Consumer
 */
public class Consumer implements BeanPostProcessor {

    private final ClientConfig clientConfig;

    public Consumer(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionsUtil.doWithFields(
                bean.getClass(),
                field -> fillProxyInstance(bean, field),
                field -> !Objects.isNull(AnnotationUtil.findAnnotation(field, RpcReference.class))
        );
        return bean;
    }

    private void fillProxyInstance(Object bean, Field field) {
        Class<?> fieldClass = field.getType();
        if (!fieldClass.isInterface()) {
            throw new BusinessException(MessageUtil.format("rpcReference not interface",
                    "className", bean.getClass().getName(), "fieldClass", fieldClass.getName()));
        }

        RpcReference rpcReference = AnnotationUtil.findAnnotation(field, RpcReference.class);

        String version = rpcReference.version();

        long timeout = rpcReference.timeout();
        if (timeout <= NumberUtil.ZERO.longValue()) {
            throw new BusinessException(MessageUtil.format("timeout <= 0",
                    "className", bean.getClass().getName(), "fieldClass", fieldClass.getName()));
        }

        String serviceKey = Provider.getServiceKey(fieldClass.getName(), version);
    }
}
