package com.nanyan.test;

import com.nanyan.spring.NanYanApplicationContext;
import com.nanyan.spring.annotation.NYAutowired;

/**
 * @author nanyan
 * @date 2022/9/11 11:56
 */
public class Test {
    public static void main(String[] args) throws Exception {

        NanYanApplicationContext nanYanApplicationContext = new NanYanApplicationContext(AppConfig.class);

        UserService userService = (UserService) nanYanApplicationContext.getBean("userService");
//        System.out.println(userService.orderService);
//        System.out.println(userService.beanName);
//        System.out.println(userService.name);
//        System.out.println(nanYanApplicationContext.getBean("userService"));

        userService.run();
        userService.close();

    }
}
