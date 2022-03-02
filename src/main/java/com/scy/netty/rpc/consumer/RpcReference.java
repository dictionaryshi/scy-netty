package com.scy.netty.rpc.consumer;

import com.scy.core.StringUtil;

import java.lang.annotation.*;

/**
 * @author : shichunyang
 * Date    : 2022/3/2
 * Time    : 4:10 下午
 * ---------------------------------------
 * Desc    : RpcReference
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {

    /**
     * 版本
     */
    String version();

    /**
     * 超时
     */
    long timeout();

    /**
     * 直连地址
     */
    String address() default StringUtil.EMPTY;
}
