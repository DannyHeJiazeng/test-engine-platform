# Test Engine Platform UI

Vue 3 + Element Plus 前端管理控制台

## 技术栈

- **框架**: Vue 3.4
- **UI 组件**: Element Plus 2.6
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.3
- **HTTP 客户端**: Axios 1.6
- **图表**: ECharts 5.5
- **构建工具**: Vite 5.2

## 功能模块

- **工作台**: 测试统计、质量门禁状态、测试趋势图、快捷操作
- **测试执行**: 同步/异步执行测试、按类型执行、质量门禁检查
- **执行历史**: 查看历史执行记录、测试详情
- **质量门禁**: 配置质量阈值、查看门禁历史、通过率趋势
- **巡检监控**: 管理巡检任务、查看告警历史
- **性能测试**: 并发测试、梯度并发、负载测试、结果分析
- **报告中心**: 测试报告详情、历史趋势
- **系统设置**: 基础配置、告警配置、Maven 配置

## 快速开始

### 安装依赖

```bash
cd test-web-ui
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

构建产物输出到 `dist/` 目录

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
test-web-ui/
├── src/
│   ├── api/              # API 接口
│   │   ├── test.js
│   │   ├── quality.js
│   │   ├── inspection.js
│   │   └── performance.js
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── layouts/          # 布局组件
│   │   └── MainLayout.vue
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # Pinia 状态管理
│   │   └── user.js
│   ├── utils/            # 工具函数
│   │   └── request.js
│   ├── views/            # 页面组件
│   │   ├── Login.vue
│   │   ├── Dashboard.vue
│   │   ├── Settings.vue
│   │   ├── test/
│   │   │   ├── Execute.vue
│   │   │   └── History.vue
│   │   ├── quality/
│   │   │   ├── Gate.vue
│   │   │   └── History.vue
│   │   ├── inspection/
│   │   │   ├── Tasks.vue
│   │   │   └── Alerts.vue
│   │   ├── performance/
│   │   │   └── Index.vue
│   │   └── report/
│   │       └── Detail.vue
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
├── vite.config.js
└── .gitignore
```

## API 代理配置

开发环境下，API 请求会代理到后端服务 `http://localhost:8080`

修改 `vite.config.js` 中的代理配置：

```js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 默认登录账号

- 用户名：`admin`
- 密码：`admin123`

## 页面预览

### 登录页
渐变紫色背景，简洁的登录表单

### 工作台
- 4 个统计卡片：总测试数、通过测试、失败测试、通过率
- 质量门禁状态展示
- 最近执行时间线
- 测试趋势图表
- 快捷操作按钮

### 测试执行
- 同步/异步执行选择
- 测试类型选择
- 包路径/类名输入
- 质量门禁配置
- 实时执行结果展示

### 质量门禁
- 通过率滑块配置
- 预设配置快捷应用（默认/宽松/严格）
- 门禁历史趋势图

### 巡检任务
- 任务列表表格
- 新建/编辑任务对话框
- 立即执行功能

### 性能测试
- 多种测试类型选择
- 实时测试结果展示
- 响应时间趋势图

## 注意事项

1. 确保后端服务已启动在 `http://localhost:8080`
2. 首次运行需要先安装 Node.js 18+
3. 生产环境部署需要将 `dist` 目录复制到后端 `src/main/resources/static`
