package com.nanyan.spring.aop.proxy;

import com.nanyan.spring.aop.AdvisedSupport;

import com.nanyan.spring.aop.Interceptor.MethodInvocation;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author nanyan
 * @date 2022/9/14 18:18
 */
public class CglibAopProxy implements AopProxy, MethodInterceptor {

    private Class targetClass;

    private Object target;

    private AdvisedSupport config;

    public CglibAopProxy(AdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.config.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        this.targetClass = this.config.getTargetClass();
        this.target = this.config.getTarget();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.targetClass);
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers = this.config.getInterceptorAndDynamicInterceptionAdvice(method, this.targetClass);
        MethodInvocation invocation = new MethodInvocation(proxy, method, this.target, this.targetClass, args, interceptorsAndDynamicMethodMatchers);
        return invocation.proceed();
    }
}