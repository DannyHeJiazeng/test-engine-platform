package com.test.web.controller;

import com.test.performance.*;
import com.test.web.dto.ApiResponse;
import com.test.web.dto.PerformanceTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Tag(name = "Performance Test", description = "性能测试相关接口")
public class PerformanceController {

    private final JMeterService jMeterService;
    private final JmxBuilder jmxBuilder;
    private final ConcurrentHashMap<String, PerformanceResult> resultStore = new ConcurrentHashMap<>();

    /**
     * 执行性能测试
     */
    @PostMapping("/run")
    @Operation(summary = "执行性能测试", description = "根据配置执行 JMeter 性能测试")
    public ApiResponse<PerformanceResult> runPerformanceTest(@RequestBody PerformanceTestRequest request) {
        log.info("Running performance test: {}", request);

        try {
            PerformanceConfig config = PerformanceConfig.builder()
                    .url(request.getUrl())
                    .method(request.getMethod())
                    .threads(request.getThreads())
                    .rampUpSeconds(request.getRampUpSeconds())
                    .durationSeconds(request.getDurationSeconds())
                    .testName(request.getTestName())
                    .testId(generateTestId())
                    .build();

            PerformanceResult result = jMeterService.runPerformanceTest(config);

            // 保存结果
            resultStore.put(result.getTestId(), result);

            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Performance test failed", e);
            return ApiResponse.error("Performance test failed: " + e.getMessage());
        }
    }

    /**
     * 执行并发测试
     */
    @PostMapping("/run/concurrency")
    @Operation(summary = "执行并发测试", description = "执行指定并发数的测试")
    public ApiResponse<PerformanceResult> runConcurrencyTest(
            @RequestParam String url,
            @RequestParam int concurrency) {

        log.info("Running concurrency test: url={}, concurrency={}", url, concurrency);

        try {
            PerformanceResult result = jMeterService.runConcurrencyTest(url, concurrency);
            resultStore.put(result.getTestId(), result);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Concurrency test failed", e);
            return ApiResponse.error("Concurrency test failed: " + e.getMessage());
        }
    }

    /**
     * 执行梯度并发测试
     */
    @PostMapping("/run/gradual")
    @Operation(summary = "执行梯度并发测试", description = "从起始并发逐步增加到最大并发")
    public ApiResponse<List<PerformanceResult>> runGradualConcurrencyTest(
            @RequestParam String url,
            @RequestParam int startConcurrency,
            @RequestParam int endConcurrency,
            @RequestParam int step) {

        log.info("Running gradual concurrency test: url={}, start={}, end={}, step={}",
                url, startConcurrency, endConcurrency, step);

        try {
            List<PerformanceResult> results = new java.util.ArrayList<>();
            for (int c = startConcurrency; c <= endConcurrency; c += step) {
                results.add(jMeterService.runConcurrencyTest(url, c));
            }
            return ApiResponse.success(results);

        } catch (Exception e) {
            log.error("Gradual concurrency test failed", e);
            return ApiResponse.error("Gradual concurrency test failed: " + e.getMessage());
        }
    }

    /**
     * 执行负载测试
     */
    @PostMapping("/run/load")
    @Operation(summary = "执行负载测试")
    public ApiResponse<PerformanceResult> runLoadTest(
            @RequestParam String url,
            @RequestParam int threads,
            @RequestParam int durationMinutes) {

        log.info("Running load test: url={}, threads={}, duration={}min",
                url, threads, durationMinutes);

        try {
            PerformanceResult result = jMeterService.runStressTest(url, threads);
            resultStore.put(result.getTestId(), result);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("Load test failed", e);
            return ApiResponse.error("Load test failed: " + e.getMessage());
        }
    }

    /**
     * 生成 JMX 文件
     */
    @PostMapping("/jmx/generate")
    @Operation(summary = "生成 JMX 文件", description = "根据配置生成 JMeter JMX 文件")
    public ApiResponse<String> generateJmx(@RequestBody PerformanceTestRequest request) {
        log.info("Generating JMX file for: {}", request.getUrl());

        try {
            PerformanceConfig config = PerformanceConfig.builder()
                    .url(request.getUrl())
                    .method(request.getMethod())
                    .threads(request.getThreads())
                    .rampUpSeconds(request.getRampUpSeconds())
                    .durationSeconds(request.getDurationSeconds())
                    .testName(request.getTestName())
                    .build();

            String jmxPath = jmxBuilder.buildJmx(config);
            return ApiResponse.success(jmxPath, "JMX file generated successfully");

        } catch (Exception e) {
            log.error("Failed to generate JMX file", e);
            return ApiResponse.error("Failed to generate JMX file: " + e.getMessage());
        }
    }

    /**
     * 获取性能测试结果
     */
    @GetMapping("/result/{testId}")
    @Operation(summary = "获取性能测试结果")
    public ApiResponse<PerformanceResult> getResult(@PathVariable String testId) {
        PerformanceResult result = resultStore.get(testId);

        if (result == null) {
            return ApiResponse.error("Result not found: " + testId);
        }

        return ApiResponse.success(result);
    }

    /**
     * 分析性能测试结果 - 简化版
     */
    @PostMapping("/analyze/{testId}")
    @Operation(summary = "分析性能测试结果")
    public ApiResponse<Map<String, Object>> analyzeResult(@PathVariable String testId) {
        PerformanceResult result = resultStore.get(testId);

        if (result == null) {
            return ApiResponse.error("Result not found: " + testId);
        }

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("testName", result.getTestName());
        analysis.put("tps", result.getTps());
        analysis.put("avgResponseTime", result.getAvgResponseTime());
        analysis.put("tp90ResponseTime", result.getTp90ResponseTime());
        analysis.put("errorRate", result.getErrorRate());
        analysis.put("rating", "GOOD");

        return ApiResponse.success(analysis);
    }

    /**
     * 获取所有性能测试结果
     */
    @GetMapping("/results")
    @Operation(summary = "获取所有性能测试结果")
    public ApiResponse<List<PerformanceResult>> getAllResults() {
        return ApiResponse.success(List.copyOf(resultStore.values()));
    }

    /**
     * 清除性能测试结果
     */
    @DeleteMapping("/results")
    @Operation(summary = "清除所有性能测试结果")
    public ApiResponse<Void> clearResults() {
        resultStore.clear();
        return ApiResponse.success(null, "All results cleared");
    }

    private String generateTestId() {
        return "PERF-" + System.currentTimeMillis();
    }
}
