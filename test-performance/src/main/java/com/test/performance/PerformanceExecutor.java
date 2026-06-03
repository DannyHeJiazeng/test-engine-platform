package com.test.performance;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 性能测试执行器
 * 整合 JMeterService 和 JmxBuilder 提供完整的性能测试服务
 */
@Slf4j
public class PerformanceExecutor {

    private JMeterService jMeterService;
    private JmxBuilder jmxBuilder;

    public PerformanceExecutor() {
        // 默认构造函数
    }

    public PerformanceExecutor(JMeterService jMeterService, JmxBuilder jmxBuilder) {
        this.jMeterService = jMeterService;
        this.jmxBuilder = jmxBuilder;
    }

    /**
     * 执行性能测试
     */
    public PerformanceResult execute(PerformanceConfig config) {
        log.info("Executing performance test: {}", config.getTestName());

        try {
            // 1. 构建 JMX 文件（可选，用于保存测试计划）
            String jmxFile = jmxBuilder.buildJmx(config);
            log.debug("JMX file: {}", jmxFile);

            // 2. 执行性能测试
            PerformanceResult result = jMeterService.runPerformanceTest(config);

            return result;

        } catch (Exception e) {
            log.error("Failed to execute performance test", e);
            return PerformanceResult.failure(
                    config.getTestId(),
                    config.getTestName(),
                    e.getMessage()
            );
        }
    }

    /**
     * 执行并发测试
     */
    public PerformanceResult executeConcurrencyTest(String url, int concurrency) {
        PerformanceConfig config = PerformanceConfig.concurrencyTest(url, concurrency);
        return execute(config);
    }

    /**
     * 执行梯度并发测试
     */
    public List<PerformanceResult> executeGradualConcurrencyTest(String url, int startConcurrency,
                                                                  int endConcurrency, int step) {
        List<PerformanceResult> results = new ArrayList<>();

        for (int concurrency = startConcurrency; concurrency <= endConcurrency; concurrency += step) {
            log.info("Running concurrency test: {} users", concurrency);
            PerformanceResult result = executeConcurrencyTest(url, concurrency);
            results.add(result);

            if ("FAILURE".equals(result.getStatus())) {
                log.warn("Concurrency test failed at {} users, stopping", concurrency);
                break;
            }
        }

        return results;
    }

    /**
     * 执行压力测试
     */
    public PerformanceResult executeStressTest(String url) {
        PerformanceConfig config = PerformanceConfig.stressTest(url, 100);
        return execute(config);
    }

    /**
     * 执行负载测试
     */
    public PerformanceResult executeLoadTest(String url, int threads, int durationMinutes) {
        PerformanceConfig config = PerformanceConfig.loadTest(url, threads, durationMinutes);
        return execute(config);
    }

    /**
     * 批量执行性能测试
     */
    public Map<String, PerformanceResult> executeBatch(List<PerformanceConfig> configs) {
        Map<String, PerformanceResult> results = new HashMap<>();

        for (PerformanceConfig config : configs) {
            log.info("Executing batch test: {}", config.getTestName());
            PerformanceResult result = execute(config);
            results.put(config.getTestId(), result);
        }

        return results;
    }
}
