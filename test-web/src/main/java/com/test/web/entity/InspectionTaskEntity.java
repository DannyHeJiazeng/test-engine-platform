package com.test.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 巡检任务实体
 */
@Data
@Entity
@Table(name = "inspection_task")
public class InspectionTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务 ID
     */
    @Column(name = "task_id", unique = true, nullable = false, length = 50)
    private String taskId;

    /**
     * 任务名称
     */
    @Column(name = "task_name", length = 200)
    private String taskName;

    /**
     * 包路径
     */
    @Column(name = "package_path", length = 500)
    private String packagePath;

    /**
     * 执行间隔（秒）
     */
    @Column(name = "interval_seconds")
    private Integer intervalSeconds;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled;

    /**
     * 失败时告警
     */
    @Column(name = "alert_on_failure")
    private Boolean alertOnFailure;

    /**
     * 告警级别
     */
    @Column(name = "alert_level", length = 20)
    private String alertLevel;

    /**
     * 最后执行时间
     */
    @Column(name = "last_run_time")
    private LocalDateTime lastRunTime;

    /**
     * 最后执行状态
     */
    @Column(name = "last_run_status", length = 20)
    private String lastRunStatus;

    /**
     * 总执行次数
     */
    @Column(name = "total_runs")
    private Integer totalRuns;

    /**
     * 成功次数
     */
    @Column(name = "success_count")
    private Integer successCount;

    /**
     * 失败次数
     */
    @Column(name = "failure_count")
    private Integer failureCount;

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