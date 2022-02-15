package com.scy.netty.server.handler;

import com.scy.core.format.MessageUtil;
import com.scy.core.net.NetworkInterfaceUtil;
import com.scy.netty.model.LoginRequestPacket;
import com.scy.netty.model.LoginResponsePacket;
import com.scy.netty.model.Session;
import com.scy.netty.util.NettyUtil;
import com.scy.netty.util.SessionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : shichunyang
 * Date    : 2022/2/15
 * Time    : 2:27 下午
 * ---------------------------------------
 * Desc    : LoginRequestHandler
 */
@Slf4j
@ChannelHandler.Sharable
public class LoginRequestHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {

    public static final LoginRequestHandler INSTANCE = new LoginRequestHandler();

    private LoginRequestHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket loginRequestPacket) throws Exception {
        LoginResponsePacket loginResponsePacket = new LoginResponsePacket();

        if (valid(loginRequestPacket)) {
            loginResponsePacket.setServerIp(NetworkInterfaceUtil.getIp());
            loginResponsePacket.setSuccess(Boolean.TRUE);

            Session session = new Session();
            session.setUserId(loginRequestPacket.getClientIp());
            SessionUtil.bindSession(ctx.channel(), session);
        } else {
            loginResponsePacket.setReason("请求校验失败");
            loginResponsePacket.setSuccess(Boolean.FALSE);
            log.info(MessageUtil.format("登陆失败", "loginRequestPacket", loginRequestPacket));
        }

        NettyUtil.pushMsg(ctx, loginResponsePacket);
    }

    private boolean valid(LoginRequestPacket loginRequestPacket) {
        return true;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        boolean flag = SessionUtil.unBindSession(ctx.channel());
        if (flag) {
            ctx.channel().close();
        }
    }
}
