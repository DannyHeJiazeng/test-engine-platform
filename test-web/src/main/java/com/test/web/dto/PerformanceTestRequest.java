package com.test.web.dto;

import lombok.Data;

import java.util.Map;

/**
 * 性能测试请求
 */
@Data
public class PerformanceTestRequest {

    /**
     * 测试 URL
     */
    private String url;

    /**
     * HTTP 方法
     */
    private String method = "GET";

    /**
     * 并发线程数
     */
    private int threads = 10;

    /**
     * 启动时间（秒）
     */
    private int rampUpSeconds = 10;

    /**
     * 持续时间（秒）
     */
    private int durationSeconds = 60;

    /**
     * 测试名称
     */
    private String testName;

    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * 请求体
     */
    private String requestBody;

    /**
     * 最大响应时间（毫秒）
     */
    private long maxResponseTimeMs = 5000;

    /**
     * 断言响应码
     */
    private int assertResponseCode = 200;
}