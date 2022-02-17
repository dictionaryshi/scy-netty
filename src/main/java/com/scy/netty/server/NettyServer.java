package com.scy.netty.server;

import com.scy.core.ObjectUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author : shichunyang
 * Date    : 2022/2/17
 * Time    : 12:06 下午
 * ---------------------------------------
 * Desc    : NettyServer
 */
@Slf4j
public class NettyServer extends AbstractServer {

    private volatile EventLoopGroup bossGroup;

    private volatile EventLoopGroup workerGroup;

    private volatile ServerBootstrap bootstrap;

    private volatile ServerConfig serverConfig;

    private volatile boolean closed = Boolean.FALSE;

    @Override
    public void start(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void stopBossGroup() {
        closed = Boolean.TRUE;

        beforeStop();

        if (!ObjectUtil.isNull(bossGroup)) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

    @Override
    public void stopWorkerGroup() {
        closed = Boolean.TRUE;

        if (!Objects.isNull(workerGroup)) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
