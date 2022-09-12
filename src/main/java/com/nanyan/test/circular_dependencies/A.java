package com.nanyan.test.circular_dependencies;

import com.nanyan.spring.annotation.NYAutowired;
import com.nanyan.spring.annotation.NYComponent;
import com.nanyan.spring.annotation.NYScope;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author nanyan
 * @date 2022/9/12 14:47
 */
@NYComponent
//@NYScope("prototype")
public class A implements AutoCloseable {

    @NYAutowired
    public B b;

    public String AName;

    public void setAName(String AName) {
        this.AName = AName;
    }

    public void printB()
    {
        b.run();
        b.setBName("I am B!");
        System.out.println(b.Bname);
    }

    @Override
    public void close() throws Exception {
        System.out.println("开始销毁Bean...");
    }
}
