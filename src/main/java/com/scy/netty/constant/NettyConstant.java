package com.scy.netty.constant;

import com.scy.netty.model.Session;
import io.netty.util.AttributeKey;

/**
 * @author : shichunyang
 * Date    : 2022/2/13
 * Time    : 11:15 下午
 * ---------------------------------------
 * Desc    : NettyConstant
 */
public class NettyConstant {

    public static final int HEARTBEAT_REQUEST = 1;

    public static final int HEARTBEAT_RESPONSE = 2;

    public static final AttributeKey<Session> SESSION = AttributeKey.newInstance("session");

    public static final AttributeKey<Long> LAST_READ_TIME = AttributeKey.newInstance("lastReadTime");
}
