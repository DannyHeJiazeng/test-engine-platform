package com.test.web.config;

import com.test.performance.JMeterService;
import com.test.performance.JmxBuilder;
import com.test.performance.PerformanceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 性能测试模块配置
 * 确保性能测试相关Bean被正确注册
 */
@Configuration
public class PerformanceModuleConfig {

    @Bean
    @Primary
    public JmxBuilder jmxBuilder() {
        return new JmxBuilder();
    }

    @Bean
    @Primary
    public JMeterService jMeterService() {
        return new JMeterService();
    }

    @Bean
    @Primary
    public PerformanceExecutor performanceExecutor(JMeterService jMeterService, JmxBuilder jmxBuilder) {
        return new PerformanceExecutor(jMeterService, jmxBuilder);
    }
}