package com.test.web.dto;

import com.test.middleware.QualityGate;
import lombok.Data;

import java.util.List;

/**
 * 测试执行请求
 */
@Data
public class TestExecutionRequest {

    /**
     * 测试类全限定名
     */
    private String testClass;

    /**
     * 包路径
     */
    private String packagePath;

    /**
     * TestNG groups
     */
    private String[] groups;

    /**
     * 是否执行质量门禁检查
     */
    private boolean checkQualityGate = true;

    /**
     * 质量阈值配置
     */
    private QualityGate.QualityThreshold threshold;

    /**
     * 并发线程数
     */
    private int threadPoolSize = 10;

    /**
     * 是否异步执行
     */
    private boolean async = false;
}