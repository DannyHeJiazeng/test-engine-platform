package com.test.web.controller;

import com.test.monitor.*;
import com.test.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 监控控制器
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Tag(name = "Monitor", description = "监控相关接口")
public class MonitorController {

    private final AlertService alertService;
    private final HealthChecker healthChecker;
    private final MetricsCollector metricsCollector;
    private final InspectionScheduler inspectionScheduler;

    /**
     * 获取告警历史
     */
    @GetMapping("/alerts")
    @Operation(summary = "获取告警历史")
    public ApiResponse<List<AlertMessage>> getAlerts(@RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(alertService.getAlertHistory(limit));
    }

    /**
     * 获取健康检查列表
     */
    @GetMapping("/health")
    @Operation(summary = "获取健康检查状态")
    public ApiResponse<List<HealthChecker.HealthCheckResult>> getHealth() {
        return ApiResponse.success(healthChecker.getAllResults());
    }

    /**
     * 执行健康检查
     */
    @PostMapping("/health/check/{name}")
    @Operation(summary = "执行健康检查")
    public ApiResponse<HealthChecker.HealthCheckResult> checkHealth(@PathVariable String name) {
        return ApiResponse.success(healthChecker.check(name));
    }

    /**
     * 获取健康摘要
     */
    @GetMapping("/health/summary")
    @Operation(summary = "获取健康摘要")
    public ApiResponse<HealthChecker.HealthSummary> getHealthSummary() {
        return ApiResponse.success(healthChecker.getHealthSummary());
    }

    /**
     * 获取指标摘要
     */
    @GetMapping("/metrics/summary")
    @Operation(summary = "获取指标摘要")
    public ApiResponse<MetricsCollector.MetricsSummary> getMetricsSummary() {
        return ApiResponse.success(metricsCollector.getSummary());
    }

    /**
     * 获取所有指标
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取所有指标")
    public ApiResponse<Map<String, MetricsCollector.TestMetrics>> getAllMetrics() {
        return ApiResponse.success(metricsCollector.getAllMetrics());
    }

    /**
     * 导出 Prometheus 格式指标
     */
    @GetMapping("/metrics/prometheus")
    @Operation(summary = "导出 Prometheus 格式指标")
    public String exportPrometheusMetrics() {
        return metricsCollector.exportPrometheusFormat();
    }

    /**
     * 获取巡检任务列表
     */
    @GetMapping("/inspection/tasks")
    @Operation(summary = "获取巡检任务列表")
    public ApiResponse<List<InspectionScheduler.InspectionTask>> getInspectionTasks() {
        return ApiResponse.success(inspectionScheduler.getAllTasks());
    }

    /**
     * 创建巡检任务
     */
    @PostMapping("/inspection/tasks")
    @Operation(summary = "创建巡检任务")
    public ApiResponse<Void> createInspectionTask(
            @RequestParam String taskId,
            @RequestParam(defaultValue = "com.example.inspection") String packagePath,
            @RequestParam(defaultValue = "300") int intervalSeconds,
            @RequestParam(defaultValue = "true") boolean alertOnFailure) {

        InspectionScheduler.InspectionConfig config = new InspectionScheduler.InspectionConfig();
        config.setPackagePath(packagePath);
        config.setIntervalSeconds(intervalSeconds);
        config.setAlertOnFailure(alertOnFailure);

        inspectionScheduler.registerTask(taskId, config);
        return ApiResponse.success(null, "巡检任务已创建");
    }

    /**
     * 删除巡检任务
     */
    @DeleteMapping("/inspection/tasks/{taskId}")
    @Operation(summary = "删除巡检任务")
    public ApiResponse<Void> deleteInspectionTask(@PathVariable String taskId) {
        inspectionScheduler.cancelTask(taskId);
        return ApiResponse.success(null, "巡检任务已删除");
    }

    /**
     * 立即执行巡检任务
     */
    @PostMapping("/inspection/tasks/{taskId}/execute")
    @Operation(summary = "立即执行巡检任务")
    public ApiResponse<Void> executeInspectionTask(@PathVariable String taskId) {
        inspectionScheduler.executeTaskImmediately(taskId);
        return ApiResponse.success(null, "巡检任务已执行");
    }

    /**
     * 清除告警历史
     */
    @DeleteMapping("/alerts")
    @Operation(summary = "清除告警历史")
    public ApiResponse<Void> clearAlerts() {
        alertService.clearAlertHistory();
        return ApiResponse.success(null, "告警历史已清除");
    }

    /**
     * 清除指标
     */
    @DeleteMapping("/metrics")
    @Operation(summary = "清除指标")
    public ApiResponse<Void> clearMetrics() {
        metricsCollector.clearMetrics();
        return ApiResponse.success(null, "指标已清除");
    }
}
