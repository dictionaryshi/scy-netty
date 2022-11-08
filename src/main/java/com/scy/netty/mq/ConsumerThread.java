package com.scy.netty.mq;

import com.scy.core.CollectionUtil;
import com.scy.core.UUIDUtil;
import com.scy.core.enums.JvmStatus;
import com.scy.core.exception.ExceptionUtil;
import com.scy.core.format.MessageUtil;
import com.scy.core.net.NetworkInterfaceUtil;
import com.scy.core.thread.ThreadUtil;
import com.scy.core.trace.TraceUtil;
import com.scy.zookeeper.ZkClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
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

    private Thread thread;

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
        thread = Thread.currentThread();

        int waitTime = 0;

        while (!JvmStatus.JVM_CLOSE_FLAG) {
            try {
                TraceUtil.setTraceId(null);

                MqActiveInfo mqActiveInfo = getMqActiveInfo();
                if (Objects.isNull(mqActiveInfo)) {
                    ThreadUtil.quietSleep(3000);
                    continue;
                }

                List<MqMessage> messageList = mqService.pull(consumer.topic(), consumer.group(), mqActiveInfo.getRank(), mqActiveInfo.getTotal());
                if (CollectionUtil.isEmpty(messageList)) {
                    waitTime = Math.min((waitTime + 10_000), 60_000);
                    ThreadUtil.quietSleep(waitTime);
                    continue;
                }

                waitTime = 0;

                for (MqMessage mqMessage : messageList) {
                    // double check
                    MqActiveInfo newMqActiveInfo = getMqActiveInfo();
                    if (Objects.isNull(newMqActiveInfo) || !Objects.equals(newMqActiveInfo.getRank(), mqActiveInfo.getRank()) || !Objects.equals(newMqActiveInfo.getTotal(), mqActiveInfo.getTotal())) {
                        break;
                    }

                    // lock message
                    String appendLog = MessageUtil.format("message lock", "ip", NetworkInterfaceUtil.getIp(), "mqActiveInfo", newMqActiveInfo);
                    int lockRet = mqService.lockMessage(mqMessage.getId(), appendLog);
                    if (lockRet < 1) {
                        continue;
                    }

                    MqResult mqResult;
                    try {
                        // consume message
                        mqResult = mqConsumer.consume(mqMessage.getData());

                        if (Objects.isNull(mqResult)) {
                            mqResult = MqResult.FAIL;
                        }
                    } catch (Exception e) {
                        String errorMsg = ExceptionUtil.getExceptionMessageWithTraceId(e);
                        mqResult = new MqResult(Boolean.FALSE, errorMsg);
                    }

                    // callback
                    mqMessage.setStatus(mqResult.isSuccess() ? MessageStatusEnum.SUCCESS.getStatus() : MessageStatusEnum.FAIL.getStatus());

                    String log = MessageUtil.format("message consume", "result", mqResult.isSuccess(), "content", mqResult.getContent());
                    mqMessage.setLog(log);
                }
            } catch (Exception e) {
            } finally {
                TraceUtil.clearTrace();
            }
        }
    }
}
