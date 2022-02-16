package com.scy.netty.client;

import com.scy.core.ObjectUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.format.NumberUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
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
public class NettyClient extends AbstractConnectClient {

    /**
     * 所有客户端公用
     */
    private static volatile EventLoopGroup workerGroup;

    private volatile boolean closed = Boolean.FALSE;

    private volatile Channel channel;

    public static final int MAX_RETRY = 3;

    @Override
    public void init(String address, ClientConfig clientConfig) throws Exception {
        if (closed) {
            return;
        }
    }

    @Override
    public void close() {
        closed = Boolean.TRUE;
        if (!ObjectUtil.isNull(this.channel)) {
            this.channel.close();
            this.channel = null;
        }
    }

    @Override
    public boolean isValidate() {
        return !ObjectUtil.isNull(this.channel) && this.channel.isActive();
    }

    @Override
    public ChannelFuture send(Object data) throws Exception {
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

    public void connect(Bootstrap bootstrap, String host, int port, int retry) {
        if (closed) {
            return;
        }

        try {
            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            log.error(MessageUtil.format("netty client connect fail", e, "host", host, "port", port));
        }

        if (Objects.equals(retry, NumberUtil.ZERO.intValue())) {
            log.info(MessageUtil.format("netty client reconnect fail", "host", host, "port", port));
            return;
        }

        int order = (MAX_RETRY - retry) + 1;
        log.info(MessageUtil.format("netty client reconnect", "host", host, "port", port, "order", order));

        // 重连间隔
        int delay = 1 << order;
        bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
    }
}
