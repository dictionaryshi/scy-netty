package com.scy.netty.server;

import com.scy.core.ObjectUtil;
import com.scy.core.format.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : shichunyang
 * Date    : 2022/2/10
 * Time    : 3:39 下午
 * ---------------------------------------
 * Desc    : AbstractServer
 */
@Slf4j
@Getter
@Setter
@ToString
public abstract class AbstractServer {

    private Runnable startedCallback;

    private Runnable beforeStopCallback;

    public abstract void start(ServerConfig serverConfig) throws Exception;

    public abstract void stop() throws Exception;

    public void onStarted() {
        if (!ObjectUtil.isNull(startedCallback)) {
            try {
                startedCallback.run();
            } catch (Exception e) {
                log.error(MessageUtil.format("AbstractServer onStarted error", e));
            }
        }
    }

    public void beforeStop() {
        if (!ObjectUtil.isNull(beforeStopCallback)) {
            try {
                beforeStopCallback.run();
            } catch (Exception e) {
                log.error(MessageUtil.format("AbstractServer beforeStop error", e));
            }
        }
    }
}
