package com.scy.netty.util;

import com.scy.core.format.MessageUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author : shichunyang
 * Date    : 2022/2/14
 * Time    : 11:45 上午
 * ---------------------------------------
 * Desc    : NettyUtil
 */
@Slf4j
public class NettyUtil {

    private NettyUtil() {
    }

    public static void pushMsg(ChannelHandlerContext ctx, Object msg) {
        ChannelFuture channelFuture = ctx.writeAndFlush(msg);
        channelFuture.addListener(future -> {
            if (future.isDone()) {
                log.info(MessageUtil.format("server push", "msg", msg));
            }
        });
    }

    public static void sendMsg(String fromUserId, String toUserId, Object msg) {
        Channel fromChannel = SessionUtil.getChannel(fromUserId);
        Channel toChannel = SessionUtil.getChannel(toUserId);
        if (Objects.isNull(fromChannel) || Objects.isNull(toChannel)) {
            log.info(MessageUtil.format("sendMsg fail", "fromUserId", fromUserId, "toUserId", toUserId));
            return;
        }

        ChannelFuture channelFuture = toChannel.writeAndFlush(msg);
        channelFuture.addListener(future -> {
            if (future.isDone()) {
                log.info(MessageUtil.format("send msg",
                        "from", SessionUtil.getSession(fromChannel), "to", SessionUtil.getSession(toChannel), "msg", msg));
            }
        });
    }

    public static <T> void setAttr(Channel channel, AttributeKey<T> attributeKey, T value) {
        channel.attr(attributeKey).set(value);
    }

    @Nullable
    public static <T> T getAttr(Channel channel, AttributeKey<T> attributeKey) {
        return channel.attr(attributeKey).get();
    }
}
