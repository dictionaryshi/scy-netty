package com.scy.netty.job.annotation;

import com.scy.core.StringUtil;

import java.lang.annotation.*;

/**
 * @author : shichunyang
 * Date    : 2022/4/24
 * Time    : 4:05 下午
 * ---------------------------------------
 * Desc    : Job
 */
@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Job {

    String value();

    String init() default StringUtil.EMPTY;

    String destroy() default StringUtil.EMPTY;
}
