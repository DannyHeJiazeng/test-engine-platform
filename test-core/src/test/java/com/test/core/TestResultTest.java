package com.test.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TestResult 单元测试
 */
@DisplayName("TestResult 测试")
class TestResultTest {

    @Test
    @DisplayName("创建成功的测试结果")
    void testCreateSuccessResult() {
        // When
        TestResult result = TestResult.success("com.example.TestClass", "testMethod", 100);

        // Then
        assertThat(result.getClassName()).isEqualTo("com.example.TestClass");
        assertThat(result.getMethodName()).isEqualTo("testMethod");
        assertThat(result.getStatus()).isEqualTo(TestResult.TestStatus.SUCCESS);
        assertThat(result.getDurationMs()).isEqualTo(100);
    }

    @Test
    @DisplayName("创建失败的测试结果")
    void testCreateFailureResult() {
        // When
        TestResult result = TestResult.failure(
                "com.example.TestClass",
                "testMethod",
                "测试失败",
                "stack trace",
                200
        );

        // Then
        assertThat(result.getClassName()).isEqualTo("com.example.TestClass");
        assertThat(result.getMethodName()).isEqualTo("testMethod");
        assertThat(result.getStatus()).isEqualTo(TestResult.TestStatus.FAILURE);
        assertThat(result.getFailureMessage()).isEqualTo("测试失败");
        assertThat(result.getStackTrace()).isEqualTo("stack trace");
        assertThat(result.getDurationMs()).isEqualTo(200);
    }

    @Test
    @DisplayName("创建异常的测试结果")
    void testCreateErrorResult() {
        // Given
        RuntimeException exception = new RuntimeException("运行时错误");

        // When
        TestResult result = TestResult.error("com.example.TestClass", "testMethod", exception, 50);

        // Then
        assertThat(result.getStatus()).isEqualTo(TestResult.TestStatus.FAILURE);
        assertThat(result.getFailureMessage()).isEqualTo("运行时错误");
        assertThat(result.getStackTrace()).contains("RuntimeException");
    }

    @Test
    @DisplayName("测试 Builder 模式")
    void testBuilderPattern() {
        // When
        TestResult result = TestResult.builder()
                .testId("TEST-001")
                .className("com.example.TestClass")
                .methodName("testMethod")
                .status(TestResult.TestStatus.SUCCESS)
                .description("测试描述")
                .durationMs(100)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .testType("functional")
                .priority("P0")
                .build();

        // Then
        assertThat(result.getTestId()).isEqualTo("TEST-001");
        assertThat(result.getDescription()).isEqualTo("测试描述");
        assertThat(result.getTestType()).isEqualTo("functional");
        assertThat(result.getPriority()).isEqualTo("P0");
    }
}