package com.scy.netty.client.handler;

import com.scy.core.format.MessageUtil;
import com.scy.netty.model.LoginResponsePacket;
import com.scy.netty.model.Session;
import com.scy.netty.util.SessionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : shichunyang
 * Date    : 2022/2/15
 * Time    : 2:02 下午
 * ---------------------------------------
 * Desc    : LoginResponseHandler
 */
@Slf4j
@ChannelHandler.Sharable
public class LoginResponseHandler extends SimpleChannelInboundHandler<LoginResponsePacket> {

    public static final LoginResponseHandler INSTANCE = new LoginResponseHandler();

    private LoginResponseHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, LoginResponsePacket loginResponsePacket) throws Exception {
        if (loginResponsePacket.isSuccess()) {
            Session session = new Session();
            session.setUserId(loginResponsePacket.getServerIp());
            SessionUtil.bindSession(ctx.channel(), session);
        } else {
            log.error(MessageUtil.format("登陆失败", "reason", loginResponsePacket.getReason()));
        }
    }
}
