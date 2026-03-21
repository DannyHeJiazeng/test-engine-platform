package com.test.performance;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 性能测试配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceConfig {

    /**
     * 测试 URL
     */
    private String url;

    /**
     * 请求方法：GET, POST, PUT, DELETE
     */
    private String method = "GET";

    /**
     * 请求头
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 请求体（用于 POST/PUT）
     */
    private String requestBody;

    /**
     * 并发线程数
     */
    private int threads = 10;

    /**
     * 启动时间（秒）- 多少秒内启动所有线程
     */
    private int rampUpSeconds = 10;

    /**
     * 测试持续时间（秒）
     */
    private int durationSeconds = 60;

    /**
     * 循环次数（-1 表示无限循环直到持续时间结束）
     */
    private int loopCount = -1;

    /**
     * 响应超时时间（毫秒）
     */
    private int responseTimeoutMs = 30000;

    /**
     * 是否跟随重定向
     */
    private boolean followRedirects = true;

    /**
     * 是否自动处理 Cookie
     */
    private boolean autoCookie = true;

    /**
     * 断言响应码
     */
    private int assertResponseCode = 200;

    /**
     * 最大允许响应时间（毫秒）
     */
    private long maxResponseTimeMs = 5000;

    /**
     * 结果输出目录
     */
    private String outputDir = "./performance-results";

    /**
     * 测试名称
     */
    private String testName = "Performance Test";

    /**
     * 测试 ID
     */
    private String testId;

    /**
     * 性能阈值配置
     */
    private PerformanceResult.PerformanceThreshold threshold;

    /**
     * 默认配置
     */
    public static PerformanceConfig defaultConfig(String url) {
        return PerformanceConfig.builder()
                .url(url)
                .method("GET")
                .threads(10)
                .rampUpSeconds(10)
                .durationSeconds(60)
                .build();
    }

    /**
     * 并发测试配置
     */
    public static PerformanceConfig concurrencyTest(String url, int concurrency) {
        return PerformanceConfig.builder()
                .url(url)
                .method("GET")
                .threads(concurrency)
                .rampUpSeconds(5)
                .durationSeconds(60)
                .testName("Concurrency Test - " + concurrency + " users")
                .build();
    }

    /**
     * 压力测试配置
     */
    public static PerformanceConfig stressTest(String url, int maxThreads) {
        return PerformanceConfig.builder()
                .url(url)
                .method("GET")
                .threads(maxThreads)
                .rampUpSeconds(maxThreads)  // 每秒启动一个线程
                .durationSeconds(300)        // 5 分钟
                .testName("Stress Test")
                .build();
    }

    /**
     * 负载测试配置
     */
    public static PerformanceConfig loadTest(String url, int threads, int durationMinutes) {
        return PerformanceConfig.builder()
                .url(url)
                .method("GET")
                .threads(threads)
                .rampUpSeconds(60)
                .durationSeconds(durationMinutes * 60)
                .testName("Load Test - " + durationMinutes + " minutes")
                .build();
    }
}
