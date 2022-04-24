package com.scy.netty.job;

/**
 * @author : shichunyang
 * Date    : 2022/4/24
 * Time    : 5:20 下午
 * ---------------------------------------
 * Desc    : JobHandler
 */
public interface JobHandler {

    void init() throws Exception;

    void execute() throws Exception;

    void destroy() throws Exception;
}
