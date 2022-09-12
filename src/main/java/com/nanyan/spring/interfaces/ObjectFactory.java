package com.nanyan.spring.interfaces;

/**
 * @author nanyan
 * @date 2022/9/12 16:27
 */
//保证接口只有一个抽象方法
//@FunctionalInterface
public interface ObjectFactory<T> {
    T getObject() throws RuntimeException;
}
