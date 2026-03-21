package com.test.web.controller;

import com.test.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.Duration;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息控制器
 */
@RestController
@RequestMapping("/api/system")
@Tag(name = "System", description = "系统信息相关接口")
public class SystemController {

    @GetMapping("/info")
    @Operation(summary = "获取系统信息")
    public ApiResponse<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        // 基本信息
        info.put("applicationName", "Test Engine Platform");
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("currentTime", LocalDateTime.now());
        info.put("timezone", System.getProperty("user.timezone"));

        // JVM 信息
        Runtime runtime = Runtime.getRuntime();
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("javaHome", System.getProperty("java.home"));
        info.put("processors", runtime.availableProcessors());

        // 内存信息
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        info.put("heapMemoryUsed", formatBytes(memoryBean.getHeapMemoryUsage().getUsed()));
        info.put("heapMemoryMax", formatBytes(memoryBean.getHeapMemoryUsage().getMax()));
        info.put("nonHeapMemoryUsed", formatBytes(memoryBean.getNonHeapMemoryUsage().getUsed()));
        info.put("totalMemory", formatBytes(runtime.totalMemory()));
        info.put("freeMemory", formatBytes(runtime.freeMemory()));
        info.put("maxMemory", formatBytes(runtime.maxMemory()));

        // 操作系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("osArch", System.getProperty("os.arch"));
        info.put("systemLoadAverage", osBean.getSystemLoadAverage());

        return ApiResponse.success(info);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());

        Runtime runtime = Runtime.getRuntime();
        double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory();

        health.put("memoryUsage", String.format("%.2f%%", memoryUsage * 100));
        health.put("healthy", memoryUsage < 0.9);

        return ApiResponse.success(health);
    }

    @GetMapping("/uptime")
    @Operation(summary = "获取运行时长")
    public ApiResponse<Map<String, Object>> getUptime() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration uptime = Duration.ofMillis(uptimeMs);

        Map<String, Object> result = new HashMap<>();
        result.put("uptimeMs", uptimeMs);
        result.put("uptimeSeconds", uptime.getSeconds());
        result.put("uptimeMinutes", uptime.toMinutes());
        result.put("uptimeHours", uptime.toHours());
        result.put("formatted", formatDuration(uptime));

        return ApiResponse.success(result);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}