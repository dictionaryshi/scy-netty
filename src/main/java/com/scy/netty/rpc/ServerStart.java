package com.scy.netty.rpc;

import com.scy.core.StringUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.net.NetworkInterfaceUtil;
import com.scy.netty.server.NettyServer;
import com.scy.netty.server.ServerConfig;
import lombok.Getter;

/**
 * @author : shichunyang
 * Date    : 2022/2/21
 * Time    : 11:36 下午
 * ---------------------------------------
 * Desc    : ServerStart
 */
@Getter
public class ServerStart {

    private final NettyServer nettyServer;

    private int port = 7080;

    private String ip;

    public ServerStart() {
        nettyServer = new NettyServer();

        ip = NetworkInterfaceUtil.getIp();
        if (StringUtil.isEmpty(ip)) {
            throw new BusinessException("ServerStart error 获取ip失败");
        }

        // TODO
        nettyServer.setStartedCallback(() -> {
        });

        nettyServer.setBeforeStopCallback(() -> {
        });

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(port);

        nettyServer.start(serverConfig);
    }
}
