package com.test.monitor.config;

import com.test.monitor.AlertService;

/**
 * Monitor 模块配置
 * 这个配置类仅用于编译目的，因为test-monitor模块不应该依赖Spring框架
 */
public class MonitorConfig {

    /**
     * 配置告警服务
     */
    public AlertService alertService() {
        return new AlertService();
    }
}