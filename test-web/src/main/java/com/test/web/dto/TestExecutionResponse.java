package com.test.web.dto;

import com.test.core.TestResult;
import com.test.middleware.QualityGate;
import lombok.Data;

import java.util.List;

/**
 * 测试执行响应
 */
@Data
public class TestExecutionResponse {

    /**
     * 执行 ID
     */
    private String executionId;

    /**
     * 状态
     */
    private String status = "COMPLETED";

    /**
     * 总测试数
     */
    private int totalTests;

    /**
     * 通过数
     */
    private int passedTests;

    /**
     * 失败数
     */
    private int failedTests;

    /**
     * 测试结果列表
     */
    private List<TestResult> results;

    /**
     * 质量门禁结果
     */
    private QualityGate.QualityGateResult qualityGateResult;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 执行时间
     */
    private long executionTimeMs;
}