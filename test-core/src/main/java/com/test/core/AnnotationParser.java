package com.test.core;

import com.test.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 注解解析器
 * 用于解析自定义测试注解
 */
@Slf4j
public class AnnotationParser {

    /**
     * 解析测试方法的类型
     */
    public TestType parseType(Method method) {
        if (method.isAnnotationPresent(FunctionalTest.class)) {
            return TestType.FUNCTIONAL;
        }
        if (method.isAnnotationPresent(PerformanceTest.class)) {
            return TestType.PERFORMANCE;
        }
        if (method.isAnnotationPresent(Inspection.class)) {
            return TestType.INSPECTION;
        }
        return TestType.UNKNOWN;
    }

    /**
     * 解析测试方法的 Group 名称
     */
    public String parseGroup(Method method) {
        if (method.isAnnotationPresent(FunctionalTest.class)) {
            return method.getAnnotation(FunctionalTest.class).group();
        }
        if (method.isAnnotationPresent(PerformanceTest.class)) {
            return method.getAnnotation(PerformanceTest.class).group();
        }
        if (method.isAnnotationPresent(Inspection.class)) {
            return method.getAnnotation(Inspection.class).group();
        }
        return "default";
    }

    /**
     * 解析测试描述
     */
    public String parseDescription(Method method) {
        if (method.isAnnotationPresent(FunctionalTest.class)) {
            return method.getAnnotation(FunctionalTest.class).description();
        }
        if (method.isAnnotationPresent(PerformanceTest.class)) {
            return method.getAnnotation(PerformanceTest.class).description();
        }
        if (method.isAnnotationPresent(Inspection.class)) {
            return method.getAnnotation(Inspection.class).description();
        }
        return "";
    }

    /**
     * 解析优先级
     */
    public Optional<String> parsePriority(Method method) {
        if (method.isAnnotationPresent(FunctionalTest.class)) {
            return Optional.of(method.getAnnotation(FunctionalTest.class).priority());
        }
        return Optional.empty();
    }

    /**
     * 解析标签
     */
    public Optional<String[]> parseTags(Method method) {
        if (method.isAnnotationPresent(FunctionalTest.class)) {
            return Optional.of(method.getAnnotation(FunctionalTest.class).tags());
        }
        return Optional.empty();
    }

    /**
     * 解析性能测试配置
     */
    public Optional<PerformanceConfig> parsePerformanceConfig(Method method) {
        if (!method.isAnnotationPresent(PerformanceTest.class)) {
            return Optional.empty();
        }

        PerformanceTest annotation = method.getAnnotation(PerformanceTest.class);
        PerformanceConfig config = PerformanceConfig.builder()
                .maxDurationMs(annotation.maxDurationMs())
                .concurrency(annotation.concurrency())
                .durationSeconds(annotation.durationSeconds())
                .maxErrorRate(annotation.maxErrorRate())
                .build();

        return Optional.of(config);
    }

    /**
     * 解析巡检配置
     */
    public Optional<InspectionConfig> parseInspectionConfig(Method method) {
        if (!method.isAnnotationPresent(Inspection.class)) {
            return Optional.empty();
        }

        Inspection annotation = method.getAnnotation(Inspection.class);
        InspectionConfig config = InspectionConfig.builder()
                .interval(annotation.interval())
                .alertOnFailure(annotation.alertOnFailure())
                .alertLevel(annotation.alertLevel())
                .build();

        return Optional.of(config);
    }

    /**
     * 解析测试套件
     */
    public Optional<TestSuiteInfo> parseTestSuite(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(TestSuite.class)) {
            return Optional.empty();
        }

        TestSuite annotation = clazz.getAnnotation(TestSuite.class);
        TestSuiteInfo info = TestSuiteInfo.builder()
                .name(annotation.name())
                .category(annotation.category().name())
                .description(annotation.description())
                .businessLine(annotation.businessLine())
                .build();

        return Optional.of(info);
    }

    /**
     * 检查方法是否有测试注解
     */
    public boolean hasTestAnnotation(Method method) {
        return method.isAnnotationPresent(FunctionalTest.class)
                || method.isAnnotationPresent(PerformanceTest.class)
                || method.isAnnotationPresent(Inspection.class);
    }

    /**
     * 获取方法的所有测试相关注解
     */
    public Annotation[] getTestAnnotations(Method method) {
        return method.getAnnotations();
    }

    /**
     * 性能测试配置
     */
    @lombok.Data
    @lombok.Builder
    public static class PerformanceConfig {
        private long maxDurationMs;
        private int concurrency;
        private int durationSeconds;
        private double maxErrorRate;
    }

    /**
     * 巡检配置
     */
    @lombok.Data
    @lombok.Builder
    public static class InspectionConfig {
        private String interval;
        private boolean alertOnFailure;
        private String alertLevel;
    }

    /**
     * 测试套件信息
     */
    @lombok.Data
    @lombok.Builder
    public static class TestSuiteInfo {
        private String name;
        private String category;
        private String description;
        private String businessLine;
    }
}
