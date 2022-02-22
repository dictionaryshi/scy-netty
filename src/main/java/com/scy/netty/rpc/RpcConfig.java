package com.scy.netty.rpc;

import com.scy.netty.rpc.provider.Provider;
import org.springframework.context.annotation.Bean;

/**
 * @author : shichunyang
 * Date    : 2022/2/18
 * Time    : 1:27 下午
 * ---------------------------------------
 * Desc    : RpcConfig
 */
public class RpcConfig {

    @Bean
    public Provider provider() {
        return new Provider();
    }

    @Bean
    public ServerStart serverStart() {
        return new ServerStart();
    }
}
