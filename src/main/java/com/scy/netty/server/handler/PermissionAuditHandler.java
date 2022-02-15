package com.scy.netty.server.handler;

import com.scy.netty.util.SessionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : shichunyang
 * Date    : 2022/2/15
 * Time    : 1:37 下午
 * ---------------------------------------
 * Desc    : PermissionAuditHandler
 */
@Slf4j
@ChannelHandler.Sharable
public class PermissionAuditHandler extends ChannelInboundHandlerAdapter {

    public static final PermissionAuditHandler INSTANCE = new PermissionAuditHandler();

    private PermissionAuditHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!SessionUtil.hasLogin(ctx.channel())) {
            log.info("权限校验失败, 关闭连接");
            ctx.channel().close();
        } else {
            ctx.pipeline().remove(this);
            super.channelRead(ctx, msg);
        }
    }
}
