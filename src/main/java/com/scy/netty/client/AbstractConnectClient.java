package com.scy.netty.client;

import com.scy.core.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author : shichunyang
 * Date    : 2022/2/7
 * Time    : 5:29 下午
 * ---------------------------------------
 * Desc    : AbstractConnectClient
 */
@Slf4j
public abstract class AbstractConnectClient {

    private static final ConcurrentMap<String, Object> CONNECT_CLIENT_LOCK_MAP = new ConcurrentHashMap<>();

    private static volatile ConcurrentMap<String, AbstractConnectClient> CONNECT_CLIENT_MAP;

    public abstract void init(String address, ClientConfig clientConfig) throws Exception;

    public abstract void close();

    public abstract boolean isValidate();

    public abstract void send(Object data) throws Exception;

    public static void asyncSend(String address, ClientConfig clientConfig, Object data) throws Exception {
        AbstractConnectClient clientPool = AbstractConnectClient.getPool(address, clientConfig);

        clientPool.send(data);
    }

    private static AbstractConnectClient getPool(String address, ClientConfig clientConfig) throws Exception {
        if (ObjectUtil.isNull(CONNECT_CLIENT_MAP)) {
            synchronized (AbstractConnectClient.class) {
                if (ObjectUtil.isNull(CONNECT_CLIENT_MAP)) {
                    CONNECT_CLIENT_MAP = new ConcurrentHashMap<>();
                    clientConfig.addBeforeStopCallback(AbstractConnectClient::closeConnectClient);
                }
            }
        }

        AbstractConnectClient connectClient = CONNECT_CLIENT_MAP.get(address);
        if (!ObjectUtil.isNull(connectClient) && connectClient.isValidate()) {
            return connectClient;
        }

        Object clientLock = CONNECT_CLIENT_LOCK_MAP.get(address);
        if (ObjectUtil.isNull(clientLock)) {
            CONNECT_CLIENT_LOCK_MAP.putIfAbsent(address, new Object());
            clientLock = CONNECT_CLIENT_LOCK_MAP.get(address);
        }

        synchronized (clientLock) {
            connectClient = CONNECT_CLIENT_MAP.get(address);
            if (!ObjectUtil.isNull(connectClient) && connectClient.isValidate()) {
                return connectClient;
            }

            if (!ObjectUtil.isNull(connectClient)) {
                connectClient.close();
                CONNECT_CLIENT_MAP.remove(address);
            }

            AbstractConnectClient connectClientNew = clientConfig.getConnectClientClass().newInstance();
            connectClientNew.init(address, clientConfig);
            CONNECT_CLIENT_MAP.put(address, connectClientNew);

            return connectClientNew;
        }
    }

    public static void closeConnectClient() {
        if (CONNECT_CLIENT_MAP.size() <= 0) {
            return;
        }

        CONNECT_CLIENT_MAP.forEach((address, connectClient) -> connectClient.close());

        CONNECT_CLIENT_MAP.clear();
    }
}
