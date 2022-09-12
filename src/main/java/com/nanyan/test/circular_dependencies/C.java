package com.nanyan.test.circular_dependencies;

import com.nanyan.spring.annotation.NYAutowired;
import com.nanyan.spring.annotation.NYComponent;
import com.nanyan.spring.annotation.NYScope;

/**
 * @author nanyan
 * @date 2022/9/12 17:16
 */
@NYComponent
@NYScope("prototype")
public class C {
    @NYAutowired
    A a;
}
