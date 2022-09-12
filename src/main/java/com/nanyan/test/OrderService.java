package com.nanyan.test;

import com.nanyan.spring.annotation.NYComponent;

/**
 * @author nanyan
 * @date 2022/9/11 12:55
 */
@NYComponent
public class OrderService {

    public String name;

    public void setName(String name) {
        this.name = name;
    }
}
