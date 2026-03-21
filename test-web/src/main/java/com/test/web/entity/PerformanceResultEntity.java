package com.test.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 性能测试结果实体
 */
@Data
@Entity
@Table(name = "performance_result")
public class PerformanceResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 测试 ID
     */
    @Column(name = "test_id", unique = true, length = 50)
    private String testId;

    /**
     * 测试名称
     */
    @Column(name = "test_name", length = 200)
    private String testName;

    /**
     * 测试 URL
     */
    @Column(name = "test_url", length = 500)
    private String testUrl;

    /**
     * 状态
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 总请求数
     */
    @Column(name = "total_requests")
    private Long totalRequests;

    /**
     * 成功请求数
     */
    @Column(name = "success_requests")
    private Long successRequests;

    /**
     * 失败请求数
     */
    @Column(name = "failed_requests")
    private Long failedRequests;

    /**
     * 错误率
     */
    @Column(name = "error_rate")
    private Double errorRate;

    /**
     * 平均响应时间
     */
    @Column(name = "avg_response_time")
    private Double avgResponseTime;

    /**
     * TP90 响应时间
     */
    @Column(name = "tp90_response_time")
    private Long tp90ResponseTime;

    /**
     * TP95 响应时间
     */
    @Column(name = "tp95_response_time")
    private Long tp95ResponseTime;

    /**
     * TP99 响应时间
     */
    @Column(name = "tp99_response_time")
    private Long tp99ResponseTime;

    /**
     * TPS
     */
    @Column(name = "tps")
    private Double tps;

    /**
     * 并发用户数
     */
    @Column(name = "concurrent_users")
    private Integer concurrentUsers;

    /**
     * 测试持续时间
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * 失败消息
     */
    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

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