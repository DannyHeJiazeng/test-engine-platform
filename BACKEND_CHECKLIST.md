# 后端功能检查报告

## 检查日期
2026-03-16

## 模块概述

test-web 模块是测试引擎平台的 Web 后端服务，基于 Spring Boot 3.2.4 构建，提供 RESTful API 接口。

## 控制器清单 (Controllers)

| 控制器 | 路径 | 功能描述 | 状态 |
|--------|------|----------|------|
| TestExecutionController | /api/test | 测试执行（同步/异步/按类型） | 通过 |
| QualityGateController | /api/quality-gate | 质量门禁配置和历史 | 通过 |
| InspectionController | /api/inspection | 巡检任务、健康检查、告警 | 通过 |
| PerformanceController | /api/performance | 性能测试执行和结果 | 通过 |
| ReportController | /api/report | 测试报告生成和查询 | 通过 |
| MonitorController | /api/monitor | 监控指标和告警历史 | 通过 |
| SystemController | /api/system | 系统信息和健康检查 | 通过 |

## 配置类清单 (Configuration)

| 配置类 | 功能描述 | 状态 |
|--------|----------|------|
| WebMvcConfig | MVC 配置、资源映射、视图转发 | 通过 |
| CorsConfig | CORS 跨域配置 | 通过 |
| OpenApiConfig | Swagger/OpenAPI 文档配置 | 通过 |
| AsyncConfig | 异步任务线程池配置 | 通过 |
| SpringConfig | Spring 通用配置（调度、线程池） | 通过 |
| GlobalExceptionHandler | 全局异常处理器 | 通过 |

## 拦截器和过滤器

| 组件 | 功能 | 路径 |
|------|------|------|
| RateLimitInterceptor | API 限流（100 次/分钟） | /api/** |
| CorsFilter | 跨域请求处理 | /api/** |

## 服务层 (Services)

| 服务类 | 功能描述 |
|--------|----------|
| TestExecutionService | 测试执行服务（同步/异步） |
| PerformanceTestService | 性能测试服务 |
| InspectionService | 巡检服务 |
| ReportService | 报告生成服务（HTML/汇总/趋势） |

## 实体类 (Entities)

| 实体类 | 表名 | 功能 |
|--------|------|------|
| TestExecutionEntity | test_execution | 测试执行记录 |
| TestResultDetailEntity | test_result_detail | 测试结果详情 |
| PerformanceResultEntity | performance_result | 性能测试结果 |
| InspectionTaskEntity | inspection_task | 巡检任务 |
| AlertRecordEntity | alert_record | 告警记录 |

## Repository 层

| Repository | 管理的实体 | 主要方法 |
|------------|-----------|----------|
| TestExecutionRepository | TestExecutionEntity | findById, findByStatus, findByTestType, countByDate |
| TestResultDetailRepository | TestResultDetailEntity | findByExecutionId, deleteByExecutionId |
| PerformanceResultRepository | PerformanceResultEntity | - |
| InspectionTaskRepository | InspectionTaskEntity | - |
| AlertRecordRepository | AlertRecordEntity | - |

## DTO (数据传输对象)

| DTO | 用途 |
|-----|------|
| ApiResponse<T> | 通用 API 响应封装 |
| TestExecutionRequest | 测试执行请求 |
| TestExecutionResponse | 测试执行响应 |
| PerformanceTestRequest | 性能测试请求 |
| InspectionTaskRequest | 巡检任务请求 |

## 异常处理

| 异常类 | 用途 |
|--------|------|
| BusinessException | 业务异常（支持错误码和消息） |

全局异常处理器支持：
- BusinessException → 400 BAD_REQUEST
- MethodArgumentNotValidException → 400 BAD_REQUEST（参数校验失败）
- IllegalArgumentException → 400 BAD_REQUEST
- RuntimeException → 500 INTERNAL_SERVER_ERROR
- Exception → 500 INTERNAL_SERVER_ERROR

## API 接口汇总

### 测试执行 API (/api/test)
- POST /run - 执行测试
- POST /run-async - 异步执行测试
- POST /run/functional - 执行功能测试
- POST /run/performance - 执行性能测试
- POST /run/inspection - 执行巡检测试
- GET /status/{executionId} - 查询执行状态
- GET /history - 获取执行历史

### 质量门禁 API (/api/quality-gate)
- GET /status - 获取质量门禁状态
- GET /history - 获取质量门禁历史
- DELETE /history - 清除历史记录
- GET /threshold/default - 获取默认阈值
- GET /threshold/relaxed - 获取宽松阈值
- GET /threshold/strict - 获取严格阈值

### 巡检监控 API (/api/inspection)
- GET /tasks - 获取所有巡检任务
- POST /tasks - 创建巡检任务
- DELETE /tasks/{taskId} - 取消巡检任务
- POST /tasks/{taskId}/execute - 立即执行巡检任务
- GET /tasks/{taskId} - 获取任务详情
- POST /health-checks - 注册健康检查
- GET /health-checks/{name} - 执行健康检查
- GET /health-checks - 执行所有健康检查
- GET /health-summary - 获取健康状态摘要
- GET /alerts - 获取告警历史
- POST /alerts/test - 发送测试告警
- DELETE /alerts - 清除告警历史
- GET /metrics/summary - 获取指标摘要
- GET /metrics - 获取所有指标
- GET /metrics/prometheus - 导出 Prometheus 格式指标
- DELETE /metrics - 清除指标

### 性能测试 API (/api/performance)
- POST /run - 执行性能测试
- POST /run/concurrency - 执行并发测试
- POST /run/gradual - 执行梯度并发测试
- POST /run/load - 执行负载测试
- POST /jmx/generate - 生成 JMX 文件
- GET /result/{testId} - 获取测试结果
- POST /analyze/{testId} - 分析测试结果
- GET /results - 获取所有结果
- DELETE /results - 清除结果

### 报告 API (/api/report)
- GET /{executionId} - 获取测试报告
- GET /{executionId}/html - 获取 HTML 报告
- GET /summary - 获取汇总报告
- GET /trend - 获取趋势报告

### 监控 API (/api/monitor)
- GET /alerts - 获取告警历史
- GET /health - 获取健康检查列表
- POST /health/check/{name} - 执行健康检查
- GET /health/summary - 获取健康摘要
- GET /metrics/summary - 获取指标摘要
- GET /metrics - 获取所有指标
- GET /metrics/prometheus - 导出 Prometheus 格式指标
- GET /inspection/tasks - 获取巡检任务列表
- POST /inspection/tasks - 创建巡检任务
- DELETE /inspection/tasks/{taskId} - 删除巡检任务
- POST /inspection/tasks/{taskId}/execute - 立即执行巡检任务
- DELETE /alerts - 清除告警历史
- DELETE /metrics - 清除指标

### 系统 API (/api/system)
- GET /info - 获取系统信息
- GET /health - 健康检查
- GET /uptime - 获取运行时长

## 配置文件

### application.yml 关键配置

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop

  h2:
    console:
      enabled: true
      path: /h2-console

test-engine:
  quality-gate:
    min-pass-rate: 0.8
    require-p0-pass: true
    max-failures: 0
```

## 质量门禁功能

QualityGate 服务提供三种预设阈值配置：

### 默认阈值 (defaultThreshold)
- 最小通过率：80%
- 最大失败数：0
- 不允许失败：是
- P0 用例必须通过：是

### 宽松阈值 (relaxedThreshold)
- 最小通过率：60%
- 最大失败数：10
- 不允许失败：否
- P0 用例必须通过：否

### 严格阈值 (strictThreshold)
- 最小通过率：100%
- 最大失败数：0
- 不允许失败：是
- P0 用例必须通过：是

## 线程池配置

| 线程池 | 核心线程数 | 最大线程数 | 队列容量 | 用途 |
|--------|-----------|-----------|---------|------|
| asyncExecutor | 10 | 50 | 100 | 异步任务 |
| performanceExecutor | 5 | 20 | 50 | 性能测试 |
| testExecutor | 20 | 100 | 200 | 测试执行 |

## 功能验证清单

### 已验证的代码功能
- [x] TestExecutionController - 测试执行接口完整
- [x] QualityGateController - 质量门禁配置和检查完整
- [x] InspectionController - 巡检任务和告警完整
- [x] PerformanceController - 性能测试接口完整
- [x] ReportController - 报告生成接口完整
- [x] MonitorController - 监控指标接口完整
- [x] SystemController - 系统信息接口完整
- [x] GlobalExceptionHandler - 异常处理完整
- [x] CorsConfig - 跨域配置完整
- [x] OpenApiConfig - Swagger 文档配置完整
- [x] AsyncConfig - 异步配置完整
- [x] WebMvcConfig - MVC 配置完整
- [x] RateLimitInterceptor - 限流拦截器完整
- [x] TestExecutionService - 测试执行服务完整
- [x] ReportService - 报告服务完整
- [x] QualityGate - 质量门禁服务完整
- [x] 所有 Entity 类定义完整
- [x] 所有 Repository 接口定义完整
- [x] 所有 DTO 类定义完整

### 外部依赖依赖
- [ ] Java 17+ 运行环境（当前环境未安装）
- [ ] Maven 3.8+ 构建工具（当前环境未安装）
- [ ] Spring Boot 3.2.4 运行时

## 结论

**后端代码检查结果：通过**

所有控制器、服务、实体、Repository、DTO、配置类均已完整实现，代码结构清晰，符合 Spring Boot 最佳实践。

由于当前环境缺少 Java 和 Maven，无法直接启动验证。需要在安装了 JDK 17+ 和 Maven 的环境中执行以下命令启动：

```bash
# 在项目根目录执行
mvn clean install -DskipTests
cd test-web
mvn spring-boot:run
```

启动后访问：
- 应用首页：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui.html
- H2 控制台：http://localhost:8080/h2-console
