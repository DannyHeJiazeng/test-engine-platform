package com.test.web.controller;

import com.test.core.TestNGRunner;
import com.test.core.TestResult;
import com.test.middleware.QualityGate;
import com.test.web.dto.ApiResponse;
import com.test.web.dto.TestExecutionRequest;
import com.test.web.dto.TestExecutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试执行控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test Execution", description = "测试执行相关接口")
public class TestExecutionController {

    private final TestNGRunner testNGRunner;
    private final QualityGate qualityGate;
    private final ConcurrentHashMap<String, TestExecutionResponse> executionStore = new ConcurrentHashMap<>();

    /**
     * 执行测试
     */
    @PostMapping("/run")
    @Operation(summary = "执行测试", description = "根据配置执行测试用例")
    public ApiResponse<TestExecutionResponse> runTest(@RequestBody TestExecutionRequest request) {
        log.info("Received test execution request: {}", request);

        try {
            List<TestResult> results;

            if (request.getTestClass() != null && !request.getTestClass().isEmpty()) {
                // 执行指定测试类
                Class<?> testClass = Class.forName(request.getTestClass());
                results = testNGRunner.runTestClass(testClass);
            } else if (request.getPackagePath() != null && !request.getPackagePath().isEmpty()) {
                // 执行指定包下的测试
                results = testNGRunner.runSuite(request.getPackagePath(), request.getGroups());
            } else {
                return ApiResponse.error("Invalid request: must specify testClass or packagePath");
            }

            // 执行质量门禁检查
            QualityGate.QualityGateResult gateResult = null;
            if (request.isCheckQualityGate()) {
                QualityGate.QualityThreshold threshold = request.getThreshold() != null
                        ? request.getThreshold()
                        : QualityGate.QualityThreshold.defaultThreshold();
                gateResult = qualityGate.check(results, threshold);
            }

            TestExecutionResponse response = new TestExecutionResponse();
            response.setExecutionId(generateExecutionId());
            response.setTotalTests(results.size());
            response.setResults(results);
            response.setQualityGateResult(gateResult);

            // 保存执行结果
            executionStore.put(response.getExecutionId(), response);

            return ApiResponse.success(response);

        } catch (ClassNotFoundException e) {
            log.error("Test class not found", e);
            return ApiResponse.error("Test class not found: " + request.getTestClass());
        } catch (Exception e) {
            log.error("Failed to execute test", e);
            return ApiResponse.error("Failed to execute test: " + e.getMessage());
        }
    }

    /**
     * 异步执行测试
     */
    @PostMapping("/run-async")
    @Operation(summary = "异步执行测试", description = "异步执行测试用例，立即返回执行ID")
    public ApiResponse<String> runTestAsync(@RequestBody TestExecutionRequest request) {
        log.info("Received async test execution request: {}", request);

        String executionId = generateExecutionId();

        // 初始化执行状态
        TestExecutionResponse response = new TestExecutionResponse();
        response.setExecutionId(executionId);
        response.setStatus("RUNNING");
        executionStore.put(executionId, response);

        // 异步执行
        CompletableFuture.runAsync(() -> {
            try {
                List<TestResult> results = testNGRunner.runSuite(
                        request.getPackagePath(),
                        request.getGroups()
                );

                response.setTotalTests(results.size());
                response.setResults(results);
                response.setStatus("COMPLETED");

                if (request.isCheckQualityGate()) {
                    QualityGate.QualityGateResult gateResult = qualityGate.check(results);
                    response.setQualityGateResult(gateResult);
                }

            } catch (Exception e) {
                log.error("Async test execution failed", e);
                response.setStatus("FAILED");
                response.setErrorMessage(e.getMessage());
            }
        });

        return ApiResponse.success(executionId, "Test execution started");
    }

    /**
     * 执行功能测试
     */
    @PostMapping("/run/functional")
    @Operation(summary = "执行功能测试")
    public ApiResponse<TestExecutionResponse> runFunctionalTests(@RequestParam String packagePath) {
        log.info("Running functional tests: {}", packagePath);

        try {
            List<TestResult> results = testNGRunner.runFunctionalTests(packagePath);

            TestExecutionResponse response = new TestExecutionResponse();
            response.setExecutionId(generateExecutionId());
            response.setTotalTests(results.size());
            response.setResults(results);

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Failed to run functional tests", e);
            return ApiResponse.error("Failed to run functional tests: " + e.getMessage());
        }
    }

    /**
     * 执行性能测试
     */
    @PostMapping("/run/performance")
    @Operation(summary = "执行性能测试")
    public ApiResponse<TestExecutionResponse> runPerformanceTests(@RequestParam String packagePath) {
        log.info("Running performance tests: {}", packagePath);

        try {
            List<TestResult> results = testNGRunner.runPerformanceTests(packagePath);

            TestExecutionResponse response = new TestExecutionResponse();
            response.setExecutionId(generateExecutionId());
            response.setTotalTests(results.size());
            response.setResults(results);

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Failed to run performance tests", e);
            return ApiResponse.error("Failed to run performance tests: " + e.getMessage());
        }
    }

    /**
     * 执行巡检测试
     */
    @PostMapping("/run/inspection")
    @Operation(summary = "执行巡检测试")
    public ApiResponse<TestExecutionResponse> runInspectionTests(@RequestParam String packagePath) {
        log.info("Running inspection tests: {}", packagePath);

        try {
            List<TestResult> results = testNGRunner.runInspectionTests(packagePath);

            TestExecutionResponse response = new TestExecutionResponse();
            response.setExecutionId(generateExecutionId());
            response.setTotalTests(results.size());
            response.setResults(results);

            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Failed to run inspection tests", e);
            return ApiResponse.error("Failed to run inspection tests: " + e.getMessage());
        }
    }

    /**
     * 查询执行状态
     */
    @GetMapping("/status/{executionId}")
    @Operation(summary = "查询执行状态")
    public ApiResponse<TestExecutionResponse> getExecutionStatus(@PathVariable String executionId) {
        TestExecutionResponse response = executionStore.get(executionId);

        if (response == null) {
            return ApiResponse.error("Execution not found: " + executionId);
        }

        return ApiResponse.success(response);
    }

    /**
     * 获取执行历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取执行历史")
    public ApiResponse<List<TestExecutionResponse>> getExecutionHistory() {
        return ApiResponse.success(List.copyOf(executionStore.values()));
    }

    private String generateExecutionId() {
        return "EXEC-" + System.currentTimeMillis();
    }
}