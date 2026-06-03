package com.test.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.test.web.config.PerformanceConfig;

/**
 * Test Engine Platform - Web Application
 * 一站式测试中台平台
 */
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.test", "com.test.web"})
@Import(PerformanceConfig.class)
public class TestEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestEngineApplication.class, args);
    }
}