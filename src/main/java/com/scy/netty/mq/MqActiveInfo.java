package com.scy.netty.mq;

import lombok.*;

/**
 * @author : shichunyang
 * Date    : 2022/11/7
 * Time    : 2:44 下午
 * ---------------------------------------
 * Desc    : MqActiveInfo
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MqActiveInfo {

    private int rank;

    private int total;

    private String info;
}
