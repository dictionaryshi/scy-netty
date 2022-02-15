package com.scy.netty.server.handler;

import com.scy.netty.constant.NettyConstant;
import com.scy.netty.protocol.AbstractPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : shichunyang
 * Date    : 2022/2/15
 * Time    : 5:37 下午
 * ---------------------------------------
 * Desc    : ServerHandlers
 */
@ChannelHandler.Sharable
public class ServerHandlers extends SimpleChannelInboundHandler<AbstractPacket> {

    public static final ServerHandlers INSTANCE = new ServerHandlers();

    private final Map<Integer, SimpleChannelInboundHandler<? extends AbstractPacket>> handlerMap;

    private ServerHandlers() {
        handlerMap = new HashMap<>();

        handlerMap.put(NettyConstant.LOGOUT_REQUEST, LogoutRequestHandler.INSTANCE);
        handlerMap.put(NettyConstant.MESSAGE_REQUEST, MessageRequestHandler.INSTANCE);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, AbstractPacket packet) throws Exception {
        handlerMap.get(packet.getCommand()).channelRead(ctx, packet);
    }
}
