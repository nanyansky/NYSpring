package com.nanyan.spring.aop.aspect;

import java.lang.reflect.Method;

/**
 * @author nanyan
 * @date 2022/9/14 17:40
 */
public interface JoinPoint {
    /**
     * 业务方法本身
     */
    Method getMethod();

    /**
     * 该方法的参数列表
     */
    Object[] getArguments();

    /**
     * 该方法对应的对象
     */
    Object getThis();

    /**
     * 在joinPoint中添加自定义属性
     */
    void setUserAttribute(String key, Object value);

    /**
     * 获取自定义属性
     */
    Object getUserAttribute(String key);
}
