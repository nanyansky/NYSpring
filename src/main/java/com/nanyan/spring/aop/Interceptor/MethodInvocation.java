package com.nanyan.spring.aop.Interceptor;

import com.nanyan.spring.aop.aspect.JoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nanyan
 * @date 2022/9/14 18:05
 */
public class MethodInvocation implements JoinPoint {
    /**
     * <p>代理对象</p>
     */
    private Object proxy;

    /**
     * <p>代理的目标方法</p>
     */
    private Method method;

    /**
     * <p>代理的目标对象</p>
     */
    private Object target;

    /**
     * <p>代理的目标类</p>
     */
    private Class<?> targetClass;

    /**
     * <p>代理的方法的参数列表</p>
     */
    private Object[] arguments;

    /**
     * <p>回调方法链</p>
     */
    private List<Object> interceptorsAndDynamicMethodMatchers;

    /**
     * <p>保存自定义属性</p>
     */
    private Map<String, Object> userAttributes;

    private int currentInterceptor = -1;

    public MethodInvocation(Object proxy, Method method, Object target, Class<?> targetClass, Object[] arguments,
                            List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.method = method;
        this.target = target;
        this.targetClass = targetClass;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Exception {
        // 如果执行链执行完后，执行joinPoint自己的业务逻辑方法
        if (this.currentInterceptor == this.interceptorsAndDynamicMethodMatchers.size() -1) {
            return this.method.invoke(this.target, this.arguments);
        }

        Object interceptorOrInterceptionAdvice =
                this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptor);

        // 如果该对象属于拦截器
        if (interceptorOrInterceptionAdvice instanceof MethodInterceptor) {
            MethodInterceptor mi = (MethodInterceptor) interceptorOrInterceptionAdvice;
            return mi.invoke(this);
        }

        return proceed();
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<>();
            }
            this.userAttributes.put(key, value);
        } else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return this.userAttributes != null ? this.userAttributes.get(key) : null;
    }
}
