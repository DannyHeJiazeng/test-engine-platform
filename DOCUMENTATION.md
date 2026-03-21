# Test Engine Platform - 完整项目文档

## 目录

- [项目概述](#项目概述)
- [架构设计](#架构设计)
- [核心功能](#核心功能)
- [开发指南](#开发指南)
- [部署指南](#部署指南)
- [API 文档](#api-文档)
- [故障排查](#故障排查)
- [扩展开发](#扩展开发)

---

## 项目概述

### 项目简介

Test Engine Platform 是一个企业级的一站式测试中台平台，旨在解决测试流程中的质量管控问题。

### 核心价值

1. **统一测试管理** - 集中管理功能测试、性能测试、巡检用例
2. **自动化质量门禁** - 从代码提交到发布的全流程质量管控
3. **注解驱动分类** - 通过自定义注解快速标记和管理测试用例
4. **内嵌 JMeter 支持** - 无需独立部署 JMeter，直接执行性能测试
5. **发布后监控** - 持续监控线上质量，自动巡检告警
6. **可视化报告** - 提供丰富的测试报告和趋势分析

### 技术架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                           前端层 (Vue 3 + Element Plus)                │
├─────────────────────────────────────────────────────────────────────┤
│                          API 网关层 (Spring Web)                       │
├─────────────────────────────────────────────────────────────────────┤
│  测试执行  │ 性能测试  │ 质量门禁  │ 巡检监控  │ 报告生成          │
├─────────────────────────────────────────────────────────────────────┤
│  TestNG   │ JMeter   │ Git Ops  │ 告警服务  │ 指标收集           │
├─────────────────────────────────────────────────────────────────────┤
│                    数据存储层 (MySQL + Redis)                        │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 架构设计

### 模块划分

| 模块 | 职责 | 主要技术 |
|------|------|---------|
| test-annotation | 自定义注解定义 | Java Annotation |
| test-core | 核心测试引擎 | TestNG + Reflection |
| test-performance | 性能测试 | jmeter-java-dsl |
| test-middleware | 中台服务 | JGit + ProcessBuilder |
| test-monitor | 监控告警 | Spring Scheduler |
| test-web | Web 控制台 | Spring Boot + JPA |
| test-examples | 示例用例 | TestNG |

### 设计模式

1. **策略模式** - 测试类型选择执行策略
2. **工厂模式** - 创建不同类型的测试执行器
3. **观察者模式** - 测试结果监听和通知
4. **模板方法模式** - 测试执行流程标准化

---

## 核心功能

### 1. 注解驱动的测试分类

```java
// 功能测试
@FunctionalTest(description = "用户登录测试", priority = "P0", tags = {"login", "smoke"})
@Test
public void testLogin() { }

// 性能测试
@PerformanceTest(description = "API响应时间", maxDurationMs = 1000, concurrency = 10)
@Test
public void testApiPerformance() { }

// 巡检测试
@Inspection(description = "健康检查", interval = "5m", alertOnFailure = true)
@Test
public void testHealthCheck() { }
```

### 2. 质量门禁配置

```yaml
quality-gate:
  min-pass-rate: 0.8        # 最小通过率
  require-p0-pass: true    # P0 用例必须通过
  max-failures: 0          # 最大失败数
  critical-tests:          # 关键测试用例
    - com.example.CriticalTest.testCriticalFeature
```

### 3. JMeter 性能测试

```java
// 并发测试
POST /api/performance/run/concurrency?url=http://api.example.com/users&concurrency=100

// 梯度并发测试
POST /api/performance/run/gradual?url=http://api.example.com/users&start=10&end=500&step=10

// 负载测试
POST /api/performance/run/load?url=http://api.example.com/users&threads=100&duration=10
```

### 4. 巡检任务管理

```java
// 创建巡检任务
POST /api/inspection/tasks
{
  "taskId": "daily-health-check",
  "packagePath": "com.example.inspection",
  "intervalSeconds": 300,
  "alertOnFailure": true
}

// 立即执行
POST /api/inspection/tasks/{taskId}/execute
```

---

## 开发指南

### 环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.8+ |
| Docker | 20.10+ |
| Docker Compose | 2.0+ |

### 本地开发

```bash
# 1. 克隆项目
git clone https://github.com/your-org/test-engine-platform.git
cd test-engine-platform

# 2. 构建项目
mvn clean install -DskipTests

# 3. 启动数据库
docker-compose up -d mysql redis

# 4. 运行 Web 模块
cd test-web
mvn spring-boot:run

# 5. 访问控制台
open http://localhost:8080
```

### 编写测试用例

```java
package com.example.mytest;

import com.test.annotation.*;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestSuite(name = "我的测试套件", category = TestSuite.TestCategory.FUNCTIONAL)
public class MyTest {

    @FunctionalTest(description = "测试功能A", priority = "P0")
    @Test(groups = "functional")
    public void testFeatureA() {
        // 测试逻辑
        Assert.assertTrue(true);
    }

    @PerformanceTest(description = "测试API性能", maxDurationMs = 500, concurrency = 5)
    @Test(groups = "performance")
    public void testApiPerformance() {
        // 性能测试逻辑
        long startTime = System.currentTimeMillis();
        // 执行操作
        long duration = System.currentTimeMillis() - startTime;
        Assert.assertTrue(duration < 500);
    }
}
```

### 自定义注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTest {
    String description() default "";
    String customProperty() default "";
}
```

---

## 部署指南

### Docker 部署

```bash
# 构建镜像
docker build -t test-engine-platform:latest .

# 运行容器
docker run -d \
  --name test-engine \
  -p 8080:8080 \
  -v ./test-workspace:/app/test-workspace \
  -v ./logs:/app/logs \
  test-engine-platform:latest
```

### Docker Compose 部署

```bash
# 启动所有服务
docker-compose -f docker-compose.full.yml up -d

# 查看日志
docker-compose logs -f test-engine

# 停止服务
docker-compose down
```

### Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-engine-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: test-engine
  template:
    metadata:
      labels:
        app: test-engine
    spec:
      containers:
      - name: test-engine
        image: test-engine-platform:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

---

## API 文档

### 测试执行

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/test/run` | POST | 执行测试 |
| `/api/test/run-async` | POST | 异步执行测试 |
| `/api/test/run/functional` | POST | 执行功能测试 |
| `/api/test/run/performance` | POST | 执行性能测试 |
| `/api/test/run/inspection` | POST | 执行巡检测试 |
| `/api/test/status/{id}` | GET | 查询执行状态 |
| `/api/test/history` | GET | 获取执行历史 |

### 性能测试

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/performance/run` | POST | 执行性能测试 |
| `/api/performance/run/concurrency` | POST | 并发测试 |
| `/api/performance/run/gradual` | POST | 梯度并发测试 |
| `/api/performance/jmx/generate` | POST | 生成 JMX 文件 |
| `/api/performance/result/{id}` | GET | 获取结果 |
| `/api/performance/analyze/{id}` | POST | 分析结果 |

### 巡检管理

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/inspection/tasks` | GET | 获取任务列表 |
| `/api/inspection/tasks` | POST | 创建任务 |
| `/api/inspection/tasks/{id}` | DELETE | 删除任务 |
| `/api/inspection/tasks/{id}/execute` | POST | 立即执行 |
| `/api/inspection/health-checks` | GET | 健康检查 |
| `/api/inspection/alerts` | GET | 告警历史 |

### 质量门禁

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/quality-gate/status` | GET | 获取门禁状态 |
| `/api/quality-gate/history` | GET | 获取历史记录 |
| `/api/quality-gate/threshold/default` | GET | 默认阈值 |

### 报告中心

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/report/{id}` | GET | 获取报告 |
| `/api/report/{id}/html` | GET | HTML 报告 |
| `/api/report/summary` | GET | 汇总报告 |
| `/api/report/trend` | GET | 趋势报告 |

---

## 故障排查

### 常见问题

#### 1. 测试类找不到

**问题**: 执行测试时报 `ClassNotFoundException`

**解决方案**:
- 检查 `packagePath` 配置是否正确
- 确保测试类已编译到 `target/classes` 或 `target/test-classes`
- 验证类加载器配置

#### 2. JMeter 测试失败

**问题**: 性能测试无法执行

**解决方案**:
- 检查 `jmeter-java-dsl` 依赖是否正确引入
- 确保目标 URL 可访问
- 查看 JMeter 日志获取详细错误信息

#### 3. 巡检任务未执行

**问题**: 定时巡检任务没有触发

**解决方案**:
- 检查 `@Scheduled` 注解配置
- 确认 Spring Boot 启用了定时任务 (`@EnableScheduling`)
- 查看应用日志确认任务注册情况

#### 4. 数据库连接失败

**问题**: 应用启动时无法连接数据库

**解决方案**:
- 检查 MySQL 容器是否正常运行
- 验证数据库连接配置
- 确认网络连接正常

#### 5. 内存溢出

**问题**: 性能测试时出现 OOM

**解决方案**:
- 增加 JVM 堆内存: `-Xmx2g`
- 减少并发线程数
- 检查是否有内存泄漏

---

## 扩展开发

### 自定义测试类型

```java
// 1. 定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityTest {
    String description() default "";
    String severity() default "MEDIUM";
}

// 2. 扩展注解解析器
parser.parseType() 方法中添加新的测试类型解析

// 3. 扩展执行器
testNGRunner.runByType() 方法中添加新的执行逻辑
```

### 自定义告警渠道

```java
public class SlackChannel implements AlertService.AlertChannel {

    @Override
    public void send(AlertMessage alert) {
        // 实现 Slack 告警逻辑
        String webhookUrl = "https://hooks.slack.com/services/...";
        // 发送请求...
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

// 注册渠道
alertService.registerChannel("slack", new SlackChannel());
```

### 自定义质量规则

```java
public class CustomQualityRule {

    public QualityGateResult check(List<TestResult> results) {
        // 自定义质量检查逻辑
        // 例如：检查特定模块的通过率
        QualityGateResult result = new QualityGateResult();

        String module = "com.example.payment";
        double modulePassRate = calculateModulePassRate(results, module);

        if (modulePassRate < 0.95) {
            result.setPassed(false);
            result.setFailedChecks(List.of(
                "支付模块通过率低于 95%"
            ));
        }

        return result;
    }
}
```

### 集成第三方工具

```java
// 集成 SonarQube
@Service
public class SonarQubeService {

    public SonarQualityGate checkQuality(String projectKey) {
        String url = String.format(
            "%s/api/qualitygates/project_status?projectKey=%s",
            sonarUrl,
            projectKey
        );

        // 调用 SonarQube API...
        return parseResponse(response);
    }
}

// 集成 Allure
@Service
public class AllureReportService {

    public void generateReport(String executionId, List<TestResult> results) {
        // 生成 Allure 报告
        AllureTestNg testNg = new AllureTestNg();
        testNg.setAllureProperties(allureProperties);
        testNg.write(results);
    }
}
```

---

## 性能优化建议

### 1. 测试执行优化

- 使用并行执行提高效率
- 合理设置线程池大小
- 使用 TestNG 的 `@DataProvider` 减少重复代码

### 2. 数据库优化

- 使用连接池（HikariCP）
- 添加适当的索引
- 定期清理历史数据

### 3. 缓存优化

- 使用 Redis 缓存测试结果
- 缓存测试配置信息
- 使用本地缓存减少数据库查询

---

## 安全建议

1. **API 安全**
   - 启用 HTTPS
   - 使用 JWT 认证
   - 实现请求签名验证

2. **数据安全**
   - 敏感信息加密存储
   - 使用环境变量管理密钥
   - 定期备份数据库

3. **访问控制**
   - 实现基于角色的访问控制（RBAC）
   - 限制测试代码执行权限
   - 白名单机制管理远程仓库

---

## 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发流程

1. Fork 项目
2. 创建功能分支: `git checkout -b feature/xxx`
3. 提交更改: `git commit -m 'Add xxx'`
4. 推送分支: `git push origin feature/xxx`
5. 创建 Pull Request

### 代码规范

- 遵循 Alibaba Java 编码规范
- 使用 Checkstyle 进行代码检查
- 单元测试覆盖率 > 70%

---

## 许可证

MIT License