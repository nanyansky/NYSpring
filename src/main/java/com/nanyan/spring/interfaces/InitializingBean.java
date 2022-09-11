package com.nanyan.spring.interfaces;

/**
 * @author nanyan
 * @date 2022/9/11 22:17
 */
public interface InitializingBean {
    //初始化时
    void afterPropertiesSet();
}
