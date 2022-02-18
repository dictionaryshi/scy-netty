package com.scy.netty.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.scy.core.ObjectUtil;
import com.scy.core.format.MessageUtil;
import com.scy.netty.handler.CodeHandler;
import com.scy.netty.handler.ExceptionHandler;
import com.scy.netty.handler.NettyIdleStateHandler;
import com.scy.netty.protocol.DecodeSpliter;
import com.scy.netty.server.handler.HeartBeatRequestHandler;
import com.scy.netty.server.handler.LoginRequestHandler;
import com.scy.netty.server.handler.PermissionAuditHandler;
import com.scy.netty.server.handler.ServerHandlers;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    private volatile ServerBootstrap serverBootstrap;

    private volatile ServerConfig serverConfig;

    private volatile boolean closed = Boolean.FALSE;

    @Override
    public void start(ServerConfig serverConfig) {
        if (closed) {
            return;
        }

        this.serverConfig = serverConfig;

        bossGroup = new NioEventLoopGroup(new ThreadFactoryBuilder().setNameFormat("boosGroup-thread-pool-%d").build());
        workerGroup = new NioEventLoopGroup(new ThreadFactoryBuilder().setNameFormat("workerGroup-thread-pool-%d").build());

        serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 511)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    public void initChannel(NioSocketChannel nioSocketChannel) {
                        // 空闲检测
                        nioSocketChannel.pipeline().addLast(new NettyIdleStateHandler());
                        nioSocketChannel.pipeline().addLast(new DecodeSpliter());
                        nioSocketChannel.pipeline().addLast(CodeHandler.INSTANCE);
                        nioSocketChannel.pipeline().addLast(HeartBeatRequestHandler.INSTANCE);
                        nioSocketChannel.pipeline().addLast(LoginRequestHandler.INSTANCE);
                        nioSocketChannel.pipeline().addLast(PermissionAuditHandler.INSTANCE);
                        nioSocketChannel.pipeline().addLast(ServerHandlers.INSTANCE);
                        nioSocketChannel.pipeline().addLast(ExceptionHandler.INSTANCE);
                    }
                });

        bind();
    }

    public void bind() {
        if (closed) {
            return;
        }

        serverBootstrap.bind(serverConfig.getPort()).addListener((GenericFutureListener<ChannelFuture>) future -> {
            if (future.isSuccess()) {
                log.info(MessageUtil.format("NettyServer start success", "port", serverConfig.getPort()));
                onStarted();
            } else {
                log.error(MessageUtil.format("NettyServer start fail", future.cause(), "port", serverConfig.getPort()));
                future.channel().eventLoop().schedule(this::bind, 5, TimeUnit.SECONDS);
            }
        });
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