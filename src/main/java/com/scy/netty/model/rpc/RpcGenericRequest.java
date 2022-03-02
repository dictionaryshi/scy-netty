package com.scy.netty.model.rpc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author : shichunyang
 * Date    : 2022/3/2
 * Time    : 5:57 下午
 * ---------------------------------------
 * Desc    : RpcGenericRequest
 */
@Getter
@Setter
@ToString
public class RpcGenericRequest {

    private String className;

    private String methodName;

    private String[] parameterTypes;

    private Object[] parameters;

    private String version;
}
