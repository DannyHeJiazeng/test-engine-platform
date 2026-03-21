package com.test.performance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 性能测试结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResult {

    /**
     * 测试 ID
     */
    private String testId;

    /**
     * 测试名称
     */
    private String testName;

    /**
     * 测试 URL
     */
    private String testUrl;

    /**
     * 测试状态：SUCCESS, FAILURE
     */
    private String status;

    /**
     * 失败消息
     */
    private String failureMessage;

    // ========== 性能指标 ==========

    /**
     * 总请求数
     */
    private long totalRequests;

    /**
     * 成功请求数
     */
    private long successRequests;

    /**
     * 失败请求数
     */
    private long failedRequests;

    /**
     * 错误率 (0.0 - 1.0)
     */
    private double errorRate;

    /**
     * 平均响应时间 (ms)
     */
    private double avgResponseTime;

    /**
     * 最小响应时间 (ms)
     */
    private long minResponseTime;

    /**
     * 最大响应时间 (ms)
     */
    private long maxResponseTime;

    /**
     * TP50 响应时间 (ms)
     */
    private long tp50ResponseTime;

    /**
     * TP90 响应时间 (ms)
     */
    private long tp90ResponseTime;

    /**
     * TP95 响应时间 (ms)
     */
    private long tp95ResponseTime;

    /**
     * TP99 响应时间 (ms)
     */
    private long tp99ResponseTime;

    /**
     * 吞吐量 (TPS - Transactions Per Second)
     */
    private double tps;

    /**
     * 网络吞吐量 (KB/s)
     */
    private double throughputKbps;

    /**
     * 测试持续时间 (ms)
     */
    private long durationMs;

    /**
     * 并发用户数
     */
    private int concurrentUsers;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 额外指标
     */
    private Map<String, Object> metrics = new HashMap<>();

    /**
     * 是否通过性能阈值检查
     */
    public boolean isPassedThreshold(PerformanceThreshold threshold) {
        if (threshold.getMaxResponseTime() > 0 && avgResponseTime > threshold.getMaxResponseTime()) {
            return false;
        }
        if (threshold.getMaxTp90() > 0 && tp90ResponseTime > threshold.getMaxTp90()) {
            return false;
        }
        if (threshold.getMaxErrorRate() > 0 && errorRate > threshold.getMaxErrorRate()) {
            return false;
        }
        if (threshold.getMinTps() > 0 && tps < threshold.getMinTps()) {
            return false;
        }
        return true;
    }

    /**
     * 创建成功的结果
     */
    public static PerformanceResult success(String testId, String testName) {
        return PerformanceResult.builder()
                .testId(testId)
                .testName(testName)
                .status("SUCCESS")
                .build();
    }

    /**
     * 创建失败的结果
     */
    public static PerformanceResult failure(String testId, String testName, String failureMessage) {
        return PerformanceResult.builder()
                .testId(testId)
                .testName(testName)
                .status("FAILURE")
                .failureMessage(failureMessage)
                .build();
    }

    /**
     * 性能阈值配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceThreshold {
        /**
         * 最大平均响应时间 (ms)
         */
        private long maxResponseTime;

        /**
         * 最大 TP90 响应时间 (ms)
         */
        private long maxTp90;

        /**
         * 最大错误率 (0.0 - 1.0)
         */
        private double maxErrorRate;

        /**
         * 最小 TPS
         */
        private double minTps;
    }
}
