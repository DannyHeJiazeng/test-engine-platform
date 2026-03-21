package com.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 性能测试注解
 * 用于标记性能测试用例，支持并发测试和响应时间验证
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceTest {

    /**
     * 测试描述
     */
    String description() default "";

    /**
     * 最大允许执行时间（毫秒）
     */
    long maxDurationMs() default 5000;

    /**
     * 并发数（用于并发测试）
     */
    int concurrency() default 1;

    /**
     * 并发持续时间（秒）
     */
    int durationSeconds() default 60;

    /**
     * 最大允许错误率（0.0 - 1.0）
     */
    double maxErrorRate() default 0.01;

    /**
     * TestNG group 名称，默认为 performance
     */
    String group() default "performance";
}
