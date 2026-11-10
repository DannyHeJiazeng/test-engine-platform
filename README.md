# Test Engine Platform

一站式测试中台平台 - 质量门禁自动化、测试用例执行、性能测试、发布后监控

## 项目简介

Test Engine Platform 是一个综合性的测试中台系统，提供从代码提交到发布后的全流程质量管控能力。

### 核心功能

- **测试用例执行**：支持 TestNG 测试用例的动态加载和执行
- **自定义注解分类**：通过 `@FunctionalTest`、`@PerformanceTest`、`@Inspection` 注解区分测试类型
- **质量门禁**：定义质量阈值，自动检查测试通过率
- **JMeter 集成**：内置 JMeter DSL，支持性能和并发测试
- **发布后监控**：定时执行巡检用例，失败自动告警
- **Web 控制台**：提供 REST API 和管理界面

## 项目结构

```
test-engine-platform/
├── test-annotation/       # 注解模块
├── test-core/             # 核心引擎模块
├── test-performance/      # 性能测试模块
├── test-middleware/       # 中台服务模块
├── test-monitor/          # 监控模块
├── test-web/              # Web 控制台
└── test-examples/         # 示例测试用例
```

## 技术栈

| 组件 | 技术选型 |
|------|---------|
| 框架 | Spring Boot 3.x |
| 测试框架 | TestNG 7.9+ |
| 性能测试 | jmeter-java-dsl |
| Git 操作 | JGit |
| 数据库 | MySQL / H2 |
| 前端 | Vue 3 + Element Plus |
| 容器 | Docker |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose (可选)

### 本地运行

```bash
# 克隆项目
git clone https://github.com/your-org/test-engine-platform.git
cd test-engine-platform

# 构建项目
mvn clean install -DskipTests

# 运行 Web 模块
cd test-web
mvn spring-boot:run
```

### Docker 部署

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f test-engine
```

## API 接口

### 测试执行

```bash
# 执行测试
POST /api/test/run
{
  "packagePath": "com.example.functional",
  "groups": ["functional"],
  "checkQualityGate": true
}

# 执行功能测试
POST /api/test/run/functional?packagePath=com.example.functional

# 执行性能测试
POST /api/test/run/performance?packagePath=com.example.performance

# 执行巡检测试
POST /api/test/run/inspection?packagePath=com.example.inspection
```

### 性能测试

```bash
# 执行性能测试
POST /api/performance/run
{
  "url": "http://localhost:8080/api/users",
  "threads": 10,
  "rampUpSeconds": 5,
  "durationSeconds": 60
}

# 并发测试
POST /api/performance/run/concurrency?url=http://localhost:8080&concurrency=100

# 生成 JMX 文件
POST /api/performance/jmx/generate
```

### 质量门禁

```bash
# 获取质量门禁状态
GET /api/quality-gate/status

# 获取历史记录
GET /api/quality-gate/history
```

### 巡检管理

```bash
# 获取所有巡检任务
GET /api/inspection/tasks

# 创建巡检任务
POST /api/inspection/tasks

# 健康检查
GET /api/inspection/health-checks

# 获取告警历史
GET /api/inspection/alerts
```

## 自定义注解使用

### 功能测试注解

```java
@FunctionalTest(description = "用户登录测试", priority = "P0", tags = {"login", "smoke"})
@Test(groups = "functional")
public void testLogin() {
    // 测试逻辑
}
```

### 性能测试注解

```java
@PerformanceTest(description = "API响应时间测试", maxDurationMs = 1000, concurrency = 10)
@Test(groups = "performance")
public void testApiResponseTime() {
    // 测试逻辑
}
```

### 巡检注解

```java
@Inspection(description = "系统健康检查", interval = "5m", alertOnFailure = true)
@Test(groups = "inspection")
public void testSystemHealth() {
    // 巡检逻辑
}
```

## 配置说明

### application.yml

```yaml
test-engine:
  workspace: ./test-workspace
  output-dir: ./test-results

  quality-gate:
    min-pass-rate: 0.8
    require-p0-pass: true
    max-failures: 0

  alert:
    dingtalk:
      enabled: true
      webhook-url: https://oapi.dingtalk.com/robot/send?access_token=xxx
```

## 开发指南

### 模块说明

1. **test-annotation**：定义自定义测试注解
2. **test-core**：TestNG 执行引擎，支持反射调用
3. **test-performance**：JMeter DSL 集成，性能测试
4. **test-middleware**：Git 服务、构建服务、质量门禁
5. **test-monitor**：巡检调度、健康检查、告警服务
6. **test-web**：REST API 控制器

### 扩展开发

```java
// 自定义测试类型
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTest {
    String description() default "";
    String customParam() default "";
}
```

## 许可证

MIT License

### 默认管理员账号

默认账号：`admin`，默认密码：`123456`
可通过 `TEST_ENGINE_ADMIN_USERNAME` 和 `TEST_ENGINE_ADMIN_PASSWORD_HASH` 覆盖默认配置。
