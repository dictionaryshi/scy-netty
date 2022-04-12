package com.scy.netty.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : shichunyang
 * Date    : 2022/4/12
 * Time    : 12:04 下午
 * ---------------------------------------
 * Desc    : HttpServerHandler
 */
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());

        HttpMethod httpMethod = fullHttpRequest.method();

        HttpHeaders headers = fullHttpRequest.headers();

        String requestData = fullHttpRequest.content().toString(CharsetUtil.UTF_8);

        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
    }
}
