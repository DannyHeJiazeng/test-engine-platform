package com.test.web.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 告警记录实体
 */
@Data
@Entity
@Table(name = "alert_record")
public class AlertRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 告警类型
     */
    @Column(name = "alert_type", length = 30)
    private String alertType;

    /**
     * 告警标题
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * 告警内容
     */
    @Column(name = "content", length = 2000)
    private String content;

    /**
     * 告警级别
     */
    @Column(name = "level", length = 20)
    private String level;

    /**
     * 来源
     */
    @Column(name = "source", length = 100)
    private String source;

    /**
     * 关联的执行 ID
     */
    @Column(name = "related_execution_id", length = 50)
    private String relatedExecutionId;

    /**
     * 是否已发送
     */
    @Column(name = "sent")
    private Boolean sent;

    /**
     * 发送时间
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * 发送渠道
     */
    @Column(name = "sent_channels", length = 200)
    private String sentChannels;

    /**
     * 是否已确认
     */
    @Column(name = "acknowledged")
    private Boolean acknowledged;

    /**
     * 确认时间
     */
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    /**
     * 确认人
     */
    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}