package com.scy.netty.rpc;

import com.scy.core.StringUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.net.NetworkInterfaceUtil;
import com.scy.netty.rpc.provider.Provider;
import com.scy.netty.server.NettyServer;
import com.scy.netty.server.ServerConfig;
import com.scy.zookeeper.config.RegisterCenter;
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

    private final RegisterCenter registerCenter;

    private int port = 7080;

    private String ip;

    private String address;

    public ServerStart(RegisterCenter registerCenter) {
        nettyServer = new NettyServer();

        this.registerCenter = registerCenter;

        ip = NetworkInterfaceUtil.getIp();
        if (StringUtil.isEmpty(ip)) {
            throw new BusinessException("ServerStart error 获取ip失败");
        }

        address = NetworkInterfaceUtil.getIpPort(ip, port);

        nettyServer.setStartedCallback(() -> registerCenter.registry(Provider.getServiceMap().keySet(), address));

        nettyServer.setBeforeStopCallback(() -> registerCenter.remove(Provider.getServiceMap().keySet(), address));

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(port);

        nettyServer.start(serverConfig);
    }
}
