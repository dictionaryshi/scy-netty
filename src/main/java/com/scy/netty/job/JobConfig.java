package com.scy.netty.job;

import com.scy.core.CollectionUtil;
import com.scy.core.spring.ApplicationContextUtil;
import com.scy.netty.job.annotation.Job;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author : shichunyang
 * Date    : 2022/4/24
 * Time    : 4:01 下午
 * ---------------------------------------
 * Desc    : JobConfig
 */
public class JobConfig implements SmartInitializingSingleton {

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = ApplicationContextUtil.getApplicationContext().getBeanNamesForType(Object.class, Boolean.FALSE, Boolean.TRUE);
        Stream.of(beanNames).forEach(beanName -> {
            Object bean = ApplicationContextUtil.getBean(beanName, null);

            Map<Method, Job> jobMethodMap = MethodIntrospector.selectMethods(bean.getClass(),
                    (MethodIntrospector.MetadataLookup<Job>) method -> AnnotatedElementUtils.findMergedAnnotation(method, Job.class));
            if (CollectionUtil.isEmpty(jobMethodMap)) {
                return;
            }

            jobMethodMap.forEach((method, job) -> register(bean, method, job));
        });
    }

    private void register(Object bean, Method method, Job job) {
    }
}
