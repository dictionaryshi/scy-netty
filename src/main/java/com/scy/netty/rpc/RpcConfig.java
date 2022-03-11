package com.scy.netty.rpc;

import com.scy.netty.client.ClientConfig;
import com.scy.netty.client.NettyClient;
import com.scy.netty.rpc.consumer.Consumer;
import com.scy.netty.rpc.provider.Provider;
import com.scy.zookeeper.config.RegisterCenter;
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
    public ServerStart serverStart(RegisterCenter registerCenter) {
        return new ServerStart(registerCenter);
    }

    @Bean
    public ClientConfig clientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setConnectClientClass(NettyClient.class);
        return clientConfig;
    }

    @Bean
    public Consumer consumer(ClientConfig clientConfig) {
        return new Consumer(clientConfig);
    }
}
