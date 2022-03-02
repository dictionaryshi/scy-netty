package com.scy.netty.client;

import com.scy.core.format.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : shichunyang
 * Date    : 2022/2/9
 * Time    : 8:06 下午
 * ---------------------------------------
 * Desc    : ClientConfig
 */
@Slf4j
@Getter
@Setter
@ToString
public class ClientConfig {

    private Class<? extends AbstractConnectClient> connectClientClass;

    private List<Runnable> stopCallbacks = new ArrayList<>();

    public void addStopCallback(Runnable stopCallback) {
        stopCallbacks.add(stopCallback);
    }

    public void stop() {
        for (Runnable stopCallback : stopCallbacks) {
            try {
                stopCallback.run();
            } catch (Exception e) {
                log.error(MessageUtil.format("ClientConfig stop error", e));
            }
        }
    }
}
