package com.scy.netty.model;

import com.scy.netty.protocol.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.scy.netty.constant.NettyConstant.LOGOUT_REQUEST;

@Getter
@Setter
@ToString
public class LogoutRequestPacket extends AbstractPacket {

    @Override
    public int getCommand() {
        return LOGOUT_REQUEST;
    }
}
