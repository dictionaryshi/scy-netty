package com.scy.netty.rpc.provider.annotation;

import java.lang.annotation.*;

/**
 * @author : shichunyang
 * Date    : 2022/2/18
 * Time    : 1:10 下午
 * ---------------------------------------
 * Desc    : RpcService
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {

    /**
     * 版本
     */
    String version();

    /**
     * 核心线程数
     */
    int corePoolSize();

    /**
     * 最大线程数
     */
    int maximumPoolSize();

    /**
     * 队列数
     */
    int queueSize();
}
