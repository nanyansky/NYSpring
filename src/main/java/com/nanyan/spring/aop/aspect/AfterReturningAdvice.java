package com.nanyan.spring.aop.aspect;

import com.nanyan.spring.aop.Interceptor.MethodInterceptor;
import com.nanyan.spring.aop.Interceptor.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author nanyan
 * @date 2022/9/14 18:09
 */
public class AfterReturningAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {
    public AfterReturningAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Exception {
        Object returnValue = mi.proceed();
        invokeAdviceMethod(mi, returnValue, null);
        return returnValue;
    }
}
