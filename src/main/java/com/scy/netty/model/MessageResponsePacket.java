package com.scy.netty.model;

import com.scy.netty.protocol.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.scy.netty.constant.NettyConstant.MESSAGE_RESPONSE;

@Getter
@Setter
@ToString
public class MessageResponsePacket extends AbstractPacket {

    private String fromUserId;

    private String message;

    @Override
    public int getCommand() {
        return MESSAGE_RESPONSE;
    }
}
