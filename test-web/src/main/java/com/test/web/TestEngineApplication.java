package com.test.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Test Engine Platform - Web Application
 * 一站式测试中台平台
 */
@SpringBootApplication(scanBasePackages = {"com.test", "com.test.performance", "com.test.web"})
@EnableAsync
public class TestEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestEngineApplication.class, args);
    }
}