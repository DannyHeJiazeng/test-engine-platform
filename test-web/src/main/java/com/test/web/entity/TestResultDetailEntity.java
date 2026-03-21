package com.test.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 测试结果详情实体
 */
@Data
@Entity
@Table(name = "test_result_detail")
public class TestResultDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的执行记录 ID
     */
    @Column(name = "execution_id", nullable = false, length = 50)
    private String executionId;

    /**
     * 测试类名
     */
    @Column(name = "class_name", length = 200)
    private String className;

    /**
     * 测试方法名
     */
    @Column(name = "method_name", length = 100)
    private String methodName;

    /**
     * 测试状态：SUCCESS, FAILURE, SKIP
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 测试描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 优先级
     */
    @Column(name = "priority", length = 5)
    private String priority;

    /**
     * 执行时间（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 失败消息
     */
    @Column(name = "failure_message", length = 2000)
    private String failureMessage;

    /**
     * 堆栈跟踪
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}