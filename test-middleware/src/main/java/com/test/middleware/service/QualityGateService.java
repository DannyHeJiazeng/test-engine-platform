package com.test.middleware.service;

import com.test.core.TestResult;
import com.test.middleware.QualityGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 质量门禁服务 - 重构后的服务层
 * 提供质量门禁检查的核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityGateService {

    private final QualityGate qualityGate;

    /**
     * 执行质量门禁检查
     *
     * @param testResults 测试结果列表
     * @return 质量门禁结果
     */
    public QualityGate.QualityGateResult checkQualityGate(List<TestResult> testResults) {
        log.info("Executing quality gate check for {} test results", testResults.size());

        QualityGate.QualityThreshold defaultThreshold = QualityGate.QualityThreshold.defaultThreshold();
        QualityGate.QualityGateResult result = qualityGate.check(testResults, defaultThreshold);

        // 记录检查结果到历史
        if (result.getCheckTime() != null) {
            String buildId = "BUILD-" + System.currentTimeMillis();
            qualityGate.recordHistory(buildId, result);
        }

        return result;
    }

    /**
     * 使用自定义阈值执行质量门禁检查
     *
     * @param testResults 测试结果列表
     * @param threshold   自定义阈值配置
     * @return 质量门禁结果
     */
    public QualityGate.QualityGateResult checkQualityGate(List<TestResult> testResults, QualityGate.QualityThreshold threshold) {
        log.info("Executing quality gate check with custom threshold for {} test results", testResults.size());

        QualityGate.QualityGateResult result = qualityGate.check(testResults, threshold);

        // 记录检查结果到历史
        if (result.getCheckTime() != null) {
            String buildId = "BUILD-" + System.currentTimeMillis();
            qualityGate.recordHistory(buildId, result);
        }

        return result;
    }

    /**
     * 获取质量门禁历史记录
     *
     * @return 质量门禁历史列表
     */
    public List<QualityGate.QualityGateHistory> getQualityGateHistory() {
        return qualityGate.getHistory();
    }

    /**
     * 获取特定构建的质量门禁结果
     *
     * @param buildId 构建ID
     * @return 质量门禁结果
     */
    public QualityGate.QualityGateResult getQualityGateResult(String buildId) {
        return qualityGate.getHistory(buildId);
    }

    /**
     * 清除质量门禁历史记录
     */
    public void clearHistory() {
        qualityGate.clearAllHistory();
    }

    /**
     * 计算测试统计数据
     *
     * @param testResults 测试结果列表
     * @return 测试统计数据
     */
    public TestStatistics calculateStatistics(List<TestResult> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            return new TestStatistics(0, 0, 0, 0, 0.0);
        }

        int total = testResults.size();
        long successCount = testResults.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.SUCCESS)
                .count();
        long failureCount = testResults.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.FAILURE)
                .count();
        long skipCount = testResults.stream()
                .filter(r -> r.getStatus() == TestResult.TestStatus.SKIP)
                .count();
        int errorCount = 0; // 在当前TestResult.TestStatus枚举中没有ERROR状态，所以设为0

        double passRate = total > 0 ? (double) successCount / total : 0.0;

        return new TestStatistics(total, (int) successCount, (int) failureCount, (int) skipCount, passRate);
    }

    /**
     * 测试统计数据
     */
    public static class TestStatistics {
        private final int totalCount;
        private final int passedCount;
        private final int failedCount;
        private final int skippedCount;
        private final double passRate;

        public TestStatistics(int totalCount, int passedCount, int failedCount, int skippedCount, double passRate) {
            this.totalCount = totalCount;
            this.passedCount = passedCount;
            this.failedCount = failedCount;
            this.skippedCount = skippedCount;
            this.passRate = passRate;
        }

        // Getters
        public int getTotalCount() { return totalCount; }
        public int getPassedCount() { return passedCount; }
        public int getFailedCount() { return failedCount; }
        public int getSkippedCount() { return skippedCount; }
        public double getPassRate() { return passRate; }

        public double getPassRatePercentage() {
            return passRate * 100;
        }

        @Override
        public String toString() {
            return String.format(
                "TestStatistics{totalCount=%d, passed=%d, failed=%d, skipped=%d, passRate=%.2f%%}",
                totalCount, passedCount, failedCount, skippedCount, getPassRatePercentage()
            );
        }
    }
}