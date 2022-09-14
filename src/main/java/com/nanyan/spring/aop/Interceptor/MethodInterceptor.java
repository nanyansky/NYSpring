package com.nanyan.spring.aop.Interceptor;



/**
 * @author nanyan
 * @date 2022/9/14 17:42
 */
public interface MethodInterceptor {
    Object invoke(MethodInvocation mi) throws Exception;
}
