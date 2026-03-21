package com.test.web.service;

import com.test.middleware.QualityGate;
import com.test.web.entity.TestExecutionEntity;
import com.test.web.repository.TestExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TestExecutionRepository executionRepository;

    /**
     * 生成测试报告
     */
    public TestReport generateReport(String executionId) {
        TestExecutionEntity execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        TestReport report = new TestReport();
        report.setExecutionId(executionId);
        report.setTestType(execution.getTestType());
        report.setStatus(execution.getStatus());
        report.setTotalTests(execution.getTotalTests());
        report.setPassedTests(execution.getPassedTests());
        report.setFailedTests(execution.getFailedTests());
        report.setSkippedTests(execution.getSkippedTests());
        report.setPassRate(execution.getPassRate());
        report.setDurationMs(execution.getDurationMs());
        report.setCreatedAt(execution.getCreatedAt());
        report.setQualityGatePassed(execution.getQualityGatePassed());

        return report;
    }

    /**
     * 生成汇总报告
     */
    public SummaryReport generateSummaryReport(LocalDateTime start, LocalDateTime end) {
        List<TestExecutionEntity> executions = executionRepository.findByCreatedAtBetween(start, end);

        SummaryReport report = new SummaryReport();
        report.setStartTime(start);
        report.setEndTime(end);
        report.setTotalExecutions(executions.size());

        if (!executions.isEmpty()) {
            // 统计各类型数量
            Map<String, Long> typeCounts = new HashMap<>();
            executions.forEach(e -> {
                String type = e.getTestType() != null ? e.getTestType() : "UNKNOWN";
                typeCounts.merge(type, 1L, Long::sum);
            });
            report.setExecutionsByType(typeCounts);

            // 统计总数
            report.setTotalTests(executions.stream()
                    .mapToLong(e -> e.getTotalTests() != null ? e.getTotalTests() : 0).sum());
            report.setTotalPassed(executions.stream()
                    .mapToLong(e -> e.getPassedTests() != null ? e.getPassedTests() : 0).sum());
            report.setTotalFailed(executions.stream()
                    .mapToLong(e -> e.getFailedTests() != null ? e.getFailedTests() : 0).sum());

            // 平均通过率
            report.setAveragePassRate(executions.stream()
                    .filter(e -> e.getPassRate() != null)
                    .mapToDouble(TestExecutionEntity::getPassRate)
                    .average().orElse(0.0));

            // 质量门禁通过率
            long gatePassed = executions.stream()
                    .filter(e -> Boolean.TRUE.equals(e.getQualityGatePassed()))
                    .count();
            report.setQualityGatePassRate((double) gatePassed / executions.size());
        }

        return report;
    }

    /**
     * 生成趋势报告
     */
    public TrendReport generateTrendReport(int days) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        List<Object[]> dailyStats = executionRepository.countByDate(start, end);

        TrendReport report = new TrendReport();
        report.setDays(days);

        List<DailyStatistics> dailyData = dailyStats.stream()
                .map(row -> {
                    DailyStatistics stats = new DailyStatistics();
                    stats.setDate(row[0].toString());
                    stats.setExecutionCount(((Number) row[1]).longValue());
                    return stats;
                })
                .toList();

        report.setDailyStatistics(dailyData);

        return report;
    }

    /**
     * 生成 HTML 报告
     */
    public String generateHtmlReport(String executionId) {
        TestReport report = generateReport(executionId);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <title>测试报告 - ").append(executionId).append("</title>\n");
        html.append("  <style>\n");
        html.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("    .header { background: #2196F3; color: white; padding: 20px; }\n");
        html.append("    .summary { margin: 20px 0; }\n");
        html.append("    .summary table { border-collapse: collapse; width: 100%; }\n");
        html.append("    .summary th, .summary td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("    .pass { color: green; }\n");
        html.append("    .fail { color: red; }\n");
        html.append("    .pass-rate { font-size: 24px; font-weight: bold; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // 头部
        html.append("  <div class=\"header\">\n");
        html.append("    <h1>测试执行报告</h1>\n");
        html.append("    <p>Execution ID: ").append(executionId).append("</p>\n");
        html.append("    <p>执行时间: ").append(report.getCreatedAt()).append("</p>\n");
        html.append("  </div>\n");

        // 摘要
        html.append("  <div class=\"summary\">\n");
        html.append("    <h2>执行摘要</h2>\n");
        html.append("    <table>\n");
        html.append("      <tr><th>指标</th><th>值</th></tr>\n");
        html.append("      <tr><td>状态</td><td>").append(report.getStatus()).append("</td></tr>\n");
        html.append("      <tr><td>总测试数</td><td>").append(report.getTotalTests()).append("</td></tr>\n");
        html.append("      <tr><td>通过数</td><td class=\"pass\">").append(report.getPassedTests()).append("</td></tr>\n");
        html.append("      <tr><td>失败数</td><td class=\"fail\">").append(report.getFailedTests()).append("</td></tr>\n");
        html.append("      <tr><td>跳过数</td><td>").append(report.getSkippedTests()).append("</td></tr>\n");
        html.append("      <tr><td>通过率</td><td class=\"pass-rate\">")
                .append(String.format("%.2f%%", report.getPassRate() * 100)).append("</td></tr>\n");
        html.append("      <tr><td>质量门禁</td><td>")
                .append(Boolean.TRUE.equals(report.getQualityGatePassed()) ? "✅ 通过" : "❌ 失败")
                .append("</td></tr>\n");
        html.append("    </table>\n");
        html.append("  </div>\n");

        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    /**
     * 测试报告
     */
    @lombok.Data
    public static class TestReport {
        private String executionId;
        private String testType;
        private String status;
        private Integer totalTests;
        private Integer passedTests;
        private Integer failedTests;
        private Integer skippedTests;
        private Double passRate;
        private Long durationMs;
        private LocalDateTime createdAt;
        private Boolean qualityGatePassed;
    }

    /**
     * 汇总报告
     */
    @lombok.Data
    public static class SummaryReport {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int totalExecutions;
        private Map<String, Long> executionsByType;
        private long totalTests;
        private long totalPassed;
        private long totalFailed;
        private double averagePassRate;
        private double qualityGatePassRate;
    }

    /**
     * 趋势报告
     */
    @lombok.Data
    public static class TrendReport {
        private int days;
        private List<DailyStatistics> dailyStatistics;
    }

    /**
     * 每日统计
     */
    @lombok.Data
    public static class DailyStatistics {
        private String date;
        private long executionCount;
    }
}