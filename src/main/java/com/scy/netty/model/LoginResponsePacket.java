package com.scy.netty.model;

import com.scy.netty.protocol.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.scy.netty.constant.NettyConstant.LOGIN_RESPONSE;

@Getter
@Setter
@ToString
public class LoginResponsePacket extends AbstractPacket {

    private String serverIp;

    private boolean success;

    private String reason;

    @Override
    public int getCommand() {
        return LOGIN_RESPONSE;
    }
}
