<template>
  <div class="report-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>测试报告 - {{ reportId }}</span>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
        </div>
      </template>

      <!-- 报告概览 -->
      <div class="report-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-label">总测试数</div>
              <div class="overview-value">{{ report.totalTests }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-label">通过数</div>
              <div class="overview-value" style="color: #67C23A">{{ report.passedTests }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-label">失败数</div>
              <div class="overview-value" style="color: #F56C6C">{{ report.failedTests }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-label">通过率</div>
              <div class="overview-value">{{ report.passRate }}%</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 质量门禁结果 -->
      <el-divider>质量门禁</el-divider>
      <div class="quality-gate-result">
        <el-tag :type="report.qualityGate?.passed ? 'success' : 'danger'" size="large">
          {{ report.qualityGate?.passed ? '通过' : '未通过' }}
        </el-tag>
        <p v-if="report.qualityGate?.message" class="quality-message">
          {{ report.qualityGate.message }}
        </p>
      </div>

      <!-- 测试结果详情 -->
      <el-divider>测试结果详情</el-divider>
      <el-table :data="report.results" style="width: 100%">
        <el-table-column prop="testId" label="测试 ID" width="180" />
        <el-table-column prop="className" label="测试类" />
        <el-table-column prop="methodName" label="测试方法" width="200" />
        <el-table-column prop="testType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.testType)">{{ row.testType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '通过' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时 (ms)" width="100" />
        <el-table-column prop="failureMessage" label="失败信息" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 趋势图表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>历史趋势</span>
      </template>
      <div ref="chartRef" style="height: 300px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'

const route = useRoute()
const reportId = ref(route.params.id)

const report = ref({
  totalTests: 128,
  passedTests: 115,
  failedTests: 13,
  passRate: 89.84,
  qualityGate: {
    passed: true,
    message: 'Quality gate passed'
  },
  results: [
    {
      testId: 'TEST-001',
      className: 'com.example.UserLoginTest',
      methodName: 'testNormalLogin',
      testType: 'functional',
      priority: 'P0',
      status: 'SUCCESS',
      durationMs: 120,
      failureMessage: ''
    },
    {
      testId: 'TEST-002',
      className: 'com.example.UserLoginTest',
      methodName: 'testEmptyUsername',
      testType: 'functional',
      priority: 'P1',
      status: 'FAILURE',
      durationMs: 80,
      failureMessage: 'Expected assertion to fail'
    }
  ]
})

const chartRef = ref(null)

onMounted(() => {
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    title: { text: '通过率趋势' },
    tooltip: { trigger: 'axis', formatter: '{b}: {c}%' },
    xAxis: {
      type: 'category',
      data: ['BUILD-001', 'BUILD-002', 'BUILD-003', 'BUILD-004', 'BUILD-005']
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '{value}%' }
    },
    series: [{
      data: [85, 88, 92, 87, 89.84],
      type: 'line',
      smooth: true,
      itemStyle: { color: '#409EFF' },
      markLine: {
        data: [{ yAxis: 80, name: '最低要求' }],
        lineStyle: { color: '#F56C6C', type: 'dashed' }
      }
    }]
  })
})

const getTypeTag = (type) => {
  const map = { functional: '', performance: 'warning', inspection: 'info' }
  return map[type] || ''
}

const handleExport = () => {
  // 导出逻辑
}
</script>

<style scoped>
.report-detail {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-overview {
  padding: 20px 0;
}

.overview-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.overview-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.overview-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.quality-gate-result {
  padding: 15px 0;
}

.quality-message {
  margin-top: 10px;
  color: #606266;
}
</style>
