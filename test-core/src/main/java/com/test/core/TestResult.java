package com.test.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试结果封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

    /**
     * 测试 ID
     */
    private String testId;

    /**
     * 测试类名
     */
    private String className;

    /**
     * 测试方法名
     */
    private String methodName;

    /**
     * 测试状态：SUCCESS, FAILURE, SKIP
     */
    private TestStatus status;

    /**
     * 测试描述
     */
    private String description;

    /**
     * 失败消息
     */
    private String failureMessage;

    /**
     * 堆栈跟踪
     */
    private String stackTrace;

    /**
     * 执行时间（毫秒）
     */
    private long durationMs;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 测试类型：functional, performance, inspection
     */
    private String testType;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 标签
     */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * 创建成功的测试结果
     */
    public static TestResult success(String className, String methodName, long durationMs) {
        return TestResult.builder()
                .className(className)
                .methodName(methodName)
                .status(TestStatus.SUCCESS)
                .durationMs(durationMs)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败的测试结果
     */
    public static TestResult failure(String className, String methodName, String failureMessage,
                                      String stackTrace, long durationMs) {
        return TestResult.builder()
                .className(className)
                .methodName(methodName)
                .status(TestStatus.FAILURE)
                .failureMessage(failureMessage)
                .stackTrace(stackTrace)
                .durationMs(durationMs)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建异常的测试结果
     */
    public static TestResult error(String className, String methodName, Throwable throwable, long durationMs) {
        return TestResult.builder()
                .className(className)
                .methodName(methodName)
                .status(TestStatus.FAILURE)
                .failureMessage(throwable.getMessage())
                .stackTrace(getStackTraceAsString(throwable))
                .durationMs(durationMs)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }

    private static String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    public enum TestStatus {
        SUCCESS,
        FAILURE,
        SKIP
    }
}
