package com.scy.netty.protocol;

import java.io.Serializable;

/**
 * @author : shichunyang
 * Date    : 2022/2/11
 * Time    : 4:08 下午
 * ---------------------------------------
 * Desc    : AbstractPacket
 */
public abstract class AbstractPacket implements Serializable {

    /**
     * 命令
     */
    public abstract int getCommand();
}
