package com.scy.netty.mq;

import com.scy.core.CollectionUtil;
import com.scy.core.UUIDUtil;
import com.scy.zookeeper.ZkClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.TreeSet;

/**
 * @author : shichunyang
 * Date    : 2022/11/7
 * Time    : 1:06 下午
 * ---------------------------------------
 * Desc    : ConsumerThread
 */
@Slf4j
@Getter
@Setter
public class ConsumerThread implements Runnable {

    private ZkClient zkClient;

    private MqService mqService;

    private MqConsumer mqConsumer;

    private Consumer consumer;

    private String uuid;

    public ConsumerThread() {
    }

    public ConsumerThread(ZkClient zkClient, MqService mqService, MqConsumer mqConsumer) {
        this.zkClient = zkClient;
        this.mqService = mqService;
        this.mqConsumer = mqConsumer;
        this.consumer = mqConsumer.getClass().getAnnotation(Consumer.class);
        this.uuid = UUIDUtil.uuid();
    }

    public MqActiveInfo getMqActiveInfo() {
        List<String> groups = zkClient.getChildren("/mq/".concat(consumer.topic()));
        if (CollectionUtil.isEmpty(groups)) {
            return null;
        }

        TreeSet<String> groupSet = new TreeSet<>(groups);

        int rank = -1;
        for (String group : groupSet) {
            rank++;
            if (group.equals(consumer.group().concat("_").concat(uuid))) {
                break;
            }
        }

        if (rank == -1) {
            return null;
        }

        return new MqActiveInfo(rank, groupSet.size(), groupSet.toString());
    }

    @Override
    public void run() {
    }
}
