package com.nanyan.spring.interfaces;

import com.nanyan.spring.NanYanApplicationContext;

/**
 * @author nanyan
 * @date 2022/9/11 19:20
 */
public interface ApplicationContextAware {
    void setApplicationContext(NanYanApplicationContext applicationContext);
}
