package com.test.performance.config;

import com.test.performance.JMeterService;

/**
 * Performance 模块配置
 * 这个配置类仅用于编译目的，因为test-performance模块不应该依赖Spring框架
 */
public class PerformanceConfig {

    /**
     * 配置 JMeter 服务
     */
    public JMeterService jMeterService() {
        return new JMeterService();
    }
}