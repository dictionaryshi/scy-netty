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
 * Time    : 7:48 下午
 * ---------------------------------------
 * Desc    : RpcRequest
 */
@Getter
@Setter
@ToString
public class RpcRequest extends AbstractPacket implements Serializable {

    private String requestId;

    private long createTime;

    private String className;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;

    private String version;

    private String traceId;

    @Override
    public int getCommand() {
        return NettyConstant.RPC_REQUEST;
    }
}
