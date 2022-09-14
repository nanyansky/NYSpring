package com.nanyan.spring.aop.aspect;

import com.nanyan.spring.aop.Interceptor.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author nanyan
 * @date 2022/9/14 18:02
 */
public class MethodBeforeAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

    public MethodBeforeAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(com.nanyan.spring.aop.Interceptor.MethodInvocation mi) throws Exception {
        super.invokeAdviceMethod(mi, null, null);
        return mi.proceed();
    }
}
