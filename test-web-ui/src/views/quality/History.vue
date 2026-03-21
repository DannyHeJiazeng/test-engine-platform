<template>
  <div class="quality-history">
    <el-card>
      <template #header>
        <span>门禁历史</span>
      </template>

      <div ref="chartRef" style="height: 300px; margin-bottom: 20px"></div>

      <el-table :data="history" style="width: 100%">
        <el-table-column prop="buildId" label="构建 ID" width="200" />
        <el-table-column prop="checkTime" label="检查时间" width="180" />
        <el-table-column prop="passed" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.passed ? 'success' : 'danger'">
              {{ row.passed ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="通过率" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.passRate * 100" :status="row.passed ? 'success' : 'exception'" />
          </template>
        </el-table-column>
        <el-table-column prop="totalTests" label="总测试数" width="100" />
        <el-table-column prop="passedTests" label="通过数" width="100" />
        <el-table-column prop="failedTests" label="失败数" width="100" />
        <el-table-column label="详情" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" title="门禁详情" width="600px">
      <el-descriptions :column="2" border v-if="selectedItem">
        <el-descriptions-item label="构建 ID">{{ selectedItem.buildId }}</el-descriptions-item>
        <el-descriptions-item label="检查时间">{{ selectedItem.checkTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="selectedItem.passed ? 'success' : 'danger'">
            {{ selectedItem.passed ? '通过' : '未通过' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="通过率">
          {{ (selectedItem.passRate * 100).toFixed(2) }}%
        </el-descriptions-item>
        <el-descriptions-item label="总测试数">{{ selectedItem.totalTests }}</el-descriptions-item>
        <el-descriptions-item label="通过数">{{ selectedItem.passedTests }}</el-descriptions-item>
        <el-descriptions-item label="失败数">{{ selectedItem.failedTests }}</el-descriptions-item>
        <el-descriptions-item label="跳过数">{{ selectedItem.skippedTests }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="selectedItem?.failedChecks?.length > 0" style="margin-top: 20px">
        <p class="failed-title">失败检查项：</p>
        <ul class="failed-list">
          <li v-for="(check, index) in selectedItem.failedChecks" :key="index">
            {{ check }}
          </li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
const dialogVisible = ref(false)
const selectedItem = ref(null)

const history = ref([
  {
    buildId: 'BUILD-001',
    checkTime: '2024-03-16 10:30:00',
    passed: true,
    passRate: 0.95,
    totalTests: 100,
    passedTests: 95,
    failedTests: 5,
    skippedTests: 0,
    failedChecks: []
  },
  {
    buildId: 'BUILD-002',
    checkTime: '2024-03-16 09:00:00',
    passed: false,
    passRate: 0.75,
    totalTests: 100,
    passedTests: 75,
    failedTests: 25,
    skippedTests: 0,
    failedChecks: ['Pass rate 75.00% < 80.00% (minimum)', 'P0 tests failed: 2']
  },
  {
    buildId: 'BUILD-003',
    checkTime: '2024-03-15 18:00:00',
    passed: true,
    passRate: 0.98,
    totalTests: 100,
    passedTests: 98,
    failedTests: 2,
    skippedTests: 0,
    failedChecks: []
  }
])

onMounted(() => {
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    title: {
      text: '通过率趋势'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}%'
    },
    xAxis: {
      type: 'category',
      data: history.value.map(h => h.buildId)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [{
      data: history.value.map(h => (h.passRate * 100).toFixed(2)),
      type: 'line',
      smooth: true,
      itemStyle: { color: '#409EFF' },
      markLine: {
        data: [{ yAxis: 80, name: '最低要求' }],
        lineStyle: { color: '#F56C6C', type: 'dashed' }
      }
    }]
  })

  window.addEventListener('resize', () => chart.resize())
})

const handleViewDetail = (item) => {
  selectedItem.value = item
  dialogVisible.value = true
}
</script>

<style scoped>
.quality-history {
  padding: 0;
}

.failed-title {
  color: #F56C6C;
  font-weight: bold;
  margin-bottom: 10px;
}

.failed-list {
  color: #606266;
  font-size: 14px;
  padding-left: 20px;
}

.failed-list li {
  margin: 5px 0;
}
</style>
