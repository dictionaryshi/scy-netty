package com.scy.netty.server.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
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
@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final HttpServerHandler INSTANCE = new HttpServerHandler();

    private HttpServerHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());

        HttpMethod httpMethod = fullHttpRequest.method();

        HttpHeaders headers = fullHttpRequest.headers();

        String requestData = fullHttpRequest.content().toString(CharsetUtil.UTF_8);

        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
    }

    private void writeResponse(ChannelHandlerContext channelHandlerContext, boolean keepAlive, String data) {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(data, CharsetUtil.UTF_8));

        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON.toString());

        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());

        if (keepAlive) {
            fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        channelHandlerContext.writeAndFlush(fullHttpResponse);
    }
}
