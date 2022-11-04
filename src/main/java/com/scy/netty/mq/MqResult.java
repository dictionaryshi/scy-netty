package com.scy.netty.mq;

import com.scy.core.StringUtil;
import lombok.*;

/**
 * @author : shichunyang
 * Date    : 2022/11/4
 * Time    : 2:44 下午
 * ---------------------------------------
 * Desc    : MqResult
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MqResult {

    public static final MqResult SUCCESS = new MqResult(Boolean.TRUE, StringUtil.EMPTY);

    public static final MqResult FAIL = new MqResult(Boolean.FALSE, StringUtil.EMPTY);

    private boolean success;

    private String content;
}
