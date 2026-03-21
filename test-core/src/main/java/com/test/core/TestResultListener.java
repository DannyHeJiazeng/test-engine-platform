package com.test.core;

import lombok.Builder;
import lombok.Data;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TestNG 结果监听器
 * 用于收集测试执行结果
 */
public class TestResultListener extends TestListenerAdapter {

    private final Map<String, TestResult> results = new ConcurrentHashMap<>();
    private final List<TestResult> resultList = new ArrayList<>();

    @Override
    public void onTestSuccess(ITestResult result) {
        TestResult testResult = TestResult.success(
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                result.getEndMillis() - result.getStartMillis()
        );
        testResult.setTestId(generateTestId(result));
        testResult.setDescription(getAnnotationDescription(result));
        results.put(testResult.getTestId(), testResult);
        resultList.add(testResult);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestResult testResult = TestResult.failure(
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown failure",
                result.getThrowable() != null ? getStackTrace(result.getThrowable()) : "",
                result.getEndMillis() - result.getStartMillis()
        );
        testResult.setTestId(generateTestId(result));
        testResult.setDescription(getAnnotationDescription(result));
        results.put(testResult.getTestId(), testResult);
        resultList.add(testResult);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Throwable skipCause = result.getThrowable();
        TestResult testResult = TestResult.builder()
                .testId(generateTestId(result))
                .className(result.getTestClass().getName())
                .methodName(result.getMethod().getMethodName())
                .status(TestResult.TestStatus.SKIP)
                .failureMessage(skipCause != null ? skipCause.getMessage() : "Skipped")
                .durationMs(result.getEndMillis() - result.getStartMillis())
                .build();
        results.put(testResult.getTestId(), testResult);
        resultList.add(testResult);
    }

    private String generateTestId(ITestResult result) {
        return result.getTestClass().getName() + "#" + result.getMethod().getMethodName();
    }

    private String getAnnotationDescription(ITestResult result) {
        // 从方法获取注解描述
        try {
            java.lang.reflect.Method method = result.getMethod().getConstructorOrMethod().getMethod();

            // 检查 FunctionalTest 注解
            if (method.isAnnotationPresent(com.test.annotation.FunctionalTest.class)) {
                return method.getAnnotation(com.test.annotation.FunctionalTest.class).description();
            }

            // 检查 PerformanceTest 注解
            if (method.isAnnotationPresent(com.test.annotation.PerformanceTest.class)) {
                return method.getAnnotation(com.test.annotation.PerformanceTest.class).description();
            }

            // 检查 Inspection 注解
            if (method.isAnnotationPresent(com.test.annotation.Inspection.class)) {
                return method.getAnnotation(com.test.annotation.Inspection.class).description();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取所有测试结果
     */
    public List<TestResult> getResults() {
        return new ArrayList<>(resultList);
    }

    /**
     * 获取成功的测试结果
     */
    public List<TestResult> getSuccessResults() {
        List<TestResult> successResults = new ArrayList<>();
        for (TestResult result : resultList) {
            if (result.getStatus() == TestResult.TestStatus.SUCCESS) {
                successResults.add(result);
            }
        }
        return successResults;
    }

    /**
     * 获取失败的测试结果
     */
    public List<TestResult> getFailureResults() {
        List<TestResult> failureResults = new ArrayList<>();
        for (TestResult result : resultList) {
            if (result.getStatus() == TestResult.TestStatus.FAILURE) {
                failureResults.add(result);
            }
        }
        return failureResults;
    }

    /**
     * 获取测试统计
     */
    public TestStatistics getStatistics() {
        int total = resultList.size();
        int success = getSuccessResults().size();
        int failure = getFailureResults().size();
        int skip = total - success - failure;

        return TestStatistics.builder()
                .total(total)
                .success(success)
                .failure(failure)
                .skip(skip)
                .passRate(total > 0 ? (double) success / total : 0.0)
                .build();
    }

    @Data
    @Builder
    public static class TestStatistics {
        private int total;
        private int success;
        private int failure;
        private int skip;
        private double passRate;
    }
}
