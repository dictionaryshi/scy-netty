package com.scy.netty.mq;

import java.lang.annotation.*;

/**
 * @author : shichunyang
 * Date    : 2022/11/3
 * Time    : 3:41 下午
 * ---------------------------------------
 * Desc    : Consumer
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Consumer {

    String DEFAULT_GROUP = "default_group";

    String topic();

    String group() default DEFAULT_GROUP;
}
