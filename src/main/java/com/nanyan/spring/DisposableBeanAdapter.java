package com.nanyan.spring;

import com.nanyan.spring.interfaces.DisposableBean;

/**
 * @author nanyan
 * @date 2022/9/12 22:24
 * 用于适配各种销毁方法的适配器
 */
public class DisposableBeanAdapter implements DisposableBean {
    private Object bean;
    private String beanName;
    private BeanDefinition beanDefinition;

    //构造器传参
    public DisposableBeanAdapter(Object bean, String beanName, BeanDefinition beanDefinition) {
        this.bean = bean;
        this.beanName = beanName;
        this.beanDefinition = beanDefinition;
    }

    //判断是否需要销毁
    public static boolean hasDestroyMethod(Object bean, BeanDefinition beanDefinition) {
        if (bean instanceof DisposableBean || bean instanceof AutoCloseable) {
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        try {
            if (bean instanceof DisposableBean) {
                ((DisposableBean) bean).destroy();
            } else if (bean instanceof AutoCloseable) {
                ((AutoCloseable) bean).close();
            }
        } catch (Exception e) {
            System.out.println("Invocation of destroy method failed on bean with name '" + this.beanName + "'");
        }
    }
}
