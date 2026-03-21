package com.test.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 告警消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {

    private AlertType type;
    private String title;
    private String content;
    private AlertLevel level;
    private LocalDateTime timestamp;
    private List<String> tags = new ArrayList<>();
    private Map<String, Object> data;
    private List<String> receivers;
}
