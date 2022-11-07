package com.scy.netty.mq;

import com.scy.core.CollectionUtil;
import com.scy.core.StringUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.reflect.ClassUtil;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.zookeeper.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author : shichunyang
 * Date    : 2022/11/7
 * Time    : 11:20 上午
 * ---------------------------------------
 * Desc    : ConsumerConfig
 */
public class ConsumerConfig implements ApplicationContextAware, DisposableBean {

    private ZkClient zkClient;

    private MqService mqService;

    public static final List<ConsumerThread> CONSUMER_THREADS = new ArrayList<>();

    public static final ThreadPoolExecutor CONSUMER_EXECUTOR = ThreadPoolUtil.getThreadPool("mq-consumer", 10, 30, 1024);

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

        validConsumer(consumerList);

        startConsumer();
    }

    @SuppressWarnings(ClassUtil.UNCHECKED)
    private void validConsumer(List<MqConsumer> consumerList) {
        consumerList.forEach(consumer -> {
            Consumer annotation = consumer.getClass().getAnnotation(Consumer.class);
            if (StringUtil.isEmpty(annotation.group())) {
                try {
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
                    Field field = invocationHandler.getClass().getDeclaredField("memberValues");
                    field.setAccessible(Boolean.TRUE);

                    Map<String, Object> memberValues = (Map<String, Object>) field.get(invocationHandler);
                    memberValues.put("group", Consumer.DEFAULT_GROUP);
                } catch (Exception e) {
                    throw new BusinessException(MessageUtil.format("group empty and generator error", e, "consumer", consumer.getClass().getName()));
                }
            }

            CONSUMER_THREADS.add(new ConsumerThread(zkClient, mqService, consumer));
        });
    }

    private void startConsumer() {
        CONSUMER_THREADS.forEach(consumerThread -> {
            String topic = consumerThread.getConsumer().topic();
            String group = consumerThread.getConsumer().group();
            String uuid = consumerThread.getUuid();

            zkClient.createNode("/mq/".concat(topic).concat("/").concat(group).concat("_").concat(uuid), StringUtil.EMPTY, CreateMode.EPHEMERAL);
        });

        CONSUMER_THREADS.forEach(CONSUMER_EXECUTOR::execute);
    }

    @Override
    public void destroy() throws Exception {
        CONSUMER_THREADS.forEach(consumerThread -> {
            String topic = consumerThread.getConsumer().topic();
            String group = consumerThread.getConsumer().group();
            String uuid = consumerThread.getUuid();

            zkClient.delete("/mq/".concat(topic).concat("/").concat(group).concat("_").concat(uuid));
        });
    }
}
