package com.scy.netty.rpc.consumer;

import com.scy.netty.client.ClientConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

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
        return bean;
    }
}
