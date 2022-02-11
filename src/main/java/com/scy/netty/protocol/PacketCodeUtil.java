package com.scy.netty.protocol;

import com.scy.core.ObjectUtil;
import com.scy.core.exception.BusinessException;
import com.scy.core.format.MessageUtil;
import com.scy.core.net.HessianUtil;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * @author : shichunyang
 * Date    : 2022/2/11
 * Time    : 4:18 下午
 * ---------------------------------------
 * Desc    : PacketCodeUtil
 */
public class PacketCodeUtil {

    private PacketCodeUtil() {
    }

    public static final int MAGIC_NUMBER = 0x12345678;

    public static final byte SERIALIZER_HESSIAN = 1;

    public static void encode(ByteBuf byteBuf, AbstractPacket packet) {
        byteBuf.writeInt(MAGIC_NUMBER);

        byteBuf.writeByte(SERIALIZER_HESSIAN);

        byte[] bytes = HessianUtil.serialize(packet);
        if (ObjectUtil.isNull(bytes)) {
            throw new BusinessException("PacketCodeUtil encode error");
        }

        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    public static AbstractPacket decode(ByteBuf byteBuf) {
        // 跳过 magic number
        byteBuf.skipBytes(4);

        // 序列化算法
        byte serializeAlgorithm = byteBuf.readByte();

        // 数据包长度
        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        if (Objects.equals(serializeAlgorithm, SERIALIZER_HESSIAN)) {
            return HessianUtil.deserialize(bytes);
        }

        throw new BusinessException(MessageUtil.format("PacketCodeUtil decode error", "serializeAlgorithm", serializeAlgorithm));
    }
}
