package com.test.web.controller;

import com.test.monitor.*;
import com.test.web.dto.ApiResponse;
import com.test.web.dto.InspectionTaskRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 巡检控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inspection")
@RequiredArgsConstructor
@Tag(name = "Inspection", description = "巡检相关接口")
public class InspectionController {

    private final InspectionScheduler inspectionScheduler;
    private final HealthChecker healthChecker;
    private final AlertService alertService;
    private final MetricsCollector metricsCollector;

    /**
     * 获取所有巡检任务
     */
    @GetMapping("/tasks")
    @Operation(summary = "获取所有巡检任务")
    public ApiResponse<List<InspectionScheduler.InspectionTask>> getAllTasks() {
        return ApiResponse.success(inspectionScheduler.getAllTasks());
    }

    /**
     * 创建巡检任务
     */
    @PostMapping("/tasks")
    @Operation(summary = "创建巡检任务")
    public ApiResponse<Void> createTask(@RequestBody InspectionTaskRequest request) {
        log.info("Creating inspection task: {}", request.getTaskId());

        InspectionScheduler.InspectionConfig config = new InspectionScheduler.InspectionConfig();
        config.setPackagePath(request.getPackagePath());
        config.setIntervalSeconds(request.getIntervalSeconds());
        config.setAlertOnFailure(request.isAlertOnFailure());
        config.setAlertLevel(request.getAlertLevel());

        inspectionScheduler.registerTask(request.getTaskId(), config);

        return ApiResponse.success(null, "Inspection task created");
    }

    /**
     * 取消巡检任务
     */
    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "取消巡检任务")
    public ApiResponse<Void> cancelTask(@PathVariable String taskId) {
        log.info("Cancelling inspection task: {}", taskId);
        inspectionScheduler.cancelTask(taskId);
        return ApiResponse.success(null, "Inspection task cancelled");
    }

    /**
     * 立即执行巡检任务
     */
    @PostMapping("/tasks/{taskId}/execute")
    @Operation(summary = "立即执行巡检任务")
    public ApiResponse<Void> executeTask(@PathVariable String taskId) {
        log.info("Executing inspection task immediately: {}", taskId);
        inspectionScheduler.executeTaskImmediately(taskId);
        return ApiResponse.success(null, "Inspection task execution started");
    }

    /**
     * 获取巡检任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取巡检任务详情")
    public ApiResponse<InspectionScheduler.InspectionTask> getTask(@PathVariable String taskId) {
        InspectionScheduler.InspectionTask task = inspectionScheduler.getTask(taskId);
        if (task == null) {
            return ApiResponse.error("Task not found: " + taskId);
        }
        return ApiResponse.success(task);
    }

    /**
     * 注册健康检查
     */
    @PostMapping("/health-checks")
    @Operation(summary = "注册健康检查")
    public ApiResponse<Void> registerHealthCheck(
            @RequestParam String name,
            @RequestParam String url,
            @RequestParam(defaultValue = "HTTP") String type) {

        log.info("Registering health check: {}", name);

        HealthChecker.HealthCheckConfig config = new HealthChecker.HealthCheckConfig();
        config.setName(name);
        config.setUrl(url);
        config.setType(HealthChecker.HealthCheckConfig.CheckType.valueOf(type));

        healthChecker.registerCheck(name, config);

        return ApiResponse.success(null, "Health check registered");
    }

    /**
     * 执行健康检查
     */
    @GetMapping("/health-checks/{name}")
    @Operation(summary = "执行健康检查")
    public ApiResponse<HealthChecker.HealthCheckResult> checkHealth(@PathVariable String name) {
        HealthChecker.HealthCheckResult result = healthChecker.check(name);
        return ApiResponse.success(result);
    }

    /**
     * 执行所有健康检查
     */
    @GetMapping("/health-checks")
    @Operation(summary = "执行所有健康检查")
    public ApiResponse<List<HealthChecker.HealthCheckResult>> checkAllHealth() {
        return ApiResponse.success(healthChecker.checkAll());
    }

    /**
     * 获取健康状态摘要
     */
    @GetMapping("/health-summary")
    @Operation(summary = "获取健康状态摘要")
    public ApiResponse<HealthChecker.HealthSummary> getHealthSummary() {
        return ApiResponse.success(healthChecker.getHealthSummary());
    }

    /**
     * 获取告警历史
     */
    @GetMapping("/alerts")
    @Operation(summary = "获取告警历史")
    public ApiResponse<List<AlertMessage>> getAlertHistory(
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(alertService.getAlertHistory(limit));
    }

    /**
     * 发送测试告警
     */
    @PostMapping("/alerts/test")
    @Operation(summary = "发送测试告警")
    public ApiResponse<Void> sendTestAlert() {
        AlertMessage alert = AlertMessage.builder()
                .type(AlertType.CUSTOM)
                .title("测试告警")
                .content("这是一条测试告警消息")
                .level(AlertLevel.INFO)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        alertService.sendAlert(alert);
        return ApiResponse.success(null, "Test alert sent");
    }

    /**
     * 清除告警历史
     */
    @DeleteMapping("/alerts")
    @Operation(summary = "清除告警历史")
    public ApiResponse<Void> clearAlertHistory() {
        alertService.clearAlertHistory();
        return ApiResponse.success(null, "Alert history cleared");
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
    public ApiResponse<List<MetricsCollector.TestMetrics>> getAllMetrics() {
        return ApiResponse.success(List.copyOf(metricsCollector.getAllMetrics().values()));
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
     * 清除指标
     */
    @DeleteMapping("/metrics")
    @Operation(summary = "清除所有指标")
    public ApiResponse<Void> clearMetrics() {
        metricsCollector.clearMetrics();
        return ApiResponse.success(null, "Metrics cleared");
    }
}