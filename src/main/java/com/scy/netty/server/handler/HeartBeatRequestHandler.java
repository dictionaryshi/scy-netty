package com.scy.netty.server.handler;

import com.scy.netty.constant.NettyConstant;
import com.scy.netty.model.HeartBeatRequestPacket;
import com.scy.netty.model.HeartBeatResponsePacket;
import com.scy.netty.util.NettyUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class HeartBeatRequestHandler extends SimpleChannelInboundHandler<HeartBeatRequestPacket> {

    public static final HeartBeatRequestHandler INSTANCE = new HeartBeatRequestHandler();

    private HeartBeatRequestHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HeartBeatRequestPacket requestPacket) {
        NettyUtil.setAttr(ctx.channel(), NettyConstant.LAST_READ_TIME, System.currentTimeMillis());

        ctx.writeAndFlush(HeartBeatResponsePacket.INSTANCE);
    }
}
