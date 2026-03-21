package com.test.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 测试执行记录实体
 */
@Data
@Entity
@Table(name = "test_execution")
public class TestExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 执行 ID
     */
    @Column(name = "execution_id", unique = true, nullable = false, length = 50)
    private String executionId;

    /**
     * 测试类型：FUNCTIONAL, PERFORMANCE, INSPECTION
     */
    @Column(name = "test_type", length = 20)
    private String testType;

    /**
     * 测试名称
     */
    @Column(name = "test_name", length = 200)
    private String testName;

    /**
     * 包路径
     */
    @Column(name = "package_path", length = 500)
    private String packagePath;

    /**
     * 测试类名
     */
    @Column(name = "class_name", length = 200)
    private String className;

    /**
     * 状态：RUNNING, COMPLETED, FAILED
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 总测试数
     */
    @Column(name = "total_tests")
    private Integer totalTests;

    /**
     * 通过数
     */
    @Column(name = "passed_tests")
    private Integer passedTests;

    /**
     * 失败数
     */
    @Column(name = "failed_tests")
    private Integer failedTests;

    /**
     * 跳过数
     */
    @Column(name = "skipped_tests")
    private Integer skippedTests;

    /**
     * 通过率
     */
    @Column(name = "pass_rate")
    private Double passRate;

    /**
     * 执行时间（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 质量门禁结果
     */
    @Column(name = "quality_gate_passed")
    private Boolean qualityGatePassed;

    /**
     * 错误消息
     */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    /**
     * 触发来源：MANUAL, WEBHOOK, SCHEDULE
     */
    @Column(name = "trigger_source", length = 20)
    private String triggerSource;

    /**
     * 触发者
     */
    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    /**
     * 构建信息
     */
    @Column(name = "build_info", length = 500)
    private String buildInfo;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}