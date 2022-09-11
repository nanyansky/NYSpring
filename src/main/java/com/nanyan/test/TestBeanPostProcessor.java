package com.nanyan.test;

import com.nanyan.spring.annotation.NYComponent;
import com.nanyan.spring.interfaces.BeanPostProcessor;

/**
 * @author nanyan
 * @date 2022/9/11 23:03
 */

@NYComponent
public class TestBeanPostProcessor implements BeanPostProcessor {


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前...");
        if(beanName.equals("userService"))
        {
            ((UserService)bean).setName("HELLO!");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后...");
        return bean;
    }
}
