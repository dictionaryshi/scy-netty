package com.scy.netty.client.handler;

import com.scy.netty.model.rpc.RpcResponse;
import com.scy.netty.rpc.consumer.Consumer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : shichunyang
 * Date    : 2022/3/4
 * Time    : 11:05 上午
 * ---------------------------------------
 * Desc    : RpcResponseHandler
 */
@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    public static final RpcResponseHandler INSTANCE = new RpcResponseHandler();

    private RpcResponseHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        Consumer.notifyRpcResponseFuture(rpcResponse);
    }
}
