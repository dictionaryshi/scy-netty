package com.scy.netty.model;

import com.scy.netty.protocol.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.scy.netty.constant.NettyConstant.LOGIN_REQUEST;

@Getter
@Setter
@ToString
public class LoginRequestPacket extends AbstractPacket {

    private String clientIp;

    private Long timestamp;

    private String token;

    @Override
    public int getCommand() {
        return LOGIN_REQUEST;
    }
}
