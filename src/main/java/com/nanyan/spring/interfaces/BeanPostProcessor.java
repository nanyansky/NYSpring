package com.nanyan.spring.interfaces;

/**
 * @author nanyan
 * @date 2022/9/11 22:26
 * 此处可以在 Bean 初始化前后进行一些操作，用于增强或扩展 Bean
 */

public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);
}
