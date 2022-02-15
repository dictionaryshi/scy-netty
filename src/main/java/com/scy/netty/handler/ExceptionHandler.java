package com.scy.netty.handler;

import com.scy.core.exception.ExceptionUtil;
import com.scy.core.format.MessageUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : shichunyang
 * Date    : 2022/2/15
 * Time    : 1:23 下午
 * ---------------------------------------
 * Desc    : ExceptionHandler
 */
@Slf4j
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    public static final ExceptionHandler INSTANCE = new ExceptionHandler();

    private ExceptionHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String message = cause.getMessage();
        if (ExceptionUtil.isIgnoreErrorMessage(message)) {
            log.info(message);
            return;
        }

        log.error(MessageUtil.format("exceptionCaught", cause));
    }
}
