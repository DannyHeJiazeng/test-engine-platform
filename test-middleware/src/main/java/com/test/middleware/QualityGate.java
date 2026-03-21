package com.test.middleware;

import com.test.core.TestResult;
import com.test.core.TestNGRunner;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 质量门禁服务
 * 定义质量阈值，检查测试执行结果是否满足发布条件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityGate {

    private final Map<String, QualityGateHistory> historyStore = new ConcurrentHashMap<>();

    /**
     * 执行质量门禁检查
     *
     * @param testResults 测试结果列表
     * @param threshold   质量阈值配置
     * @return 质量门禁结果
     */
    public QualityGateResult check(List<TestResult> testResults, QualityThreshold threshold) {
        log.info("Starting quality gate check with threshold: {}", threshold);

        QualityGateResult result = new QualityGateResult();
        result.setCheckTime(java.time.LocalDateTime.now());

        if (testResults == null || testResults.isEmpty()) {
            result.setPassed(false);
            result.setMessage("No test results to check");
            return result;
        }

        // 统计测试结果
        int total = testResults.size();
        int success = (int) testResults.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.SUCCESS)
                .count();
        int failure = (int) testResults.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE)
                .count();
        int skipped = total - success - failure;

        double passRate = total > 0 ? (double) success / total : 0.0;

        result.setTotalTests(total);
        result.setPassedTests(success);
        result.setFailedTests(failure);
        result.setSkippedTests(skipped);
        result.setPassRate(passRate);

        // 检查各项阈值
        List<String> failedChecks = new ArrayList<>();

        // 1. 检查通过率
        if (threshold.getMinPassRate() > 0 && passRate < threshold.getMinPassRate()) {
            failedChecks.add(String.format("Pass rate %.2f%% < %.2f%% (minimum)",
                    passRate * 100, threshold.getMinPassRate() * 100));
        }

        // 2. 检查 P0 用例（高优先级用例）
        if (threshold.isRequireP0Pass()) {
            List<TestResult> p0Failures = testResults.stream()
                    .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE)
                    .filter(r -> "P0".equals(r.getPriority()))
                    .toList();
            if (!p0Failures.isEmpty()) {
                failedChecks.add(String.format("P0 tests failed: %d", p0Failures.size()));
                result.setP0Failures(p0Failures.stream()
                        .map(TestResult::getMethodName)
                        .toList());
            }
        }

        // 3. 检查最大失败数
        if (threshold.getMaxFailures() > 0 && failure > threshold.getMaxFailures()) {
            failedChecks.add(String.format("Failed tests %d > %d (maximum)",
                    failure, threshold.getMaxFailures()));
        }

        // 4. 检查是否允许失败
        if (threshold.isNoFailureAllowed() && failure > 0) {
            failedChecks.add(String.format("No failures allowed, but %d tests failed", failure));
        }

        // 5. 检查关键测试用例
        if (threshold.getCriticalTests() != null && !threshold.getCriticalTests().isEmpty()) {
            List<String> failedCriticalTests = new ArrayList<>();
            for (TestResult testResult : testResults) {
                if (testResult.getStatus() == TestResult.TestStatus.FAILURE &&
                    threshold.getCriticalTests().contains(testResult.getTestId())) {
                    failedCriticalTests.add(testResult.getTestId());
                }
            }
            if (!failedCriticalTests.isEmpty()) {
                failedChecks.add("Critical tests failed: " + String.join(", ", failedCriticalTests));
                result.setCriticalFailures(failedCriticalTests);
            }
        }

        // 判断是否通过
        result.setPassed(failedChecks.isEmpty());
        result.setFailedChecks(failedChecks);

        if (result.isPassed()) {
            result.setMessage("Quality gate passed");
            log.info("Quality gate PASSED: {}/{} tests passed ({:.2f}%)",
                    success, total, passRate * 100);
        } else {
            result.setMessage("Quality gate failed: " + String.join("; ", failedChecks));
            log.warn("Quality gate FAILED: {}", result.getMessage());
        }

        return result;
    }

    /**
     * 执行质量门禁检查（使用默认阈值）
     */
    public QualityGateResult check(List<TestResult> testResults) {
        QualityThreshold defaultThreshold = QualityThreshold.defaultThreshold();
        return check(testResults, defaultThreshold);
    }

    /**
     * 记录质量门禁历史
     */
    public void recordHistory(String buildId, QualityGateResult result) {
        historyStore.computeIfAbsent(buildId, k -> new QualityGateHistory());
        QualityGateHistory history = historyStore.get(buildId);
        history.setBuildId(buildId);
        history.getResults().add(result);
    }

    /**
     * 获取质量门禁历史
     */
    public List<QualityGateHistory> getHistory() {
        return new ArrayList<>(historyStore.values());
    }

    /**
     * 获取特定构建的质量门禁结果
     */
    public QualityGateResult getHistory(String buildId) {
        QualityGateHistory history = historyStore.get(buildId);
        if (history == null || history.getResults().isEmpty()) {
            return null;
        }
        return history.getResults().get(history.getResults().size() - 1);
    }

    /**
     * 清除历史记录
     */
    public void clearHistory(String buildId) {
        historyStore.remove(buildId);
    }

    /**
     * 清除所有历史记录
     */
    public void clearAllHistory() {
        historyStore.clear();
    }

    /**
     * 质量阈值配置
     */
    @Data
    public static class QualityThreshold {
        /**
         * 最小通过率 (0.0 - 1.0)
         */
        private double minPassRate = 0.8;

        /**
         * 最大失败数
         */
        private int maxFailures = 0;

        /**
         * 是否不允许失败
         */
        private boolean noFailureAllowed = true;

        /**
         * 是否要求 P0 用例全部通过
         */
        private boolean requireP0Pass = true;

        /**
         * 关键测试用例列表
         */
        private List<String> criticalTests = new ArrayList<>();

        /**
         * 创建默认阈值
         */
        public static QualityThreshold defaultThreshold() {
            QualityThreshold threshold = new QualityThreshold();
            threshold.setMinPassRate(0.8);
            threshold.setMaxFailures(0);
            threshold.setNoFailureAllowed(true);
            threshold.setRequireP0Pass(true);
            return threshold;
        }

        /**
         * 创建宽松阈值
         */
        public static QualityThreshold relaxedThreshold() {
            QualityThreshold threshold = new QualityThreshold();
            threshold.setMinPassRate(0.6);
            threshold.setMaxFailures(10);
            threshold.setNoFailureAllowed(false);
            threshold.setRequireP0Pass(false);
            return threshold;
        }

        /**
         * 创建严格阈值
         */
        public static QualityThreshold strictThreshold() {
            QualityThreshold threshold = new QualityThreshold();
            threshold.setMinPassRate(1.0);
            threshold.setMaxFailures(0);
            threshold.setNoFailureAllowed(true);
            threshold.setRequireP0Pass(true);
            return threshold;
        }
    }

    /**
     * 质量门禁结果
     */
    @Data
    public static class QualityGateResult {
        /**
         * 是否通过
         */
        private boolean passed;

        /**
         * 消息
         */
        private String message;

        /**
         * 检查时间
         */
        private java.time.LocalDateTime checkTime;

        /**
         * 总测试数
         */
        private int totalTests;

        /**
         * 通过测试数
         */
        private int passedTests;

        /**
         * 失败测试数
         */
        private int failedTests;

        /**
         * 跳过测试数
         */
        private int skippedTests;

        /**
         * 通过率
         */
        private double passRate;

        /**
         * P0 失败用例
         */
        private List<String> p0Failures = new ArrayList<>();

        /**
         * 关键失败用例
         */
        private List<String> criticalFailures = new ArrayList<>();

        /**
         * 失败的检查项
         */
        private List<String> failedChecks = new ArrayList<>();

        /**
         * 获取状态描述
         */
        public String getStatusText() {
            return passed ? "PASSED" : "FAILED";
        }

        /**
         * 获取详细报告
         */
        public String getDetailedReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== Quality Gate Report ===\n");
            report.append("Status: ").append(getStatusText()).append("\n");
            report.append("Message: ").append(message).append("\n");
            report.append("Total Tests: ").append(totalTests).append("\n");
            report.append("Passed: ").append(passedTests).append("\n");
            report.append("Failed: ").append(failedTests).append("\n");
            report.append("Skipped: ").append(skippedTests).append("\n");
            report.append("Pass Rate: ").append(String.format("%.2f%%", passRate * 100)).append("\n");

            if (!failedChecks.isEmpty()) {
                report.append("\nFailed Checks:\n");
                for (String check : failedChecks) {
                    report.append("  - ").append(check).append("\n");
                }
            }

            return report.toString();
        }
    }

    /**
     * 质量门禁历史
     */
    @Data
    public static class QualityGateHistory {
        private String buildId;
        private List<QualityGateResult> results = new ArrayList<>();

        /**
         * 获取通过率趋势
         */
        public List<Double> getPassRateTrend() {
            List<Double> trend = new ArrayList<>();
            for (QualityGateResult result : results) {
                trend.add(result.getPassRate());
            }
            return trend;
        }

        /**
         * 获取平均通过率
         */
        public double getAveragePassRate() {
            if (results.isEmpty()) {
                return 0.0;
            }
            return results.stream()
                    .mapToDouble(QualityGateResult::getPassRate)
                    .average()
                    .orElse(0.0);
        }
    }
}
