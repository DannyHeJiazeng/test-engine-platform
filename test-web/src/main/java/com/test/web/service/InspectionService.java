package com.test.web.service;

import com.test.core.TestNGRunner;
import com.test.core.TestResult;
import com.test.monitor.*;
import com.test.web.dto.InspectionTaskRequest;
import com.test.web.entity.AlertRecordEntity;
import com.test.web.entity.InspectionTaskEntity;
import com.test.web.exception.BusinessException;
import com.test.web.repository.AlertRecordRepository;
import com.test.web.repository.InspectionTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 巡检服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionService {

    private final TestNGRunner testNGRunner;
    private final AlertService alertService;
    private final InspectionTaskRepository taskRepository;
    private final AlertRecordRepository alertRecordRepository;

    /**
     * 创建巡检任务
     */
    @Transactional
    public InspectionTaskEntity createTask(InspectionTaskRequest request) {
        if (taskRepository.existsByTaskId(request.getTaskId())) {
            throw BusinessException.alreadyExists("Inspection task");
        }

        InspectionTaskEntity entity = new InspectionTaskEntity();
        entity.setTaskId(request.getTaskId());
        entity.setTaskName("Inspection Task - " + request.getTaskId());
        entity.setPackagePath(request.getPackagePath());
        entity.setIntervalSeconds(request.getIntervalSeconds());
        entity.setEnabled(true);
        entity.setAlertOnFailure(request.isAlertOnFailure());
        entity.setAlertLevel(request.getAlertLevel().name());
        entity.setTotalRuns(0);
        entity.setSuccessCount(0);
        entity.setFailureCount(0);

        return taskRepository.save(entity);
    }

    /**
     * 执行巡检任务
     */
    @Transactional
    public InspectionResult executeTask(String taskId) {
        InspectionTaskEntity entity = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> BusinessException.notFound("Inspection task"));

        log.info("Executing inspection task: {}", taskId);

        // 执行测试
        List<TestResult> results = testNGRunner.runInspectionTests(entity.getPackagePath());

        // 统计结果
        int total = results.size();
        int passed = (int) results.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.SUCCESS).count();
        int failed = total - passed;

        // 更新任务状态
        entity.setLastRunTime(LocalDateTime.now());
        entity.setLastRunStatus(failed == 0 ? "SUCCESS" : "FAILURE");
        entity.setTotalRuns(entity.getTotalRuns() + 1);
        entity.setSuccessCount(entity.getSuccessCount() + (failed == 0 ? 1 : 0));
        entity.setFailureCount(entity.getFailureCount() + (failed > 0 ? 1 : 0));

        // 发送告警
        if (failed > 0 && entity.getAlertOnFailure()) {
            sendInspectionAlert(entity, results);
        }

        // 构建结果
        InspectionResult result = new InspectionResult();
        result.setTaskId(taskId);
        result.setTotalTests(total);
        result.setPassedTests(passed);
        result.setFailedTests(failed);
        result.setResults(results);
        result.setExecutionTime(LocalDateTime.now());

        return result;
    }

    /**
     * 异步执行巡检任务
     */
    @Async
    public void executeTaskAsync(String taskId) {
        executeTask(taskId);
    }

    /**
     * 启用/禁用任务
     */
    @Transactional
    public void setTaskEnabled(String taskId, boolean enabled) {
        InspectionTaskEntity entity = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> BusinessException.notFound("Inspection task"));
        entity.setEnabled(enabled);
        taskRepository.save(entity);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(String taskId) {
        taskRepository.deleteByTaskId(taskId);
    }

    /**
     * 获取所有启用的任务
     */
    public List<InspectionTaskEntity> getEnabledTasks() {
        return taskRepository.findByEnabled(true);
    }

    /**
     * 获取任务详情
     */
    public InspectionTaskEntity getTask(String taskId) {
        return taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> BusinessException.notFound("Inspection task"));
    }

    /**
     * 分页查询任务
     */
    public Page<InspectionTaskEntity> getTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    /**
     * 获取告警历史
     */
    public Page<AlertRecordEntity> getAlertHistory(Pageable pageable) {
        return alertRecordRepository.findAll(pageable);
    }

    /**
     * 确认告警
     */
    @Transactional
    public void acknowledgeAlert(Long alertId, String acknowledgedBy) {
        AlertRecordEntity entity = alertRecordRepository.findById(alertId)
                .orElseThrow(() -> BusinessException.notFound("Alert"));

        entity.setAcknowledged(true);
        entity.setAcknowledgedAt(LocalDateTime.now());
        entity.setAcknowledgedBy(acknowledgedBy);

        alertRecordRepository.save(entity);
    }

    /**
     * 发送巡检告警
     */
    private void sendInspectionAlert(InspectionTaskEntity task, List<TestResult> results) {
        List<String> failedTests = results.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE)
                .map(TestResult::getMethodName)
                .collect(Collectors.toList());

        AlertMessage alert = AlertMessage.builder()
                .type(AlertType.INSPECTION_FAILED)
                .title("巡检任务失败告警")
                .content(String.format("巡检任务 %s 执行失败，失败用例：%s",
                        task.getTaskId(), String.join(", ", failedTests)))
                .level(AlertLevel.valueOf(task.getAlertLevel()))
                .timestamp(LocalDateTime.now())
                .build();

        alertService.sendAlert(alert);

        // 保存告警记录
        saveAlertRecord(alert, task.getTaskId());
    }

    private void saveAlertRecord(AlertMessage alert, String taskId) {
        AlertRecordEntity entity = new AlertRecordEntity();
        entity.setAlertType(alert.getType().name());
        entity.setTitle(alert.getTitle());
        entity.setContent(alert.getContent());
        entity.setLevel(alert.getLevel().name());
        entity.setRelatedExecutionId(taskId);
        entity.setSent(true);
        entity.setSentAt(LocalDateTime.now());
        entity.setAcknowledged(false);

        alertRecordRepository.save(entity);
    }

    /**
     * 巡检结果
     */
    @lombok.Data
    public static class InspectionResult {
        private String taskId;
        private int totalTests;
        private int passedTests;
        private int failedTests;
        private List<TestResult> results;
        private LocalDateTime executionTime;
    }
}