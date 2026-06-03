package com.test.core.config;

import com.test.core.TestNGRunner;
import com.test.core.TestResultListener;

/**
 * Test Core 模块配置
 * 这个配置类仅用于编译目的，因为test-core模块不应该依赖Spring框架
 */
public class TestCoreConfig {

    /**
     * 配置 TestNG Runner
     */
    public TestNGRunner testNGRunner() {
        return new TestNGRunner();
    }

    /**
     * 配置测试结果监听器
     */
    public TestResultListener testResultListener() {
        return new TestResultListener();
    }
}