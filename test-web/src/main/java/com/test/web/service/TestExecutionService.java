package com.test.web.service;

import com.test.core.TestNGRunner;
import com.test.core.TestResult;
import com.test.middleware.QualityGate;
import com.test.web.dto.TestExecutionRequest;
import com.test.web.dto.TestExecutionResponse;
import com.test.web.entity.TestExecutionEntity;
import com.test.web.entity.TestResultDetailEntity;
import com.test.web.exception.BusinessException;
import com.test.web.repository.TestExecutionRepository;
import com.test.web.repository.TestResultDetailRepository;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 测试执行服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutionService {

    private final TestNGRunner testNGRunner;
    private final QualityGate qualityGate;
    private final TestExecutionRepository executionRepository;
    private final TestResultDetailRepository resultDetailRepository;

    /**
     * 执行测试
     */
    @Transactional
    public TestExecutionResponse executeTest(TestExecutionRequest request) {
        log.info("Executing test: {}", request);

        // 创建执行记录
        String executionId = generateExecutionId();
        TestExecutionEntity entity = new TestExecutionEntity();
        entity.setExecutionId(executionId);
        entity.setTestType(determineTestType(request));
        entity.setPackagePath(request.getPackagePath());
        entity.setStatus("RUNNING");
        entity.setTriggerSource("MANUAL");
        entity.setCreatedAt(LocalDateTime.now());

        executionRepository.save(entity);

        try {
            // 执行测试
            List<TestResult> results;
            if (request.getTestClass() != null && !request.getTestClass().isEmpty()) {
                Class<?> testClass = Class.forName(request.getTestClass());
                results = testNGRunner.runTestClass(testClass);
                entity.setClassName(request.getTestClass());
            } else {
                results = testNGRunner.runSuite(request.getPackagePath(), request.getGroups());
            }

            // 更新统计信息
            int total = results.size();
            int passed = (int) results.stream()
                    .filter(r -> r.getStatus() == TestResult.TestStatus.SUCCESS).count();
            int failed = (int) results.stream()
                    .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE).count();
            int skipped = total - passed - failed;

            entity.setTotalTests(total);
            entity.setPassedTests(passed);
            entity.setFailedTests(failed);
            entity.setSkippedTests(skipped);
            entity.setPassRate(total > 0 ? (double) passed / total : 0.0);
            entity.setStatus("COMPLETED");

            // 质量门禁检查
            if (request.isCheckQualityGate()) {
                QualityGate.QualityGateResult gateResult = qualityGate.check(results);
                entity.setQualityGatePassed(gateResult.isPassed());
            }

            // 保存详细结果
            saveResultDetails(executionId, results);

            // 构建响应
            return buildResponse(entity, results);

        } catch (ClassNotFoundException e) {
            log.error("Test class not found: {}", request.getTestClass(), e);
            entity.setStatus("FAILED");
            entity.setErrorMessage("Test class not found: " + request.getTestClass());
            executionRepository.save(entity);
            throw BusinessException.of("Test class not found: " + request.getTestClass());

        } catch (Exception e) {
            log.error("Test execution failed", e);
            entity.setStatus("FAILED");
            entity.setErrorMessage(e.getMessage());
            executionRepository.save(entity);
            throw BusinessException.of("Test execution failed: " + e.getMessage());
        }
    }

    /**
     * 异步执行测试
     */
    @Async
    @Transactional
    public CompletableFuture<TestExecutionResponse> executeTestAsync(TestExecutionRequest request) {
        return CompletableFuture.completedFuture(executeTest(request));
    }

    /**
     * 获取执行历史
     */
    public Page<TestExecutionEntity> getExecutionHistory(String testType, Pageable pageable) {
        if (testType != null && !testType.isEmpty()) {
            return executionRepository.findByTestType(testType, pageable);
        }
        return executionRepository.findAll(pageable);
    }

    /**
     * 获取执行详情
     */
    public TestExecutionResponse getExecutionDetail(String executionId) {
        TestExecutionEntity entity = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> BusinessException.notFound("Execution"));

        List<TestResultDetailEntity> details = resultDetailRepository.findByExecutionId(executionId);
        return buildResponseFromEntity(entity, details);
    }

    /**
     * 获取执行统计
     */
    public ExecutionStatistics getStatistics(LocalDateTime start, LocalDateTime end) {
        List<TestExecutionEntity> executions = executionRepository.findByCreatedAtBetween(start, end);

        ExecutionStatistics stats = new ExecutionStatistics();
        stats.setTotalExecutions(executions.size());

        if (!executions.isEmpty()) {
            stats.setTotalTests(executions.stream().mapToLong(e -> e.getTotalTests() != null ? e.getTotalTests() : 0).sum());
            stats.setTotalPassed(executions.stream().mapToLong(e -> e.getPassedTests() != null ? e.getPassedTests() : 0).sum());
            stats.setTotalFailed(executions.stream().mapToLong(e -> e.getFailedTests() != null ? e.getFailedTests() : 0).sum());
            stats.setAveragePassRate(executions.stream()
                    .filter(e -> e.getPassRate() != null)
                    .mapToDouble(TestExecutionEntity::getPassRate)
                    .average()
                    .orElse(0.0));
        }

        return stats;
    }

    /**
     * 删除执行记录
     */
    @Transactional
    public void deleteExecution(String executionId) {
        resultDetailRepository.deleteByExecutionId(executionId);
        executionRepository.deleteByExecutionId(executionId);
    }

    private String generateExecutionId() {
        return "EXEC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String determineTestType(TestExecutionRequest request) {
        if (request.getGroups() == null || request.getGroups().length == 0) {
            return "FUNCTIONAL";
        }
        String group = request.getGroups()[0];
        return group.toUpperCase();
    }

    private void saveResultDetails(String executionId, List<TestResult> results) {
        List<TestResultDetailEntity> details = results.stream()
                .map(result -> {
                    TestResultDetailEntity detail = new TestResultDetailEntity();
                    detail.setExecutionId(executionId);
                    detail.setClassName(result.getClassName());
                    detail.setMethodName(result.getMethodName());
                    detail.setStatus(result.getStatus().name());
                    detail.setDescription(result.getDescription());
                    detail.setPriority(result.getPriority());
                    detail.setDurationMs(result.getDurationMs());
                    detail.setFailureMessage(result.getFailureMessage());
                    detail.setStackTrace(result.getStackTrace());
                    return detail;
                })
                .collect(Collectors.toList());

        resultDetailRepository.saveAll(details);
    }

    private TestExecutionResponse buildResponse(TestExecutionEntity entity, List<TestResult> results) {
        TestExecutionResponse response = new TestExecutionResponse();
        response.setExecutionId(entity.getExecutionId());
        response.setStatus(entity.getStatus());
        response.setTotalTests(entity.getTotalTests());
        response.setPassedTests(entity.getPassedTests());
        response.setFailedTests(entity.getFailedTests());
        response.setResults(results);
        return response;
    }

    private TestExecutionResponse buildResponseFromEntity(TestExecutionEntity entity, List<TestResultDetailEntity> details) {
        TestExecutionResponse response = new TestExecutionResponse();
        response.setExecutionId(entity.getExecutionId());
        response.setStatus(entity.getStatus());
        response.setTotalTests(entity.getTotalTests());
        response.setPassedTests(entity.getPassedTests());
        response.setFailedTests(entity.getFailedTests());
        response.setErrorMessage(entity.getErrorMessage());
        return response;
    }

    /**
     * 执行统计
     */
    @lombok.Data
    public static class ExecutionStatistics {
        private int totalExecutions;
        private long totalTests;
        private long totalPassed;
        private long totalFailed;
        private double averagePassRate;
    }
}