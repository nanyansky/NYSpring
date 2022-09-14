package com.nanyan.test.circular_dependencies;

import com.nanyan.spring.NanYanApplicationContext;

/**
 * @author nanyan
 * @date 2022/9/12 14:49
 */
public class Test {
    public static void main(String[] args) throws Exception {
        NanYanApplicationContext applicationContext = new NanYanApplicationContext(AppConfig.class);
        A a = (A) applicationContext.getBean("a");
        System.out.println(a);
        a.printB();
    }
}
