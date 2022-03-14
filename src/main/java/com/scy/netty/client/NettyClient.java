package com.scy.netty.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.scy.core.ObjectUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.format.NumberUtil;
import com.scy.core.model.UrlBO;
import com.scy.core.net.NetworkInterfaceUtil;
import com.scy.netty.client.handler.ClientHandlers;
import com.scy.netty.client.handler.HeartBeatTimerHandler;
import com.scy.netty.handler.CodeHandler;
import com.scy.netty.handler.ExceptionHandler;
import com.scy.netty.handler.NettyIdleStateHandler;
import com.scy.netty.model.LoginRequestPacket;
import com.scy.netty.protocol.DecodeSpliter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author : shichunyang
 * Date    : 2022/2/15
 * Time    : 9:06 下午
 * ---------------------------------------
 * Desc    : NettyClient
 */
@Slf4j
@Getter
public class NettyClient extends AbstractConnectClient {

    /**
     * 所有客户端公用
     */
    private static volatile EventLoopGroup workerGroup;

    private volatile Bootstrap bootstrap;

    private volatile boolean closed = Boolean.FALSE;

    private volatile Channel channel;

    private volatile String host;

    private volatile int port;

    public static final int MAX_RETRY = 2;

    private volatile int writeBufferLowWaterMark = WriteBufferWaterMark.DEFAULT.low();

    private volatile int writeBufferHighWaterMark = WriteBufferWaterMark.DEFAULT.high();

    @Override
    public void init(String address, ClientConfig clientConfig) {
        if (closed) {
            return;
        }

        UrlBO urlBO = NetworkInterfaceUtil.parseIpPort(address);
        if (ObjectUtil.isNull(urlBO)) {
            throw new BusinessException(MessageUtil.format("NettyClient init error, address解析失败", "address", address));
        }

        this.host = urlBO.getHost();
        this.port = urlBO.getPort();

        if (ObjectUtil.isNull(workerGroup)) {
            synchronized (NettyClient.class) {
                if (ObjectUtil.isNull(workerGroup)) {
                    workerGroup = new NioEventLoopGroup(new ThreadFactoryBuilder().setNameFormat("netty-client-thread-pool-%d").build());
                    clientConfig.addStopCallback(NettyClient::shutdown);
                }
            }
        }

        NettyClient nettyClient = this;

        bootstrap = new Bootstrap();
        bootstrap
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) {
                        // 空闲检测
                        socketChannel.pipeline().addLast(new NettyIdleStateHandler());
                        socketChannel.pipeline().addLast(new DecodeSpliter());
                        socketChannel.pipeline().addLast(CodeHandler.INSTANCE);
                        socketChannel.pipeline().addLast(ClientHandlers.INSTANCE);
                        // 心跳定时器
                        socketChannel.pipeline().addLast(new HeartBeatTimerHandler(nettyClient));
                        socketChannel.pipeline().addLast(ExceptionHandler.INSTANCE);
                    }
                });

        // 建立连接
        connect(NumberUtil.ZERO.intValue());
    }

    @Override
    public void close() {
        closed = Boolean.TRUE;
        if (!ObjectUtil.isNull(this.channel)) {
            this.channel.close();
        }
    }

    @Override
    public boolean isValidate() {
        return !ObjectUtil.isNull(this.channel) && this.channel.isActive() && channel.isWritable();
    }

    @Override
    public ChannelFuture send(Object data) {
        if (isValidate()) {
            return this.channel.writeAndFlush(data);
        } else {
            throw new BusinessException(MessageUtil.format("NettyClient send error, 连接不可用"));
        }
    }

    public static void shutdown() {
        if (!ObjectUtil.isNull(workerGroup)) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

    public void connect(int retry) {
        if (closed) {
            return;
        }

        try {
            channel = bootstrap.connect(host, port).sync().channel();
            channel.config().setWriteBufferLowWaterMark(writeBufferLowWaterMark);
            channel.config().setWriteBufferHighWaterMark(writeBufferHighWaterMark);
            log.info(MessageUtil.format("netty client connect success", "host", host, "port", port));
            login();
            return;
        } catch (Exception e) {
            log.error(MessageUtil.format("netty client connect fail", e, "host", host, "port", port));
        }

        if (Objects.equals(retry, NumberUtil.ZERO.intValue())) {
            String message = MessageUtil.format("netty client connect fail", "host", host, "port", port, "retry", retry);
            log.error(message);
            throw new BusinessException(message);
        }

        int order = (MAX_RETRY - retry) + 1;
        log.info(MessageUtil.format("netty client reconnect", "host", host, "port", port, "order", order));

        // 重连间隔
        int delay = 1 << order;
        bootstrap.config().group().schedule(() -> connect(retry - 1), delay, TimeUnit.SECONDS);
    }

    private void login() {
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        loginRequestPacket.setClientIp(NetworkInterfaceUtil.getIp());
        loginRequestPacket.setTimestamp(System.currentTimeMillis());
        loginRequestPacket.setToken(null);
        channel.writeAndFlush(loginRequestPacket).syncUninterruptibly();
    }
}
