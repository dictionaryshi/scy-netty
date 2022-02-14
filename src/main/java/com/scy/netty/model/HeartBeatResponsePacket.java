package com.scy.netty.model;

import com.scy.netty.constant.NettyConstant;
import com.scy.netty.protocol.AbstractPacket;

public class HeartBeatResponsePacket extends AbstractPacket {

    public static final HeartBeatResponsePacket INSTANCE = new HeartBeatResponsePacket();

    @Override
    public int getCommand() {
        return NettyConstant.HEARTBEAT_RESPONSE;
    }
}
