package com.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 测试套件注解
 * 用于标记测试类，定义测试套件名称和类别
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestSuite {

    /**
     * 测试套件名称
     */
    String name();

    /**
     * 测试类别
     */
    TestCategory category() default TestCategory.FUNCTIONAL;

    /**
     * 测试套件描述
     */
    String description() default "";

    /**
     * 所属业务线/模块
     */
    String businessLine() default "";

    enum TestCategory {
        FUNCTIONAL,    // 功能测试
        PERFORMANCE,   // 性能测试
        INSPECTION     // 巡检测试
    }
}
