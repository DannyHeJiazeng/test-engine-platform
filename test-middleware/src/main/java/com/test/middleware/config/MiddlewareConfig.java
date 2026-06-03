package com.test.middleware.config;

import com.test.middleware.QualityGate;
import com.test.middleware.service.QualityGateService;

/**
 * Middleware 模块配置
 * 这个配置类仅用于编译目的，因为test-middleware模块不应该依赖Spring框架
 */
public class MiddlewareConfig {

    /**
     * 配置质量门禁
     */
    public QualityGate qualityGate() {
        return new QualityGate();
    }

    /**
     * 配置质量门禁服务
     */
    public QualityGateService qualityGateService(QualityGate qualityGate) {
        return new QualityGateService(qualityGate);
    }
}