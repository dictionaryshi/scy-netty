package com.scy.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : shichunyang
 * Date    : 2022/2/14
 * Time    : 5:40 下午
 * ---------------------------------------
 * Desc    : DecodeSpliter
 */
@Slf4j
public class DecodeSpliter extends LengthFieldBasedFrameDecoder {

    private static final int LENGTH_FIELD_OFFSET = 5;

    private static final int LENGTH_FIELD_LENGTH = 4;

    public DecodeSpliter() {
        super(Integer.MAX_VALUE, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in.getInt(in.readerIndex()) != PacketCodeUtil.MAGIC_NUMBER) {
            log.error("DecodeSpliter 非法请求, 关闭连接");
            ctx.channel().close();
            return null;
        }
        return super.decode(ctx, in);
    }
}
