package com.nanyan.spring.aop.proxy;

import com.nanyan.spring.aop.AdvisedSupport;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author nanyan
 * @date 2022/9/14 17:02
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private Class targetClass;

    private Object target;

    private AdvisedSupport config;

    public JdkDynamicAopProxy(AdvisedSupport config) {
        this.config = config;
    }


    @Override
    public Object getProxy() {
        return getProxy(this.config.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        this.target = this.config.getTarget();
        this.targetClass = this.config.getTargetClass();
        return Proxy.newProxyInstance(classLoader,this.config.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        return null;
    }
}
