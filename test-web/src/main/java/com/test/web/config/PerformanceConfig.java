package com.test.web.config;

import com.test.performance.JMeterService;
import com.test.performance.JmxBuilder;
import com.test.performance.PerformanceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 性能测试相关配置
 * 显式创建性能测试相关的Bean以确保它们在Web应用上下文中可用
 */
@Configuration
public class PerformanceConfig {

    @Bean
    public JMeterService jMeterService() {
        return new JMeterService();
    }

    @Bean
    public JmxBuilder jmxBuilder() {
        return new JmxBuilder();
    }

    @Bean
    public PerformanceExecutor performanceExecutor() {
        return new PerformanceExecutor(new JMeterService(), new JmxBuilder());
    }
}