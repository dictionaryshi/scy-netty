package com.scy.netty.model.rpc;

import com.scy.netty.constant.NettyConstant;
import com.scy.netty.protocol.AbstractPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author : shichunyang
 * Date    : 2022/2/17
 * Time    : 7:46 下午
 * ---------------------------------------
 * Desc    : RpcResponse
 */
@Getter
@Setter
@ToString
public class RpcResponse<T> extends AbstractPacket implements Serializable {

    private boolean success;

    private String requestId;

    private String errorMessage;

    private Throwable throwable;

    private T data;

    @Override
    public int getCommand() {
        return NettyConstant.RPC_RESPONSE;
    }
}
