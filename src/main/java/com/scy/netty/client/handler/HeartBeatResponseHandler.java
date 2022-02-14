package com.scy.netty.client.handler;

import com.scy.netty.constant.NettyConstant;
import com.scy.netty.model.HeartBeatResponsePacket;
import com.scy.netty.util.NettyUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class HeartBeatResponseHandler extends SimpleChannelInboundHandler<HeartBeatResponsePacket> {

    public static final HeartBeatResponseHandler INSTANCE = new HeartBeatResponseHandler();

    private HeartBeatResponseHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HeartBeatResponsePacket heartBeatResponsePacket) {
        NettyUtil.setAttr(ctx.channel(), NettyConstant.LAST_READ_TIME, System.currentTimeMillis());
    }
}
