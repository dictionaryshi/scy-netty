package com.scy.netty.server.handler;

import com.scy.core.StringUtil;
import com.scy.core.format.MessageUtil;
import com.scy.netty.model.MessageRequestPacket;
import com.scy.netty.model.MessageResponsePacket;
import com.scy.netty.model.Session;
import com.scy.netty.util.NettyUtil;
import com.scy.netty.util.SessionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@ChannelHandler.Sharable
public class MessageRequestHandler extends SimpleChannelInboundHandler<MessageRequestPacket> {

    public static final MessageRequestHandler INSTANCE = new MessageRequestHandler();

    private MessageRequestHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, MessageRequestPacket messageRequestPacket) throws Exception {
        // 消息发送方的session
        Session session = SessionUtil.getSession(ctx.channel());

        MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
        messageResponsePacket.setFromUserId(Objects.isNull(session) ? StringUtil.EMPTY : session.getUserId());
        messageResponsePacket.setMessage(messageRequestPacket.getMessage());

        // 消息接收方的 channel
        Channel toUserChannel = SessionUtil.getChannel(messageRequestPacket.getToUserId());

        // 将消息发送给消息接收方
        if (Objects.nonNull(toUserChannel) && SessionUtil.hasLogin(toUserChannel)) {
            NettyUtil.sendMsg(messageResponsePacket.getFromUserId(), messageRequestPacket.getToUserId(), messageResponsePacket);
        } else {
            log.info(MessageUtil.format("消息发送失败, 用户未登录", "toUserId", messageRequestPacket.getToUserId()));
        }
    }
}
