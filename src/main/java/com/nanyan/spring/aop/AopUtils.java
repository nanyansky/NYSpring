package com.nanyan.spring.aop;

import com.nanyan.spring.BeanDefinition;
import com.nanyan.spring.aop.annotation.*;
import com.nanyan.spring.aop.proxy.AopProxy;
import com.nanyan.spring.aop.proxy.CglibAopProxy;
import com.nanyan.spring.aop.proxy.JdkDynamicAopProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nanyan
 * @date 2022/9/14 18:25
 */
public class AopUtils {
    //储存切面配置信息
    public static final List<AdvisedSupport> CONFIGS = new ArrayList<>();

    /**
     * 初始化AOP配置类
     */
    public static void instantiationAopConfig(Map<String, BeanDefinition> beanDefinitionMap) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
//            Class<?> clazz = beanDefinition.getType();
            Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
            // 如果该类不是切面Aspect则跳过
            if (!clazz.isAnnotationPresent(NYAspect.class)) {
                continue;
            }
            AopConfig config = new AopConfig();

            Method[] methods = clazz.getMethods();

            // 设置切点和回调方法
            for (Method method : methods) {
                if (method.isAnnotationPresent(NYPointcut.class)) {
                    // 设置切点
                    config.setPointCut(method.getAnnotation(NYPointcut.class).value());
                }
                else if (method.isAnnotationPresent(NYBefore.class)) {
                    // 前后方法
                    config.setBefore(method.getName());
                }
                else if (method.isAnnotationPresent(NYAfterReturning.class)) {
                    // 后置方法
                    config.setAfterReturn(method.getName());
                }
                else if (method.isAnnotationPresent(NYAfterThrowing.class)) {
                    // 异常方法
                    config.setAfterThrow(method.getName());
                    config.setAfterThrowClass("java.lang.Exception");
                }
            }
            // 没有设置切点，跳过
            if (config.getPointCut() == null) {
                continue;
            }
            config.setAspectClass(beanDefinition.getBeanClassName());
            CONFIGS.add(new AdvisedSupport(config));
        }
    }

    /**
     * 创建代理类
     */
    public static AopProxy createProxy(AdvisedSupport config) {
        Class targetClass = config.getTargetClass();
        // 如果实现了接口则使用jdk动态代理，否则使用Cglib代理
        if (targetClass.getInterfaces().length > 0) {
            return new JdkDynamicAopProxy(config);
        }
        // 使用CGLIB代理
        return new CglibAopProxy(config);
    }

    /**
     * 判断是否是代理类
     */
    public static boolean isAopProxy(Object object) {
        return object.getClass().getSimpleName().contains("$");
    }

    /**
     * 获取被代理的对象
     */
    public static Object getTarget(Object proxy) throws Exception {

        if(proxy.getClass().getSuperclass() == Proxy.class) {
            return getJdkDynamicProxyTargetObject(proxy);
        } else {
            return getCglibProxyTargetObject(proxy);
        }
    }

    /**
     * 获取CGLIB被代理的对象
     */
    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);

        Field config = dynamicAdvisedInterceptor.getClass().getDeclaredField("config");
        config.setAccessible(true);

        return ((AdvisedSupport) config.get(dynamicAdvisedInterceptor)).getTarget();
    }

    /**
     * 获取jdk代理被代理对象
     */
    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        Object aopProxy = h.get(proxy);

        Field config = aopProxy.getClass().getDeclaredField("config");
        config.setAccessible(true);

        return ((AdvisedSupport) config.get(aopProxy)).getTarget();
    }
}
