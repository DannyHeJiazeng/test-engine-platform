package com.test.monitor;

import com.test.core.TestNGRunner;
import com.test.core.TestResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 巡检调度器
 * 定时执行巡检测试用例
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionScheduler {

    private final TestNGRunner testNGRunner;
    private final AlertService alertService;
    private final MetricsCollector metricsCollector;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, InspectionTask> taskStore = new ConcurrentHashMap<>();

    /**
     * 默认巡检任务
     * 每 5 分钟执行一次
     */
    @Scheduled(fixedRate = 300000)  // 5 minutes
    public void runDefaultInspection() {
        log.info("Running scheduled inspection...");

        try {
            // 执行巡检测试
            List<TestResult> results = testNGRunner.runInspectionTests("com.example.inspection");

            // 记录指标
            metricsCollector.recordInspectionResult(results);

            // 检查失败并发送告警
            for (TestResult result : results) {
                if (result.getStatus() == TestResult.TestStatus.FAILURE) {
                    log.warn("Inspection test failed: {}", result.getTestId());
                    sendInspectionAlert(result);
                }
            }

            log.info("Inspection completed: {} tests, {} passed, {} failed",
                    results.size(),
                    results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.SUCCESS).count(),
                    results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE).count());

        } catch (Exception e) {
            log.error("Failed to run inspection", e);
            alertService.sendSystemAlert("Inspection failed: " + e.getMessage());
        }
    }

    /**
     * 发送巡检告警
     */
    private void sendInspectionAlert(TestResult result) {
        AlertMessage alert = new AlertMessage();
        alert.setType(AlertType.INSPECTION_FAILED);
        alert.setTitle("巡检失败告警");
        alert.setContent(String.format("巡检用例 %s 执行失败：%s",
                result.getTestId(),
                result.getFailureMessage()));
        alert.setLevel(AlertLevel.WARNING);
        alert.setTimestamp(java.time.LocalDateTime.now());

        alertService.sendAlert(alert);
    }

    /**
     * 注册巡检任务
     */
    public void registerTask(String taskId, InspectionConfig config) {
        InspectionTask task = new InspectionTask();
        task.setTaskId(taskId);
        task.setConfig(config);
        task.setNextRunTime(java.time.LocalDateTime.now().plusSeconds(config.getIntervalSeconds()));
        taskStore.put(taskId, task);
        log.info("Inspection task registered: {}", taskId);
    }

    /**
     * 取消巡检任务
     */
    public void cancelTask(String taskId) {
        taskStore.remove(taskId);
        log.info("Inspection task cancelled: {}", taskId);
    }

    /**
     * 立即执行巡检任务
     */
    public void executeTaskImmediately(String taskId) {
        InspectionTask task = taskStore.get(taskId);
        if (task == null) {
            log.warn("Inspection task not found: {}", taskId);
            return;
        }

        executor.submit(() -> {
            try {
                log.info("Executing inspection task: {}", taskId);
                task.setRunning(true);
                task.setLastRunTime(java.time.LocalDateTime.now());

                // 执行测试
                List<TestResult> results = testNGRunner.runInspectionTests(
                        task.getConfig().getPackagePath());

                task.setLastResults(results);
                task.setTotalRuns(task.getTotalRuns() + 1);

                // 处理失败
                int failures = (int) results.stream()
                        .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE)
                        .count();

                if (failures > 0 && task.getConfig().isAlertOnFailure()) {
                    sendTaskAlert(taskId, results);
                }

                task.setLastSuccess(failures == 0);

            } catch (Exception e) {
                log.error("Failed to execute inspection task: {}", taskId, e);
                task.setLastSuccess(false);
            } finally {
                task.setRunning(false);
                task.setNextRunTime(java.time.LocalDateTime.now()
                        .plusSeconds(task.getConfig().getIntervalSeconds()));
            }
        });
    }

    /**
     * 发送任务告警
     */
    private void sendTaskAlert(String taskId, List<TestResult> results) {
        List<String> failedTests = results.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE)
                .map(TestResult::getTestId)
                .toList();

        AlertMessage alert = new AlertMessage();
        alert.setType(AlertType.INSPECTION_FAILED);
        alert.setTitle("巡检任务告警 - " + taskId);
        alert.setContent("巡检任务执行失败，失败用例：" + String.join(", ", failedTests));
        alert.setLevel(AlertLevel.WARNING);
        alert.setTimestamp(java.time.LocalDateTime.now());

        alertService.sendAlert(alert);
    }

    /**
     * 获取所有巡检任务
     */
    public List<InspectionTask> getAllTasks() {
        return new ArrayList<>(taskStore.values());
    }

    /**
     * 获取特定任务
     */
    public InspectionTask getTask(String taskId) {
        return taskStore.get(taskId);
    }

    /**
     * 巡检配置
     */
    @Data
    public static class InspectionConfig {
        private String packagePath = "com.example.inspection";
        private int intervalSeconds = 300;  // 5 分钟
        private boolean alertOnFailure = true;
        private AlertLevel alertLevel = AlertLevel.WARNING;
        private List<String> receivers = new ArrayList<>();
    }

    /**
     * 巡检任务
     */
    @Data
    public static class InspectionTask {
        private String taskId;
        private InspectionConfig config;
        private boolean running;
        private java.time.LocalDateTime lastRunTime;
        private java.time.LocalDateTime nextRunTime;
        private List<TestResult> lastResults;
        private int totalRuns;
        private boolean lastSuccess;
    }
}
