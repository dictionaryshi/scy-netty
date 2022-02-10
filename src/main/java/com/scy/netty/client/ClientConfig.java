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

    private List<Runnable> stoppedCallbacks = new ArrayList<>();

    public void addStoppedCallback(Runnable stoppedCallback) {
        stoppedCallbacks.add(stoppedCallback);
    }

    public void stop() throws Exception {
        for (Runnable stopCallback : stoppedCallbacks) {
            try {
                stopCallback.run();
            } catch (Exception e) {
                log.error(MessageUtil.format("ClientConfig stop error", e));
            }
        }
    }
}
