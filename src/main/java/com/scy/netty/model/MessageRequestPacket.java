package com.scy.netty.model;

import com.scy.netty.protocol.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.scy.netty.constant.NettyConstant.MESSAGE_REQUEST;

@Getter
@Setter
@ToString
public class MessageRequestPacket extends AbstractPacket {

    private String toUserId;

    private String message;

    @Override
    public int getCommand() {
        return MESSAGE_REQUEST;
    }
}
