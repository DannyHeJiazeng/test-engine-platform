package com.test.web.dto;

import com.test.monitor.AlertLevel;
import lombok.Data;

/**
 * 巡检任务请求
 */
@Data
public class InspectionTaskRequest {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 包路径
     */
    private String packagePath;

    /**
     * 执行间隔（秒）
     */
    private int intervalSeconds = 300;

    /**
     * 失败时是否告警
     */
    private boolean alertOnFailure = true;

    /**
     * 告警级别
     */
    private AlertLevel alertLevel = AlertLevel.WARNING;
}