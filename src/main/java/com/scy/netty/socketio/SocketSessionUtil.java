package com.scy.netty.socketio;

import com.corundumstudio.socketio.SocketIOClient;
import com.scy.core.StringUtil;
import com.scy.core.format.MessageUtil;
import com.scy.netty.model.Session;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : shichunyang
 * Date    : 2022/3/29
 * Time    : 2:27 下午
 * ---------------------------------------
 * Desc    : SocketSessionUtil
 */
@Slf4j
public class SocketSessionUtil {

    private static final Map<String, SocketIOClient> USER_ID_CHANNEL_MAP = new ConcurrentHashMap<>();

    public static final String SESSION = "session";

    /**
     * 绑定session
     */
    public static boolean bindSession(SocketIOClient channel, Session session) {
        if (Objects.isNull(channel) || Objects.isNull(session) || StringUtil.isEmpty(session.getUserId())) {
            log.error(MessageUtil.format("bindSession error", "session", session, "channel", channel));
            return Boolean.FALSE;
        }

        USER_ID_CHANNEL_MAP.put(session.getUserId(), channel);

        channel.set(SESSION, session);

        log.info(MessageUtil.format("bindSession success", "session", session, "onlineNumber", USER_ID_CHANNEL_MAP.size()));

        return Boolean.TRUE;
    }

    public static boolean unBindSession(SocketIOClient channel) {
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

            channel.del(SESSION);

            log.info(MessageUtil.format("unBindSession success", "session", session, "onlineNumber", USER_ID_CHANNEL_MAP.size()));
        }

        return Boolean.TRUE;
    }

    /**
     * 判断是否已经登陆
     */
    public static boolean hasLogin(SocketIOClient channel) {
        return Objects.nonNull(getSession(channel));
    }

    /**
     * 获取Session
     */
    @Nullable
    public static Session getSession(SocketIOClient channel) {
        if (Objects.isNull(channel)) {
            return null;
        }

        return channel.get(SESSION);
    }

    /**
     * 获取Channel
     */
    @Nullable
    public static SocketIOClient getChannel(String userId) {
        if (StringUtil.isEmpty(userId)) {
            return null;
        }

        return USER_ID_CHANNEL_MAP.get(userId);
    }
}
