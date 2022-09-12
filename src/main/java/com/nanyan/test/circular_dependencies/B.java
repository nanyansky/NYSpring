package com.nanyan.test.circular_dependencies;

import com.nanyan.spring.annotation.NYAutowired;
import com.nanyan.spring.annotation.NYComponent;
import com.nanyan.spring.annotation.NYScope;

/**
 * @author nanyan
 * @date 2022/9/12 14:47
 */
@NYComponent
//@NYScope("prototype")
public class B {
    public String Bname;

    @NYAutowired
    A a;

    public void run()
    {
        a.setAName("I am A!");
        System.out.println(a.AName);
    }

    public void setBName(String name) {
        this.Bname = name;
    }
}
