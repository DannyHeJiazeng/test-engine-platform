package com.test.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ID 生成器工具类
 */
public final class IdGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private IdGenerator() {}

    /**
     * 生成执行 ID
     */
    public static String generateExecutionId() {
        return "EXEC-" + LocalDateTime.now().format(DATE_FORMATTER) + "-" + randomSuffix();
    }

    /**
     * 生成测试 ID
     */
    public static String generateTestId() {
        return "TEST-" + LocalDateTime.now().format(DATE_FORMATTER) + "-" + randomSuffix();
    }

    /**
     * 生成性能测试 ID
     */
    public static String generatePerformanceId() {
        return "PERF-" + LocalDateTime.now().format(DATE_FORMATTER) + "-" + randomSuffix();
    }

    /**
     * 生成巡检任务 ID
     */
    public static String generateInspectionTaskId() {
        return "INSP-" + LocalDateTime.now().format(DATE_FORMATTER) + "-" + randomSuffix();
    }

    /**
     * 生成告警 ID
     */
    public static String generateAlertId() {
        return "ALERT-" + LocalDateTime.now().format(DATE_FORMATTER) + "-" + randomSuffix();
    }

    /**
     * 生成报告 ID
     */
    public static String generateReportId() {
        return "RPT-" + LocalDateTime.now().format(DATE_FORMATTER) + "-" + randomSuffix();
    }

    /**
     * 生成 UUID（无连字符）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成短 UUID
     */
    public static String shortUuid() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String randomSuffix() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}