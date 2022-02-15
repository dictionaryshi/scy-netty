package com.scy.netty.model;

import com.scy.netty.protocol.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.scy.netty.constant.NettyConstant.LOGOUT_RESPONSE;

@Getter
@Setter
@ToString
public class LogoutResponsePacket extends AbstractPacket {

    private boolean success;

    private String reason;


    @Override
    public int getCommand() {
        return LOGOUT_RESPONSE;
    }
}
