<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon :size="32"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTests }}</div>
              <div class="stat-label">总测试数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.passedTests }}</div>
              <div class="stat-label">通过测试</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon :size="32"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.failedTests }}</div>
              <div class="stat-label">失败测试</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.passRate }}%</div>
              <div class="stat-label">通过率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 质量门禁状态 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>质量门禁状态</span>
              <el-tag :type="qualityGate.passed ? 'success' : 'danger'">
                {{ qualityGate.passed ? '通过' : '未通过' }}
              </el-tag>
            </div>
          </template>
          <div class="quality-gate-info">
            <el-progress
              :percentage="qualityGate.passRate"
              :status="qualityGate.passed ? 'success' : 'exception'"
            />
            <div class="quality-details">
              <p>总测试数：{{ qualityGate.totalTests }}</p>
              <p>通过数：{{ qualityGate.passedTests }}</p>
              <p>失败数：{{ qualityGate.failedTests }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近执行</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="item in recentExecutions"
              :key="item.id"
              :timestamp="item.time"
              placement="top"
              :type="item.status === 'success' ? 'success' : 'danger'"
            >
              <el-card>
                <p>{{ item.name }}</p>
                <p class="timeline-detail">{{ item.detail }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 测试趋势图 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>测试趋势</span>
      </template>
      <div ref="chartRef" style="height: 300px"></div>
    </el-card>

    <!-- 快捷操作 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/test/execute')">
          <el-icon><Play /></el-icon>
          执行测试
        </el-button>
        <el-button type="success" @click="$router.push('/inspection/tasks')">
          <el-icon><Timer /></el-icon>
          创建巡检
        </el-button>
        <el-button type="warning" @click="$router.push('/performance')">
          <el-icon><Lightning /></el-icon>
          性能测试
        </el-button>
        <el-button type="info" @click="$router.push('/test/history')">
          <el-icon><Document /></el-icon>
          查看报告
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'

const stats = reactive({
  totalTests: 128,
  passedTests: 115,
  failedTests: 13,
  passRate: 89.84
})

const qualityGate = reactive({
  passed: true,
  passRate: 89.84,
  totalTests: 128,
  passedTests: 115,
  failedTests: 13
})

const recentExecutions = ref([
  { id: 1, name: '功能测试 - 用户登录', time: '10 分钟前', status: 'success', detail: '15/15 通过' },
  { id: 2, name: '性能测试 - API 响应', time: '30 分钟前', status: 'success', detail: '平均响应 120ms' },
  { id: 3, name: '巡检 - 健康检查', time: '1 小时前', status: 'danger', detail: '2 个用例失败' },
  { id: 4, name: '功能测试 - 订单管理', time: '2 小时前', status: 'success', detail: '28/28 通过' }
])

const chartRef = ref(null)

onMounted(() => {
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['通过', '失败']
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '通过',
        type: 'bar',
        stack: 'total',
        data: [120, 132, 101, 134, 90, 230, 210],
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '失败',
        type: 'bar',
        stack: 'total',
        data: [22, 18, 19, 23, 29, 33, 31],
        itemStyle: { color: '#F56C6C' }
      }
    ]
  })

  window.addEventListener('resize', () => chart.resize())
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quality-gate-info {
  padding: 10px 0;
}

.quality-details {
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

.quality-details p {
  margin: 5px 0;
}

.timeline-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.quick-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
