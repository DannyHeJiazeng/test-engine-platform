package com.test.core;

import com.test.annotation.TestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 测试上下文
 * 用于封装测试执行的上下文信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestContext {

    /**
     * 测试类
     */
    private Class<?> testClass;

    /**
     * 测试方法
     */
    private Method testMethod;

    /**
     * 测试类型
     */
    private TestType testType;

    /**
     * TestNG Group
     */
    private String group;

    /**
     * 测试描述
     */
    private String description;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 超时时间（毫秒）
     */
    private long timeoutMs;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 当前执行环境
     */
    private Environment environment;

    /**
     * 自定义参数
     */
    private Map<String, Object> parameters;

    public enum Environment {
        DEV,       // 开发环境
        TEST,      // 测试环境
        STAGING,   // 预发布环境
        PROD       // 生产环境
    }

    /**
     * 从方法创建测试上下文
     */
    public static TestContext fromMethod(Class<?> testClass, Method method, AnnotationParser parser) {
        TestType testType = parser.parseType(method);
        String group = parser.parseGroup(method);
        String description = parser.parseDescription(method);
        Optional<String> priority = parser.parsePriority(method);
        Optional<String[]> tags = parser.parseTags(method);

        // 从@Test 注解获取超时和启用状态
        Test testAnnotation = method.getAnnotation(Test.class);
        boolean enabled = testAnnotation != null && testAnnotation.enabled();
        long timeoutMs = testAnnotation != null ? testAnnotation.timeOut() : 0;

        return TestContext.builder()
                .testClass(testClass)
                .testMethod(method)
                .testType(testType)
                .group(group)
                .description(description)
                .priority(priority.orElse("P1"))
                .tags(tags.map(Arrays::asList).orElse(new ArrayList<>()))
                .enabled(enabled)
                .timeoutMs(timeoutMs)
                .retryCount(0)
                .environment(Environment.TEST)
                .parameters(new HashMap<>())
                .build();
    }

    /**
     * 获取测试唯一标识
     */
    public String getTestId() {
        return testClass.getName() + "#" + testMethod.getName();
    }

    /**
     * 复制并修改上下文
     */
    public TestContext copyWith(Map<String, Object> newParameters) {
        TestContext copy = new TestContext();
        copy.setTestClass(this.testClass);
        copy.setTestMethod(this.testMethod);
        copy.setTestType(this.testType);
        copy.setGroup(this.group);
        copy.setDescription(this.description);
        copy.setPriority(this.priority);
        copy.setTags(this.tags);
        copy.setEnabled(this.enabled);
        copy.setTimeoutMs(this.timeoutMs);
        copy.setRetryCount(this.retryCount);
        copy.setEnvironment(this.environment);
        copy.setParameters(newParameters != null ? new HashMap<>(newParameters) : new HashMap<>());
        return copy;
    }
}
