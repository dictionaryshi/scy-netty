package com.scy.netty.rpc.consumer;

import com.scy.core.reflect.AnnotationUtil;
import com.scy.core.reflect.ReflectionsUtil;
import com.scy.netty.client.ClientConfig;
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
        ReflectionsUtil.doWithFields(bean.getClass(), this::fillProxyInstance, field -> !Objects.isNull(AnnotationUtil.findAnnotation(field, RpcReference.class)));
        return bean;
    }

    private void fillProxyInstance(Field field) {
    }
}
