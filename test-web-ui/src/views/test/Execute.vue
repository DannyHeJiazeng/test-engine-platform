<template>
  <div class="test-execute">
    <el-card>
      <template #header>
        <span>执行测试</span>
      </template>

      <el-form :model="form" label-width="120px">
        <el-form-item label="执行方式">
          <el-radio-group v-model="form.executeType">
            <el-radio label="sync">同步执行</el-radio>
            <el-radio label="async">异步执行</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="测试类型">
          <el-select v-model="form.testType" placeholder="请选择测试类型">
            <el-option label="功能测试" value="functional" />
            <el-option label="性能测试" value="performance" />
            <el-option label="巡检测试" value="inspection" />
            <el-option label="全部测试" value="all" />
          </el-select>
        </el-form-item>

        <el-form-item label="包路径">
          <el-input
            v-model="form.packagePath"
            placeholder="例如：com.example.functional"
          />
        </el-form-item>

        <el-form-item label="测试类名">
          <el-input
            v-model="form.testClass"
            placeholder="例如：com.example.UserLoginTest"
          />
        </el-form-item>

        <el-form-item label="测试组">
          <el-select v-model="form.groups" multiple placeholder="可选">
            <el-option label="functional" value="functional" />
            <el-option label="performance" value="performance" />
            <el-option label="inspection" value="inspection" />
            <el-option label="smoke" value="smoke" />
            <el-option label="regression" value="regression" />
          </el-select>
        </el-form-item>

        <el-form-item label="质量门禁">
          <el-switch v-model="form.checkQualityGate" />
        </el-form-item>

        <el-form-item v-if="form.checkQualityGate" label="通过率要求">
          <el-slider v-model="form.threshold.minPassRate" :min="0" :max="1" :step="0.05" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRun">
            <el-icon><Play /></el-icon>
            开始执行
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 执行结果 -->
    <el-card v-if="result" style="margin-top: 20px">
      <template #header>
        <div class="result-header">
          <span>执行结果</span>
          <el-tag :type="result.passed ? 'success' : 'danger'">
            {{ result.passed ? '通过' : '失败' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="执行 ID">{{ result.executionId }}</el-descriptions-item>
        <el-descriptions-item label="总测试数">{{ result.totalTests }}</el-descriptions-item>
        <el-descriptions-item label="通过率">
          <el-progress :percentage="result.passRate" :status="result.passed ? 'success' : 'exception'" />
        </el-descriptions-item>
      </el-descriptions>

      <el-table :data="result.results" style="margin-top: 20px">
        <el-table-column prop="methodName" label="测试方法" />
        <el-table-column prop="testType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.testType)">
              {{ row.testType }}
            </el-tag>
          </template>
        </el-table-column>
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { runTest, runTestAsync } from '@/api/test'

const form = reactive({
  executeType: 'sync',
  testType: 'functional',
  packagePath: 'com.example.functional',
  testClass: '',
  groups: [],
  checkQualityGate: true,
  threshold: {
    minPassRate: 0.8
  }
})

const loading = ref(false)
const result = ref(null)

const handleRun = async () => {
  loading.value = true
  try {
    const data = {
      packagePath: form.packagePath,
      testClass: form.testClass || null,
      groups: form.groups.length > 0 ? form.groups : null,
      checkQualityGate: form.checkQualityGate,
      threshold: form.checkQualityGate ? form.threshold : null
    }

    let res
    if (form.executeType === 'async') {
      res = await runTestAsync(data)
      ElMessage.success('测试已异步执行，执行 ID: ' + res.data)
    } else {
      res = await runTest(data)
      result.value = res.data
      const passed = res.data.qualityGateResult?.passed !== false
      ElMessage.success(passed ? '测试执行通过' : '测试执行失败')
    }
  } catch (error) {
    ElMessage.error('执行失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  form.executeType = 'sync'
  form.testType = 'functional'
  form.packagePath = 'com.example.functional'
  form.testClass = ''
  form.groups = []
  form.checkQualityGate = true
  form.threshold.minPassRate = 0.8
  result.value = null
}

const getTypeTag = (type) => {
  const map = {
    functional: '',
    performance: 'warning',
    inspection: 'info'
  }
  return map[type] || ''
}
</script>

<style scoped>
.test-execute {
  padding: 0;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
