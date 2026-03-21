package com.test.web.service;

import com.test.performance.*;
import com.test.web.dto.PerformanceTestRequest;
import com.test.web.entity.PerformanceResultEntity;
import com.test.web.exception.BusinessException;
import com.test.web.repository.PerformanceResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 性能测试服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceTestService {

    private final PerformanceExecutor performanceExecutor;
    private final PerformanceResultRepository resultRepository;

    /**
     * 执行性能测试
     */
    @Transactional
    public PerformanceResult executePerformanceTest(PerformanceTestRequest request) {
        log.info("Executing performance test: {}", request);

        // 构建配置
        PerformanceConfig config = PerformanceConfig.builder()
                .url(request.getUrl())
                .method(request.getMethod())
                .threads(request.getThreads())
                .rampUpSeconds(request.getRampUpSeconds())
                .durationSeconds(request.getDurationSeconds())
                .testName(request.getTestName())
                .testId(generateTestId())
                .maxResponseTimeMs(request.getMaxResponseTimeMs())
                .build();

        // 执行测试
        PerformanceResult result = performanceExecutor.execute(config);

        // 保存结果
        savePerformanceResult(result);

        return result;
    }

    /**
     * 异步执行性能测试
     */
    @Async("performanceTestExecutor")
    @Transactional
    public CompletableFuture<PerformanceResult> executePerformanceTestAsync(PerformanceTestRequest request) {
        return CompletableFuture.completedFuture(executePerformanceTest(request));
    }

    /**
     * 执行并发测试
     */
    @Transactional
    public PerformanceResult executeConcurrencyTest(String url, int concurrency) {
        log.info("Executing concurrency test: url={}, concurrency={}", url, concurrency);

        PerformanceResult result = performanceExecutor.executeConcurrencyTest(url, concurrency);
        savePerformanceResult(result);

        return result;
    }

    /**
     * 执行梯度并发测试
     */
    @Transactional
    public List<PerformanceResult> executeGradualConcurrencyTest(String url, int startConcurrency,
                                                                   int endConcurrency, int step) {
        log.info("Executing gradual concurrency test: url={}, start={}, end={}, step={}",
                url, startConcurrency, endConcurrency, step);

        List<PerformanceResult> results = performanceExecutor.executeGradualConcurrencyTest(
                url, startConcurrency, endConcurrency, step);

        // 保存所有结果
        results.forEach(this::savePerformanceResult);

        return results;
    }

    /**
     * 执行负载测试
     */
    @Transactional
    public PerformanceResult executeLoadTest(String url, int threads, int durationMinutes) {
        log.info("Executing load test: url={}, threads={}, duration={}min",
                url, threads, durationMinutes);

        PerformanceResult result = performanceExecutor.executeLoadTest(url, threads, durationMinutes);
        savePerformanceResult(result);

        return result;
    }

    /**
     * 获取性能测试结果
     */
    public PerformanceResult getResult(String testId) {
        PerformanceResultEntity entity = resultRepository.findByTestId(testId)
                .orElseThrow(() -> BusinessException.notFound("Performance test result"));

        return convertToResult(entity);
    }

    /**
     * 获取所有结果
     */
    public List<PerformanceResult> getAllResults() {
        return resultRepository.findAll().stream()
                .map(this::convertToResult)
                .collect(Collectors.toList());
    }

    /**
     * 获取性能趋势
     */
    public PerformanceTrend getPerformanceTrend(String testUrl, int days) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        List<PerformanceResultEntity> results = resultRepository.findByCreatedAtBetween(start, end);

        PerformanceTrend trend = new PerformanceTrend();
        trend.setTestUrl(testUrl);
        trend.setDataPoints(results.stream()
                .map(this::convertToResult)
                .collect(Collectors.toList()));

        // 计算平均值
        if (!results.isEmpty()) {
            trend.setAverageResponseTime(results.stream()
                    .mapToDouble(r -> r.getAvgResponseTime() != null ? r.getAvgResponseTime() : 0)
                    .average().orElse(0));
            trend.setAverageTps(results.stream()
                    .mapToDouble(r -> r.getTps() != null ? r.getTps() : 0)
                    .average().orElse(0));
            trend.setAverageErrorRate(results.stream()
                    .mapToDouble(r -> r.getErrorRate() != null ? r.getErrorRate() : 0)
                    .average().orElse(0));
        }

        return trend;
    }

    /**
     * 清除所有结果
     */
    @Transactional
    public void clearAllResults() {
        resultRepository.deleteAll();
    }

    private String generateTestId() {
        return "PERF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void savePerformanceResult(PerformanceResult result) {
        PerformanceResultEntity entity = new PerformanceResultEntity();
        entity.setTestId(result.getTestId());
        entity.setTestName(result.getTestName());
        entity.setTestUrl(result.getTestUrl());
        entity.setStatus(result.getStatus());
        entity.setTotalRequests(result.getTotalRequests());
        entity.setSuccessRequests(result.getSuccessRequests());
        entity.setFailedRequests(result.getFailedRequests());
        entity.setErrorRate(result.getErrorRate());
        entity.setAvgResponseTime(result.getAvgResponseTime());
        entity.setTp90ResponseTime(result.getTp90ResponseTime());
        entity.setTp95ResponseTime(result.getTp95ResponseTime());
        entity.setTp99ResponseTime(result.getTp99ResponseTime());
        entity.setTps(result.getTps());
        entity.setConcurrentUsers(result.getConcurrentUsers());
        entity.setDurationSeconds((int) (result.getDurationMs() / 1000));
        entity.setFailureMessage(result.getFailureMessage());

        resultRepository.save(entity);
    }

    private PerformanceResult convertToResult(PerformanceResultEntity entity) {
        return PerformanceResult.builder()
                .testId(entity.getTestId())
                .testName(entity.getTestName())
                .testUrl(entity.getTestUrl())
                .status(entity.getStatus())
                .totalRequests(entity.getTotalRequests())
                .successRequests(entity.getSuccessRequests())
                .failedRequests(entity.getFailedRequests())
                .errorRate(entity.getErrorRate())
                .avgResponseTime(entity.getAvgResponseTime())
                .tp90ResponseTime(entity.getTp90ResponseTime())
                .tp95ResponseTime(entity.getTp95ResponseTime())
                .tp99ResponseTime(entity.getTp99ResponseTime())
                .tps(entity.getTps())
                .concurrentUsers(entity.getConcurrentUsers())
                .durationMs(entity.getDurationSeconds() != null ? entity.getDurationSeconds() * 1000L : 0)
                .failureMessage(entity.getFailureMessage())
                .build();
    }

    /**
     * 性能趋势
     */
    @lombok.Data
    public static class PerformanceTrend {
        private String testUrl;
        private List<PerformanceResult> dataPoints;
        private double averageResponseTime;
        private double averageTps;
        private double averageErrorRate;
    }
}