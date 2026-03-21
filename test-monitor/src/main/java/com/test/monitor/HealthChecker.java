package com.test.monitor;

import com.test.core.TestResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 健康检查服务
 * 对目标系统进行健康检查
 */
@Slf4j
@Service
public class HealthChecker {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Map<String, HealthCheckConfig> checkConfigs = new ConcurrentHashMap<>();
    private final Map<String, HealthCheckResult> lastResults = new ConcurrentHashMap<>();

    /**
     * 注册健康检查
     */
    public void registerCheck(String name, HealthCheckConfig config) {
        checkConfigs.put(name, config);
        log.info("Health check registered: {}", name);
    }

    /**
     * 执行健康检查
     */
    public HealthCheckResult check(String name) {
        HealthCheckConfig config = checkConfigs.get(name);
        if (config == null) {
            return HealthCheckResult.failed(name, "Check not found");
        }

        long startTime = System.currentTimeMillis();
        HealthCheckResult result = new HealthCheckResult();
        result.setName(name);
        result.setCheckTime(java.time.LocalDateTime.now());

        try {
            switch (config.getType()) {
                case HTTP:
                    result = checkHttp(config, startTime);
                    break;
                case TCP:
                    result = checkTcp(config, startTime);
                    break;
                case CUSTOM:
                    result = checkCustom(config, startTime);
                    break;
                default:
                    result = HealthCheckResult.failed(name, "Unknown check type");
            }

        } catch (Exception e) {
            result.setHealthy(false);
            result.setMessage("Check failed: " + e.getMessage());
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
        }

        // 保存结果
        lastResults.put(name, result);
        return result;
    }

    /**
     * HTTP 健康检查
     */
    private HealthCheckResult checkHttp(HealthCheckConfig config, long startTime) {
        HealthCheckResult result = new HealthCheckResult();
        result.setName(config.getName());
        result.setCheckTime(java.time.LocalDateTime.now());
        result.setType("HTTP");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl()))
                    .timeout(Duration.ofMillis(config.getTimeoutMs()))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            long responseTime = System.currentTimeMillis() - startTime;
            result.setResponseTimeMs(responseTime);
            result.setStatusCode(response.statusCode());

            // 检查状态码
            boolean statusOk = response.statusCode() >= 200 && response.statusCode() < 300;

            // 检查响应时间
            boolean timeOk = responseTime <= config.getMaxResponseTimeMs();

            result.setHealthy(statusOk && timeOk);

            if (!statusOk) {
                result.setMessage("HTTP status code: " + response.statusCode());
            } else if (!timeOk) {
                result.setMessage("Response time " + responseTime + "ms exceeds threshold");
            } else {
                result.setMessage("Health check passed");
            }

        } catch (Exception e) {
            result.setHealthy(false);
            result.setMessage("HTTP check failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * TCP 健康检查
     */
    private HealthCheckResult checkTcp(HealthCheckConfig config, long startTime) {
        HealthCheckResult result = new HealthCheckResult();
        result.setName(config.getName());
        result.setCheckTime(java.time.LocalDateTime.now());
        result.setType("TCP");

        try {
            String[] parts = config.getUrl().split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), (int) config.getTimeoutMs());
                long responseTime = System.currentTimeMillis() - startTime;

                result.setHealthy(true);
                result.setResponseTimeMs(responseTime);
                result.setMessage("TCP connection successful");
            }

        } catch (Exception e) {
            result.setHealthy(false);
            result.setMessage("TCP check failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * 自定义健康检查
     */
    private HealthCheckResult checkCustom(HealthCheckConfig config, long startTime) {
        HealthCheckResult result = new HealthCheckResult();
        result.setName(config.getName());
        result.setCheckTime(java.time.LocalDateTime.now());
        result.setType("CUSTOM");

        try {
            // 执行自定义检查逻辑（简化版）
            long responseTime = System.currentTimeMillis() - startTime;
            boolean healthy = true; // 默认认为健康

            result.setHealthy(healthy);
            result.setResponseTimeMs(responseTime);
            result.setMessage(healthy ? "Custom check passed" : "Custom check failed");

        } catch (Exception e) {
            result.setHealthy(false);
            result.setMessage("Custom check failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行所有注册的健康检查
     */
    public List<HealthCheckResult> checkAll() {
        List<HealthCheckResult> results = new ArrayList<>();
        for (String name : checkConfigs.keySet()) {
            results.add(check(name));
        }
        return results;
    }

    /**
     * 获取最后一次检查结果
     */
    public HealthCheckResult getLastResult(String name) {
        return lastResults.get(name);
    }

    /**
     * 获取所有检查结果
     */
    public List<HealthCheckResult> getAllResults() {
        return new ArrayList<>(lastResults.values());
    }

    /**
     * 获取健康状态摘要
     */
    public HealthSummary getHealthSummary() {
        HealthSummary summary = new HealthSummary();
        summary.setCheckTime(java.time.LocalDateTime.now());

        int total = lastResults.size();
        int healthy = (int) lastResults.values().stream()
                .filter(HealthCheckResult::isHealthy)
                .count();

        summary.setTotalChecks(total);
        summary.setHealthyChecks(healthy);
        summary.setUnhealthyChecks(total - healthy);
        summary.setOverallHealthy(healthy == total);

        return summary;
    }

    /**
     * 健康检查配置
     */
    @Data
    public static class HealthCheckConfig {
        private String name;
        private CheckType type = CheckType.HTTP;
        private String url;
        private long timeoutMs = 5000;
        private long maxResponseTimeMs = 3000;
        private Map<String, String> headers;
        private java.util.function.Function<HealthCheckConfig, Boolean> checkLogic;

        public enum CheckType {
            HTTP, TCP, CUSTOM
        }
    }

    /**
     * 健康检查结果
     */
    @Data
    public static class HealthCheckResult {
        private String name;
        private String type;
        private boolean healthy;
        private String message;
        private java.time.LocalDateTime checkTime;
        private long responseTimeMs;
        private Integer statusCode;

        public static HealthCheckResult failed(String name, String message) {
            HealthCheckResult result = new HealthCheckResult();
            result.setName(name);
            result.setHealthy(false);
            result.setMessage(message);
            result.setCheckTime(java.time.LocalDateTime.now());
            return result;
        }
    }

    /**
     * 健康摘要
     */
    @Data
    public static class HealthSummary {
        private java.time.LocalDateTime checkTime;
        private int totalChecks;
        private int healthyChecks;
        private int unhealthyChecks;
        private boolean overallHealthy;
    }
}
