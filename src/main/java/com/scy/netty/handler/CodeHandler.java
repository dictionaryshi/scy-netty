package com.scy.netty.handler;

import com.scy.netty.protocol.AbstractPacket;
import com.scy.netty.protocol.PacketCodeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * @author : shichunyang
 * Date    : 2022/2/14
 * Time    : 7:09 下午
 * ---------------------------------------
 * Desc    : CodeHandler
 */
public class CodeHandler extends MessageToMessageCodec<ByteBuf, AbstractPacket> {

    public static final CodeHandler INSTANCE = new CodeHandler();

    private CodeHandler() {
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        out.add(PacketCodeUtil.decode(byteBuf));
    }

    @Override
    public void encode(ChannelHandlerContext ctx, AbstractPacket packet, List<Object> out) {
        ByteBuf byteBuf = ctx.channel().alloc().ioBuffer();

        PacketCodeUtil.encode(byteBuf, packet);

        out.add(byteBuf);
    }
}
