package com.nanyan.spring.aop.aspect;

import com.nanyan.spring.aop.Interceptor.MethodInterceptor;
import com.nanyan.spring.aop.Interceptor.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author nanyan
 * @date 2022/9/14 18:11
 */
public class AfterThrowingAdvice extends AbstractAspectJAdvice implements Advice, MethodInterceptor {

    private String throwingName;
    private MethodInvocation mi;

    public AfterThrowingAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }
    public void setThrowingName(String name) {
        this.throwingName = name;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Exception {
        try {
            return mi.proceed();
        } catch (Throwable ex) {
            super.invokeAdviceMethod(mi, null, ex.getCause());
            throw ex;
        }
    }


}
