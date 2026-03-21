package com.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 巡检用例注解
 * 用于标记线上巡检测试用例，支持定时执行和失败告警
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inspection {

    /**
     * 测试描述
     */
    String description() default "";

    /**
     * 执行间隔：5m, 30m, 1h, 6h, 24h
     */
    String interval() default "5m";

    /**
     * 失败时是否发送告警
     */
    boolean alertOnFailure() default true;

    /**
     * 告警级别：INFO, WARNING, CRITICAL
     */
    String alertLevel() default "WARNING";

    /**
     * TestNG group 名称，默认为 inspection
     */
    String group() default "inspection";
}
