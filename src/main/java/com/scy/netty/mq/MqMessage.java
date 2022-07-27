package com.scy.netty.mq;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : shichunyang
 * Date    : 2022/7/27
 * Time    : 2:38 下午
 * ---------------------------------------
 * Desc    : MqMessage
 */
@Getter
@Setter
@ToString
public class MqMessage implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 主题
     */
    private String topic;

    /**
     * 分组
     */
    private String group;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 分片id
     */
    private Long shardingId;

    /**
     * 超时
     */
    private Integer timeout;

    /**
     * 生效时间
     */
    private Long effectTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 数据
     */
    private String data;

    /**
     * 日志
     */
    private String log;
}
