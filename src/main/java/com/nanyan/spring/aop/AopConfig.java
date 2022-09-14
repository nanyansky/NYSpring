package com.nanyan.spring.aop;

/**
 * @author nanyan
 * @date 2022/9/14 17:44
 */
public class AopConfig {
    private String pointCut;
    private String before;
    private String afterReturn;
    private String afterThrow;
    private String afterThrowClass;
    private String aspectClass;

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfterReturn() {
        return afterReturn;
    }

    public void setAfterReturn(String afterReturn) {
        this.afterReturn = afterReturn;
    }

    public String getAfterThrow() {
        return afterThrow;
    }

    public void setAfterThrow(String afterThrow) {
        this.afterThrow = afterThrow;
    }

    public String getAfterThrowClass() {
        return afterThrowClass;
    }

    public void setAfterThrowClass(String afterThrowClass) {
        this.afterThrowClass = afterThrowClass;
    }

    public String getAspectClass() {
        return aspectClass;
    }

    public void setAspectClass(String aspectClass) {
        this.aspectClass = aspectClass;
    }

    public String getPointCut() {
        return pointCut;
    }

    public void setPointCut(String pointCut) {
        this.pointCut = pointCut;
    }
}
