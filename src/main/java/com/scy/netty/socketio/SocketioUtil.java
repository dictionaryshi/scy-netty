package com.scy.netty.socketio;

import com.corundumstudio.socketio.AckMode;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DefaultExceptionListener;
import com.scy.core.StringUtil;
import com.scy.core.format.MessageUtil;
import com.scy.netty.model.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketioUtil {

    public static SocketIOServer getServer() {
        Configuration config = new Configuration();
        config.setHostname("wxjj.com");
        // 设置心跳超时时间
        config.setPingTimeout(60000);
        config.setContext("/socket.io");
        // 使用直接缓冲区
        config.setPreferDirectBuffer(Boolean.TRUE);
        // 配置每次握手时调用的授权监听器
        config.setAuthorizationListener(handshakeData -> {
            String token = SocketCookieUtil.getCookieValue(handshakeData, "SCY_SSO");
            if (StringUtil.isEmpty(token)) {
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }
            return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
        });
        // 配置传输升级过程中的超时时间
        config.setUpgradeTimeout(10000);
        // 非linux环境不使用linux epoll
        config.setUseLinuxNativeEpoll(Boolean.FALSE);
        // “沉默通道”攻击是一种网络攻击，攻击者打开连接但不发送任何数据，目的是耗尽服务器资源。
        config.setFirstDataTimeout(5000);
        // 启用HTTP压缩
        config.setHttpCompression(Boolean.TRUE);
        // 启用WebSocket压缩
        config.setWebsocketCompression(Boolean.TRUE);
        // 异常监听器
        config.setExceptionListener(new DefaultExceptionListener());
        // 设置心跳检测的时间间隔
        config.setPingInterval(25000);
        config.setPort(9092);
        // 服务器应答模式
        config.setAckMode(AckMode.AUTO_SUCCESS_ONLY);
        // HTTP请求内容的最大长度
        config.setMaxHttpContentLength(5 * 1024 * 1024);
        // WebSocket帧的最大有效载荷长度
        config.setMaxFramePayloadLength(5 * 1024 * 1024);

        // 设置是否禁用 Nagle 算法。如果设置为 true，表示禁用，可以减少数据包的延迟，适用于小包或需要低延迟的传输。通常，对于需要即时性的应用，建议设置为 true。
        config.getSocketConfig().setTcpNoDelay(Boolean.TRUE);
        // 设置是否启用 TCP 的 keepalive 属性。如果设置为 true，TCP 会定期发送探测包以检测连接是否仍然有效。这有助于在网络中断时快速发现死连接。通常，对于需要维持长连接的应用，建议设置为 true。
        config.getSocketConfig().setTcpKeepAlive(Boolean.TRUE);
        // 设置是否允许重用套接字地址。如果设置为 true，即使前一个连接还在 TIME_WAIT 状态，也允许新的套接字绑定相同的地址。这对于服务器应用来说很有用，可以快速重启绑定到相同端口。
        config.getSocketConfig().setReuseAddress(Boolean.TRUE);
        // 设置套接字接受连接请求的队列长度。如果队列满了，新的连接请求可能会被拒绝。常用值取决于应用的并发需求，但511是一个常见的设置，为了兼容性和性能。
        config.getSocketConfig().setAcceptBackLog(511);

        SocketIOServer server = new SocketIOServer(config);

        server.addPingListener(client -> {
            Session session = SocketSessionUtil.getSession(client);
            log.info(MessageUtil.format("client ping", "session", session));
        });

        server.addConnectListener(client -> {
            String token = SocketCookieUtil.getCookieValue(client.getHandshakeData(), "SCY_SSO");
            if (StringUtil.isEmpty(token)) {
                client.disconnect();
                return;
            }

            SocketSessionUtil.bindSession(client, new Session(token));
        });

        server.addDisconnectListener(client -> SocketSessionUtil.unBindSession(client));

        server.addEventInterceptor((client, eventName, args, ackRequest)
                -> log.info(MessageUtil.format("eventInterceptor", "eventName", eventName, "args", args)));

        return server;
    }
}
