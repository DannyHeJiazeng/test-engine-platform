package com.test.middleware;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试调度服务
 * 管理定时测试任务的调度
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestScheduler {

    private final GitService gitService;
    private final BuildService buildService;
    private final com.test.core.TestNGRunner testNGRunner;
    private final QualityGate qualityGate;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 定时执行测试任务
     * 默认每小时执行一次
     */
    @Scheduled(cron = "${test-engine.schedule.cron:0 0 * * * ?}")
    public void scheduledTestExecution() {
        log.info("Starting scheduled test execution...");

        try {
            // 1. 拉取最新代码
            String repoDir = "./test-workspace/test-examples";
            gitService.pull(repoDir);

            // 2. 构建项目
            BuildService.BuildResult buildResult = buildService.packageSkipTests(repoDir);
            if (!buildResult.isSuccess()) {
                log.error("Build failed: {}", buildResult.getErrorMessage());
                return;
            }

            // 3. 执行测试
            List<com.test.core.TestResult> results = testNGRunner.runSuite(
                    "com.example.functional",
                    new String[]{"functional"}
            );

            // 4. 质量门禁检查
            QualityGate.QualityGateResult gateResult = qualityGate.check(results);

            log.info("Scheduled test execution completed: {} tests, {} passed, gate passed: {}",
                    results.size(),
                    results.stream().filter(r -> r.getStatus() == com.test.core.TestResult.TestStatus.SUCCESS).count(),
                    gateResult.isPassed());

        } catch (Exception e) {
            log.error("Scheduled test execution failed", e);
        }
    }

    /**
     * 手动触发测试执行
     */
    public ScheduleResult triggerTestExecution(ScheduleRequest request) {
        log.info("Triggering test execution: {}", request);

        String executionId = "EXEC-" + System.currentTimeMillis();

        executor.submit(() -> {
            try {
                // 执行测试
                List<com.test.core.TestResult> results = testNGRunner.runSuite(
                        request.getPackagePath(),
                        request.getGroups()
                );

                // 质量门禁检查
                if (request.isCheckQualityGate()) {
                    qualityGate.check(results);
                }

                log.info("Test execution completed: {}", executionId);

            } catch (Exception e) {
                log.error("Test execution failed: {}", executionId, e);
            }
        });

        ScheduleResult result = new ScheduleResult();
        result.setExecutionId(executionId);
        result.setStatus("STARTED");
        return result;
    }

    /**
     * 调度请求
     */
    @lombok.Data
    public static class ScheduleRequest {
        private String packagePath;
        private String[] groups;
        private boolean checkQualityGate = true;
        private String triggeredBy;
    }

    /**
     * 调度结果
     */
    @lombok.Data
    public static class ScheduleResult {
        private String executionId;
        private String status;
        private String message;
    }
}