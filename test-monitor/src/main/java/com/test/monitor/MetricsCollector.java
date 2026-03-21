package com.test.monitor;

import com.test.core.TestResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标收集器
 * 收集和统计测试执行指标
 */
@Slf4j
@Service
public class MetricsCollector {

    private final Map<String, TestMetrics> metricsStore = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final List<MetricRecord> metricHistory = Collections.synchronizedList(new ArrayList<>());

    /**
     * 记录测试结果
     */
    public void recordInspectionResult(List<TestResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        for (TestResult result : results) {
            String key = result.getClassName() + "#" + result.getMethodName();
            metricsStore.computeIfAbsent(key, k -> new TestMetrics());

            TestMetrics metrics = metricsStore.get(key);
            metrics.setTestName(result.getMethodName());
            metrics.setClassName(result.getClassName());
            metrics.setTotalRuns(metrics.getTotalRuns() + 1);

            if (result.getStatus() == TestResult.TestStatus.SUCCESS) {
                metrics.setSuccessCount(metrics.getSuccessCount() + 1);
            } else if (result.getStatus() == TestResult.TestStatus.FAILURE) {
                metrics.setFailureCount(metrics.getFailureCount() + 1);
            }

            metrics.setPassRate((double) metrics.getSuccessCount() / metrics.getTotalRuns());
            metrics.setLastRunTime(LocalDateTime.now());
            metrics.setLastStatus(result.getStatus().name());
        }
    }

    /**
     * 记录计数器
     */
    public void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 获取计数器值
     */
    public long getCounter(String name) {
        AtomicLong counter = counters.get(name);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 记录自定义指标
     */
    public void recordMetric(String name, double value, Map<String, String> tags) {
        MetricRecord record = new MetricRecord();
        record.setName(name);
        record.setValue(value);
        record.setTags(tags);
        record.setTimestamp(LocalDateTime.now());

        metricHistory.add(record);

        // 限制历史记录数量
        if (metricHistory.size() > 10000) {
            metricHistory.remove(0);
        }
    }

    /**
     * 记录性能指标
     */
    public void recordPerformanceMetric(String testName, long responseTime, boolean success) {
        String key = "performance:" + testName;
        metricsStore.computeIfAbsent(key, k -> new TestMetrics());

        TestMetrics metrics = metricsStore.get(key);
        metrics.setTestName(testName);
        metrics.addResponseTime(responseTime);
        metrics.setTotalRuns(metrics.getTotalRuns() + 1);

        if (success) {
            metrics.setSuccessCount(metrics.getSuccessCount() + 1);
        } else {
            metrics.setFailureCount(metrics.getFailureCount() + 1);
        }

        // 计算百分位
        metrics.calculatePercentiles();
    }

    /**
     * 获取测试指标
     */
    public TestMetrics getMetrics(String testName) {
        return metricsStore.get(testName);
    }

    /**
     * 获取所有指标
     */
    public Map<String, TestMetrics> getAllMetrics() {
        return new HashMap<>(metricsStore);
    }

    /**
     * 获取指标历史
     */
    public List<MetricRecord> getMetricHistory(String name, int limit) {
        List<MetricRecord> result = new ArrayList<>();
        synchronized (metricHistory) {
            for (int i = metricHistory.size() - 1; i >= 0 && result.size() < limit; i--) {
                MetricRecord record = metricHistory.get(i);
                if (name == null || name.equals(record.getName())) {
                    result.add(record);
                }
            }
        }
        return result;
    }

    /**
     * 获取指标摘要
     */
    public MetricsSummary getSummary() {
        MetricsSummary summary = new MetricsSummary();
        summary.setGeneratedAt(LocalDateTime.now());

        int totalTests = metricsStore.size();
        int passingTests = 0;
        int failingTests = 0;
        long totalRuns = 0;
        long totalSuccess = 0;
        long totalFailures = 0;

        for (TestMetrics metrics : metricsStore.values()) {
            totalRuns += metrics.getTotalRuns();
            totalSuccess += metrics.getSuccessCount();
            totalFailures += metrics.getFailureCount();

            if (metrics.getPassRate() >= 0.8) {
                passingTests++;
            } else {
                failingTests++;
            }
        }

        summary.setTotalTests(totalTests);
        summary.setPassingTests(passingTests);
        summary.setFailingTests(failingTests);
        summary.setTotalRuns(totalRuns);
        summary.setTotalSuccess(totalSuccess);
        summary.setTotalFailures(totalFailures);
        summary.setOverallPassRate(totalRuns > 0 ? (double) totalSuccess / totalRuns : 0.0);

        return summary;
    }

    /**
     * 清除指标
     */
    public void clearMetrics() {
        metricsStore.clear();
        counters.clear();
        metricHistory.clear();
        log.info("All metrics cleared");
    }

    /**
     * 导出指标为 Prometheus 格式
     */
    public String exportPrometheusFormat() {
        StringBuilder sb = new StringBuilder();

        // 导出计数器
        for (Map.Entry<String, AtomicLong> entry : counters.entrySet()) {
            sb.append("# TYPE ").append(entry.getKey()).append(" counter\n");
            sb.append(entry.getKey()).append(" ").append(entry.getValue().get()).append("\n");
        }

        // 导出测试指标
        sb.append("# TYPE test_runs_total counter\n");
        sb.append("# TYPE test_success_total counter\n");
        sb.append("# TYPE test_failure_total counter\n");
        sb.append("# TYPE test_pass_rate gauge\n");

        for (Map.Entry<String, TestMetrics> entry : metricsStore.entrySet()) {
            String testName = entry.getKey().replace("#", "_");
            TestMetrics metrics = entry.getValue();

            sb.append("test_runs_total{test=\"").append(testName).append("\"} ")
              .append(metrics.getTotalRuns()).append("\n");
            sb.append("test_success_total{test=\"").append(testName).append("\"} ")
              .append(metrics.getSuccessCount()).append("\n");
            sb.append("test_failure_total{test=\"").append(testName).append("\"} ")
              .append(metrics.getFailureCount()).append("\n");
            sb.append("test_pass_rate{test=\"").append(testName).append("\"} ")
              .append(String.format("%.4f", metrics.getPassRate())).append("\n");
        }

        return sb.toString();
    }

    /**
     * 测试指标
     */
    @Data
    public static class TestMetrics {
        private String testName;
        private String className;
        private int totalRuns;
        private int successCount;
        private int failureCount;
        private double passRate;
        private LocalDateTime lastRunTime;
        private String lastStatus;
        private List<Long> responseTimes = new ArrayList<>();

        // 性能百分位
        private long avgResponseTime;
        private long p50ResponseTime;
        private long p90ResponseTime;
        private long p95ResponseTime;
        private long p99ResponseTime;

        public void addResponseTime(long time) {
            responseTimes.add(time);
            // 保持最近 1000 条记录
            if (responseTimes.size() > 1000) {
                responseTimes.remove(0);
            }
        }

        public void calculatePercentiles() {
            if (responseTimes.isEmpty()) {
                return;
            }

            List<Long> sorted = new ArrayList<>(responseTimes);
            Collections.sort(sorted);

            int size = sorted.size();
            avgResponseTime = (long) sorted.stream().mapToLong(Long::longValue).average().orElse(0);
            p50ResponseTime = sorted.get((int) (size * 0.5));
            p90ResponseTime = sorted.get((int) (size * 0.9));
            p95ResponseTime = sorted.get((int) (size * 0.95));
            p99ResponseTime = sorted.get((int) (size * 0.99));
        }
    }

    /**
     * 指标记录
     */
    @Data
    public static class MetricRecord {
        private String name;
        private double value;
        private Map<String, String> tags;
        private LocalDateTime timestamp;
    }

    /**
     * 指标摘要
     */
    @Data
    public static class MetricsSummary {
        private LocalDateTime generatedAt;
        private int totalTests;
        private int passingTests;
        private int failingTests;
        private long totalRuns;
        private long totalSuccess;
        private long totalFailures;
        private double overallPassRate;
    }
}