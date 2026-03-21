package com.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能测试注解
 * 用于标记功能测试用例，支持 TestNG groups
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionalTest {

    /**
     * 测试描述
     */
    String description() default "";

    /**
     * 优先级：P0, P1, P2, P3
     */
    String priority() default "P1";

    /**
     * 标签，用于分类和过滤
     */
    String[] tags() default {};

    /**
     * TestNG group 名称，默认为 functional
     */
    String group() default "functional";
}
