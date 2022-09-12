package com.nanyan.test;

import com.nanyan.spring.annotation.NYAutowired;
import com.nanyan.spring.annotation.NYComponent;
import com.nanyan.spring.annotation.NYScope;
import com.nanyan.spring.interfaces.BeanNameAware;
import com.nanyan.spring.interfaces.InitializingBean;

/**
 * @author nanyan
 * @date 2022/9/11 12:34
 */
@NYComponent("userService")
@NYScope("singleton")
//@NYScope("prototype")

public class UserService implements BeanNameAware, InitializingBean {

    @NYAutowired
    public OrderService orderService;

    public String beanName;

    public String name;


    public void run()
    {
        orderService.setName("orderService!");
        System.out.println(orderService.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化中......");
    }

}
