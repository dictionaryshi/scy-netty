package com.scy.netty.job.callback;

import com.scy.core.CollectionUtil;
import com.scy.core.enums.JvmStatus;
import com.scy.core.format.MessageUtil;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.netty.server.http.HttpServerHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : shichunyang
 * Date    : 2022/6/6
 * Time    : 1:34 下午
 * ---------------------------------------
 * Desc    : CallbackTask
 */
@Slf4j
public class CallbackTask {

    private final LinkedBlockingQueue<CallbackParam> callBackQueue = new LinkedBlockingQueue<>();

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = ThreadPoolUtil.getThreadPool("job-call-back", 5, 5, 5);

    private static final CallbackTask INSTANCE = new CallbackTask();

    public static CallbackTask getInstance() {
        return INSTANCE;
    }

    public static void pushCallBack(CallbackParam callbackParam) {
        getInstance().callBackQueue.add(callbackParam);
        log.info(MessageUtil.format("pushCallBack", "callbackParam", callbackParam));
    }

    public void start() {
        THREAD_POOL_EXECUTOR.execute(() -> {
            while (true) {
                if (JvmStatus.JVM_CLOSE_FLAG && HttpServerHandler.THREAD_POOL_EXECUTOR.getActiveCount() <= 0) {
                    log.info("jvm closing job callback break");
                    break;
                }

                try {
                    CallbackParam callbackParam = callBackQueue.poll(3L, TimeUnit.SECONDS);
                    if (Objects.isNull(callbackParam)) {
                        continue;
                    }

                    List<CallbackParam> callbackParamList = new ArrayList<>();
                    int count = callBackQueue.drainTo(callbackParamList, 10);
                    callbackParamList.add(callbackParam);

                    callback(callbackParamList);
                } catch (Exception e) {
                    log.error(MessageUtil.format("callback error", e));
                }
            }

            while (callBackQueue.size() > 0) {
                List<CallbackParam> callbackParamList = new ArrayList<>();
                int count = callBackQueue.drainTo(callbackParamList, 10);

                callback(callbackParamList);
            }
        });
    }

    private void callback(List<CallbackParam> callbackParamList) {
        if (CollectionUtil.isEmpty(callbackParamList)) {
            return;
        }
    }
}
