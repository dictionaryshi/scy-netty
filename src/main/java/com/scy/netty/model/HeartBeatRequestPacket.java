package com.scy.netty.model;

import com.scy.netty.constant.NettyConstant;
import com.scy.netty.protocol.AbstractPacket;

public class HeartBeatRequestPacket extends AbstractPacket {

    public static final HeartBeatRequestPacket INSTANCE = new HeartBeatRequestPacket();

    @Override
    public int getCommand() {
        return NettyConstant.HEARTBEAT_REQUEST;
    }
}
