package com.test.web.dto;

import com.test.monitor.AlertLevel;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 巡检任务请求
 */
@Data
public class InspectionTaskRequest {

    /**
     * 任务 ID
     */
    @NotBlank
    @Size(max = 50)
    private String taskId;

    private String taskName;

    /**
     * 包路径
     */
    @NotBlank
    private String packagePath;

    /**
     * 执行间隔（秒）
     */
    @Min(1)
    private int intervalSeconds = 300;

    /**
     * 失败时是否告警
     */
    private boolean alertOnFailure = true;

    /**
     * 告警级别
     */
    @NotNull
    private AlertLevel alertLevel = AlertLevel.WARNING;
}