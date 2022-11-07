package com.scy.netty.mq;

import com.scy.core.CollectionUtil;
import com.scy.zookeeper.ZkClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : shichunyang
 * Date    : 2022/11/7
 * Time    : 11:20 上午
 * ---------------------------------------
 * Desc    : ConsumerConfig
 */
public class ConsumerConfig implements ApplicationContextAware {

    private ZkClient zkClient;

    private MqService mqService;

    public ConsumerConfig() {
    }

    public ConsumerConfig(ZkClient zkClient, MqService mqService) {
        this.zkClient = zkClient;
        this.mqService = mqService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(Consumer.class);
        if (CollectionUtil.isEmpty(serviceMap)) {
            return;
        }

        List<MqConsumer> consumerList = serviceMap.values().stream()
                .filter(serviceBean -> serviceBean instanceof MqConsumer)
                .map(serviceBean -> (MqConsumer) serviceBean)
                .collect(Collectors.toList());
    }
}
