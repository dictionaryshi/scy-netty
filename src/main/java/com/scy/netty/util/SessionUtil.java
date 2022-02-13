package com.scy.netty.util;

import com.scy.core.StringUtil;
import com.scy.core.format.MessageUtil;
import com.scy.netty.constant.NettyConstant;
import com.scy.netty.model.Session;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SessionUtil {

    private static final Map<String, Channel> USER_ID_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 绑定session
     */
    public static boolean bindSession(Channel channel, Session session) {
        if (Objects.isNull(channel) || Objects.isNull(session) || StringUtil.isEmpty(session.getUserId())) {
            log.error(MessageUtil.format("bindSession error", "session", session, "channel", channel));
            return Boolean.FALSE;
        }

        USER_ID_CHANNEL_MAP.put(session.getUserId(), channel);

        channel.attr(NettyConstant.SESSION).set(session);

        log.info(MessageUtil.format("bindSession success", "session", session, "onlineNumber", USER_ID_CHANNEL_MAP.size()));

        return Boolean.TRUE;
    }

    public static boolean unBindSession(Channel channel) {
        if (Objects.isNull(channel)) {
            log.error(MessageUtil.format("unBindSession error", "channel", channel));
            return Boolean.FALSE;
        }

        if (hasLogin(channel)) {
            Session session = getSession(channel);
            if (Objects.isNull(session)) {
                log.error(MessageUtil.format("unBindSession error", "session", session));
                return Boolean.FALSE;
            }

            USER_ID_CHANNEL_MAP.remove(session.getUserId());

            channel.attr(NettyConstant.SESSION).set(null);

            log.info(MessageUtil.format("unBindSession success", "session", session, "onlineNumber", USER_ID_CHANNEL_MAP.size()));
        }

        return Boolean.TRUE;
    }

    /**
     * 判断是否已经登陆
     */
    public static boolean hasLogin(Channel channel) {
        return Objects.nonNull(getSession(channel));
    }

    /**
     * 获取Session
     */
    @Nullable
    public static Session getSession(Channel channel) {
        if (Objects.isNull(channel)) {
            return null;
        }

        return channel.attr(NettyConstant.SESSION).get();
    }

    /**
     * 获取Channel
     */
    @Nullable
    public static Channel getChannel(String userId) {
        if (StringUtil.isEmpty(userId)) {
            return null;
        }

        return USER_ID_CHANNEL_MAP.get(userId);
    }
}
