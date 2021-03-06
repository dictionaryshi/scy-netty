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

    public static final int LOGIN_REQUEST = 3;

    public static final int LOGIN_RESPONSE = 4;

    public static final int LOGOUT_REQUEST = 5;

    public static final int LOGOUT_RESPONSE = 6;

    public static final int MESSAGE_REQUEST = 7;

    public static final int MESSAGE_RESPONSE = 8;

    public static final int RPC_REQUEST = 9;

    public static final int RPC_RESPONSE = 10;

    public static final AttributeKey<Session> SESSION = AttributeKey.newInstance("session");

    public static final AttributeKey<Long> LAST_READ_TIME = AttributeKey.newInstance("lastReadTime");
}
