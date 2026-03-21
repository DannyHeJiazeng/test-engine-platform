<template>
  <div class="performance">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>执行性能测试</span>
          </template>

          <el-form :model="form" label-width="120px">
            <el-form-item label="测试类型">
              <el-select v-model="form.testType" placeholder="请选择">
                <el-option label="并发测试" value="concurrency" />
                <el-option label="梯度并发" value="gradual" />
                <el-option label="负载测试" value="load" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>

            <el-form-item label="目标 URL" required>
              <el-input v-model="form.url" placeholder="http://localhost:8080/api/users" />
            </el-form-item>

            <el-form-item v-if="form.testType !== 'gradual'" label="线程数">
              <el-input-number v-model="form.threads" :min="1" :max="1000" />
            </el-form-item>

            <el-form-item v-if="form.testType === 'gradual'" label="起始线程数">
              <el-input-number v-model="form.startThreads" :min="1" :max="1000" />
            </el-form-item>

            <el-form-item v-if="form.testType === 'gradual'" label="最终线程数">
              <el-input-number v-model="form.endThreads" :min="1" :max="1000" />
            </el-form-item>

            <el-form-item v-if="form.testType === 'gradual'" label="步进">
              <el-input-number v-model="form.step" :min="1" :max="100" />
            </el-form-item>

            <el-form-item label="持续时间 (秒)">
              <el-input-number v-model="form.duration" :min="10" :max="3600" />
            </el-form-item>

            <el-form-item label="爬坡时间 (秒)">
              <el-input-number v-model="form.rampUp" :min="1" :max="300" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleRun">
                <el-icon><Lightning /></el-icon>
                开始测试
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>测试结果</span>
          </template>

          <div v-if="result" class="result-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总请求数">{{ result.totalRequests }}</el-descriptions-item>
              <el-descriptions-item label="成功率">{{ result.successRate }}%</el-descriptions-item>
              <el-descriptions-item label="平均响应">{{ result.avgResponseTime }}ms</el-descriptions-item>
              <el-descriptions-item label="中位数响应">{{ result.medianResponseTime }}ms</el-descriptions-item>
              <el-descriptions-item label="90% 响应">{{ result.p90ResponseTime }}ms</el-descriptions-item>
              <el-descriptions-item label="95% 响应">{{ result.p95ResponseTime }}ms</el-descriptions-item>
              <el-descriptions-item label="TPS">{{ result.tps }}</el-descriptions-item>
              <el-descriptions-item label="错误数">{{ result.errorCount }}</el-descriptions-item>
            </el-descriptions>

            <div ref="chartRef" style="height: 250px; margin-top: 20px"></div>
          </div>

          <el-empty v-else description="暂无测试结果" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import * as echarts from 'echarts'

const form = reactive({
  testType: 'concurrency',
  url: '',
  threads: 100,
  startThreads: 10,
  endThreads: 500,
  step: 50,
  duration: 60,
  rampUp: 10
})

const loading = ref(false)
const result = ref(null)
const chartRef = ref(null)

const handleRun = async () => {
  loading.value = true
  // 模拟执行
  setTimeout(() => {
    result.value = {
      totalRequests: 10000,
      successRate: 99.5,
      avgResponseTime: 120,
      medianResponseTime: 100,
      p90ResponseTime: 180,
      p95ResponseTime: 220,
      tps: 166,
      errorCount: 50
    }

    // 渲染图表
    setTimeout(() => {
      const chart = echarts.init(chartRef.value)
      chart.setOption({
        title: { text: '响应时间趋势' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: ['0s', '10s', '20s', '30s', '40s', '50s', '60s']
        },
        yAxis: {
          type: 'value',
          name: 'ms'
        },
        series: [{
          data: [80, 95, 110, 125, 140, 135, 120],
          type: 'line',
          smooth: true,
          itemStyle: { color: '#409EFF' }
        }]
      })
    }, 100)

    loading.value = false
  }, 2000)
}
</script>

<style scoped>
.performance {
  padding: 0;
}

.result-content {
  padding: 10px 0;
}
</style>
