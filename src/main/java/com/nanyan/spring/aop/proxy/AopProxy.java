package com.nanyan.spring.aop.proxy;

/**
 * @author nanyan
 * @date 2022/9/14 17:02
 */
public interface AopProxy {
    Object getProxy();
    Object getProxy(ClassLoader classLoader);
}
