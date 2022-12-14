package com.nanyan.spring;

/**
 * @author nanyan
 * @date 2022/9/11 13:00
 */
public class BeanDefinition {
    private Class type;
    private String scope;
    //全类名
    private String beanClassName;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isSingleton() {
        return "singleton".equals(scope);
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "type=" + type +
                ", scope='" + scope + '\'' +
                '}';
    }
}
