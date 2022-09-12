package com.nanyan.spring.interfaces;

/**
 * @author nanyan
 * @date 2022/9/12 22:25
 */
public interface DisposableBean {
    void destroy() throws Exception;
}
