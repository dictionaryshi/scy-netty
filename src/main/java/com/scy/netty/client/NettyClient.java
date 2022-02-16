package com.scy.netty.client;

import com.scy.core.ObjectUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

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
}
