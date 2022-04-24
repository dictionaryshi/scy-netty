package com.scy.netty.job;

import com.scy.core.ArrayUtil;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author : shichunyang
 * Date    : 2022/4/24
 * Time    : 5:38 下午
 * ---------------------------------------
 * Desc    : MethodJobHandler
 */
public class MethodJobHandler implements JobHandler {

    private final Object target;

    private final Method initMethod;

    private final Method method;

    private final Method destroyMethod;

    public MethodJobHandler(Object target, Method initMethod, Method method, Method destroyMethod) {
        this.target = target;

        this.initMethod = initMethod;

        this.method = method;

        this.destroyMethod = destroyMethod;
    }

    @Override
    public void init() throws Exception {
        if (Objects.nonNull(initMethod)) {
            initMethod.invoke(target);
        }
    }

    @Override
    public void execute() throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (!ArrayUtil.isEmpty(parameterTypes)) {
            method.invoke(target, new Object[parameterTypes.length]);
        } else {
            method.invoke(target);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(destroyMethod)) {
            destroyMethod.invoke(target);
        }
    }
}
