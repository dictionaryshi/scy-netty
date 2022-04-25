package com.scy.netty.server.http;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.scy.core.ObjectUtil;
import com.scy.core.format.MessageUtil;
import com.scy.netty.handler.ExceptionHandler;
import com.scy.netty.handler.NettyIdleStateHandler;
import com.scy.netty.server.AbstractServer;
import com.scy.netty.server.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author : shichunyang
 * Date    : 2022/2/17
 * Time    : 12:06 下午
 * ---------------------------------------
 * Desc    : NettyHttpServer
 */
@Slf4j
public class NettyHttpServer extends AbstractServer {

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
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .option(ChannelOption.SO_BACKLOG, 511)
                .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    public void initChannel(NioSocketChannel nioSocketChannel) {
                        // 空闲检测
                        nioSocketChannel.pipeline().addLast("NettyIdleStateHandler", new NettyIdleStateHandler());
                        nioSocketChannel.pipeline().addLast("HttpServerCodec", new HttpServerCodec());
                        nioSocketChannel.pipeline().addLast("HttpObjectAggregator", new HttpObjectAggregator(5 * 1024 * 1024));
                        nioSocketChannel.pipeline().addLast("HttpContentCompressor", new HttpContentCompressor());
                        nioSocketChannel.pipeline().addLast("HttpServerHandler", HttpServerHandler.INSTANCE);
                        nioSocketChannel.pipeline().addLast("ExceptionHandler", ExceptionHandler.INSTANCE);
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
