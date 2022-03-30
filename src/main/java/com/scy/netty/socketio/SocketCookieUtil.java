package com.scy.netty.socketio;

import com.corundumstudio.socketio.HandshakeData;
import com.scy.core.CollectionUtil;
import com.scy.core.ObjectUtil;
import com.scy.core.StringUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collection;

/**
 * @author : shichunyang
 * Date    : 2022/3/30
 * Time    : 3:23 下午
 * ---------------------------------------
 * Desc    : SocketCookieUtil
 */
public class SocketCookieUtil {

    public static String getCookieValue(HandshakeData handshakeData, String name) {
        if (ObjectUtil.isNull(handshakeData)) {
            return StringUtil.EMPTY;
        }

        HttpHeaders httpHeaders = handshakeData.getHttpHeaders();
        if (ObjectUtil.isNull(httpHeaders)) {
            return StringUtil.EMPTY;
        }

        return CollectionUtil.emptyIfNull(httpHeaders.getAll(HttpHeaderNames.COOKIE))
                .stream().map(ServerCookieDecoder.LAX::decode).flatMap(Collection::stream)
                .filter(cookie -> ObjectUtil.equals(cookie.name(), name)).findFirst().map(Cookie::value).orElse(StringUtil.EMPTY);
    }
}
