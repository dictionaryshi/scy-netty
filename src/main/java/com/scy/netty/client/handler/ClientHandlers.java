package com.scy.netty.client.handler;

import com.scy.netty.constant.NettyConstant;
import com.scy.netty.protocol.AbstractPacket;
import com.scy.netty.util.SessionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;

@ChannelHandler.Sharable
public class ClientHandlers extends SimpleChannelInboundHandler<AbstractPacket> {

    public static final ClientHandlers INSTANCE = new ClientHandlers();

    private final Map<Integer, SimpleChannelInboundHandler<? extends AbstractPacket>> handlerMap;

    private ClientHandlers() {
        handlerMap = new HashMap<>();

        handlerMap.put(NettyConstant.HEARTBEAT_RESPONSE, HeartBeatResponseHandler.INSTANCE);
        handlerMap.put(NettyConstant.LOGIN_RESPONSE, LoginResponseHandler.INSTANCE);
        handlerMap.put(NettyConstant.LOGOUT_RESPONSE, LogoutResponseHandler.INSTANCE);
        handlerMap.put(NettyConstant.MESSAGE_RESPONSE, MessageResponseHandler.INSTANCE);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractPacket packet) throws Exception {
        handlerMap.get(packet.getCommand()).channelRead(ctx, packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        boolean flag = SessionUtil.unBindSession(ctx.channel());
        if (flag) {
            ctx.channel().close();
        }
    }
}