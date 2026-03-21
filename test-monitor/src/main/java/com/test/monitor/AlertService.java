package com.test.monitor;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警服务 - 简化版本（无需外部依赖）
 */
@Service
public class AlertService {

    private final Map<String, AlertChannel> channels = new ConcurrentHashMap<>();
    private final List<AlertMessage> alertHistory = new ArrayList<>();

    public AlertService() {
        // 初始化默认的空实现渠道
        registerChannel("dingtalk", new EmptyAlertChannel("DingTalk"));
        registerChannel("wecom", new EmptyAlertChannel("WeCom"));
        registerChannel("email", new EmptyAlertChannel("Email"));
        registerChannel("http", new EmptyAlertChannel("HTTP"));
    }

    /**
     * 注册告警渠道
     */
    public void registerChannel(String name, AlertChannel channel) {
        channels.put(name, channel);
        System.out.println("Alert channel registered: " + name);
    }

    /**
     * 发送告警
     */
    public void sendAlert(AlertMessage alert) {
        System.out.println("Sending alert: " + alert.getType() + " - " + alert.getTitle());

        synchronized (alertHistory) {
            alertHistory.add(alert);
            if (alertHistory.size() > 1000) {
                alertHistory.remove(0);
            }
        }

        try {
            for (AlertChannel channel : channels.values()) {
                if (channel.isEnabled()) {
                    channel.send(alert);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send alert: " + e.getMessage());
        }
    }

    /**
     * 发送系统告警
     */
    public void sendSystemAlert(String message) {
        AlertMessage alert = new AlertMessage();
        alert.setType(AlertType.SYSTEM);
        alert.setTitle("系统告警");
        alert.setContent(message);
        alert.setLevel(AlertLevel.ERROR);
        alert.setTimestamp(java.time.LocalDateTime.now());
        sendAlert(alert);
    }

    /**
     * 配置钉钉告警 - 简化版
     */
    public void configureDingTalk(String webhookUrl, String secret) {
        System.out.println("DingTalk alert configured: " + webhookUrl);
    }

    /**
     * 配置企业微信告警 - 简化版
     */
    public void configureWeCom(String webhookUrl) {
        System.out.println("WeCom alert configured: " + webhookUrl);
    }

    /**
     * 配置邮件告警 - 简化版
     */
    public void configureEmail(String smtpHost, int smtpPort, String username,
                               String password, List<String> receivers) {
        System.out.println("Email alert configured: " + smtpHost);
    }

    /**
     * 获取告警历史
     */
    public List<AlertMessage> getAlertHistory(int limit) {
        synchronized (alertHistory) {
            int size = alertHistory.size();
            int fromIndex = Math.max(0, size - limit);
            return new ArrayList<>(alertHistory.subList(fromIndex, size));
        }
    }

    /**
     * 清除告警历史
     */
    public void clearAlertHistory() {
        alertHistory.clear();
    }

    /**
     * 告警渠道接口
     */
    public interface AlertChannel {
        void send(AlertMessage alert);
        boolean isEnabled();
        void setEnabled(boolean enabled);
    }

    /**
     * 空实现告警渠道（占位符）
     */
    @Data
    public static class EmptyAlertChannel implements AlertChannel {
        private String name;
        private boolean enabled = false;

        public EmptyAlertChannel(String name) {
            this.name = name;
        }

        @Override
        public void send(AlertMessage alert) {
            System.out.println("Empty channel [" + name + "] sending: " + alert.getTitle());
        }
    }
}
