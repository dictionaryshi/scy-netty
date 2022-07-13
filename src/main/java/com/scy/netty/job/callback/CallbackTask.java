package com.scy.netty.job.callback;

import com.scy.core.*;
import com.scy.core.enums.JvmStatus;
import com.scy.core.format.MessageUtil;
import com.scy.core.json.JsonUtil;
import com.scy.core.net.HttpOptions;
import com.scy.core.net.HttpParam;
import com.scy.core.net.HttpUtil;
import com.scy.core.rest.ResponseResult;
import com.scy.core.thread.ThreadPoolUtil;
import com.scy.core.thread.ThreadUtil;
import com.scy.netty.job.util.JobLogUtil;
import com.scy.netty.server.http.HttpServerHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;

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

    private static final TypeReference<List<CallbackParam>> CALL_BACK_PARAM_TYPE_REFERENCE = new TypeReference<List<CallbackParam>>() {
    };

    private Thread retryFailCallbackFileThread = null;
    private Thread registryThread = null;

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
                    Optional.ofNullable(retryFailCallbackFileThread).ifPresent(Thread::interrupt);
                    Optional.ofNullable(registryThread).ifPresent(Thread::interrupt);
                    break;
                }

                try {
                    CallbackParam callbackParam = callBackQueue.poll(3L, TimeUnit.SECONDS);
                    if (Objects.isNull(callbackParam)) {
                        continue;
                    }

                    List<CallbackParam> callbackParamList = new ArrayList<>();
                    int count = callBackQueue.drainTo(callbackParamList, 9);
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

        THREAD_POOL_EXECUTOR.execute(() -> {
            retryFailCallbackFileThread = Thread.currentThread();

            while (!JvmStatus.JVM_CLOSE_FLAG) {
                ThreadUtil.quietSleep(30_000);

                retryFailCallbackFile();
            }
        });
    }

    public void startRegistry(String appName, String address) {
        THREAD_POOL_EXECUTOR.execute(() -> {
            registryThread = Thread.currentThread();

            while (!JvmStatus.JVM_CLOSE_FLAG) {
                Map<String, Object> params = new HashMap<>();
                params.put("appName", appName);
                params.put("address", address);
                ResponseResult<?> responseResult = HttpUtil.post("http://127.0.0.1:9000/job/registry", params, new TypeReference<ResponseResult<?>>() {
                }, HttpOptions.build());

                ThreadUtil.quietSleep(30_000);
            }

            removeRegistry(appName, address);
        });
    }

    public void removeRegistry(String appName, String address) {
        Map<String, Object> params = new HashMap<>();
        params.put("appName", appName);
        params.put("address", address);
        ResponseResult<?> responseResult = HttpUtil.post("http://127.0.0.1:9000/job/registryRemove", params, new TypeReference<ResponseResult<?>>() {
        }, HttpOptions.build());
    }

    private void retryFailCallbackFile() {
        File callbackLogPath = JobLogUtil.getCallbackFile().getParentFile();
        if (!callbackLogPath.exists()) {
            return;
        }

        if (callbackLogPath.isFile()) {
            callbackLogPath.delete();
            return;
        }

        File[] files = callbackLogPath.listFiles();
        if (Objects.isNull(files)) {
            return;
        }

        for (File callbackFile : files) {
            try {
                String callbackParamJson = IOUtil.readFileToString(callbackFile, SystemUtil.CHARSET_UTF_8_STR);
                if (StringUtil.isEmpty(callbackParamJson)) {
                    callbackFile.delete();
                    continue;
                }

                List<CallbackParam> callbackParamList = JsonUtil.json2Object(callbackParamJson, CALL_BACK_PARAM_TYPE_REFERENCE);

                callbackFile.delete();

                callback(callbackParamList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void callback(List<CallbackParam> callbackParamList) {
        if (CollectionUtil.isEmpty(callbackParamList)) {
            return;
        }

        boolean flag = push(callbackParamList);
        if (flag) {
            return;
        }

        appendFailCallbackFile(callbackParamList);
    }

    private void appendFailCallbackFile(List<CallbackParam> callbackParamList) {
        String callbackParamJson = JsonUtil.object2Json(callbackParamList);
        if (StringUtil.isEmpty(callbackParamJson)) {
            return;
        }

        File callbackFile = JobLogUtil.getCallbackFile();
        if (callbackFile.exists()) {
            for (int i = 0; i < 1000; i++) {
                callbackFile = new File(JobLogUtil.getCallbackFile().getAbsolutePath().concat("-").concat(String.valueOf(i)));
                if (!callbackFile.exists()) {
                    break;
                }
            }
        }

        try {
            IOUtil.writeStringToFile(callbackFile, callbackParamJson, SystemUtil.CHARSET_UTF_8_STR, Boolean.FALSE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean push(List<CallbackParam> callbackParamList) {
        HttpParam httpParam = new HttpParam();
        httpParam.setRequestUrl("http://127.0.0.1:9000/job/callback");
        httpParam.setRequestMethod(HttpUtil.POST);
        httpParam.setRequestBody(JsonUtil.object2Json(callbackParamList));
        httpParam.setHttpOptions(HttpOptions.build().contentType(HttpUtil.APPLICATION_JSON_VALUE));
        ResponseResult<Boolean> responseResult = HttpUtil.httpRequest(httpParam, new TypeReference<ResponseResult<Boolean>>() {
        });
        return Optional.ofNullable(responseResult).map(ResponseResult::getData).orElse(Boolean.FALSE);
    }
}
