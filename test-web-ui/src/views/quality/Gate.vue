<template>
  <div class="quality-gate">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>质量门禁配置</span>
          </template>

          <el-form :model="config" label-width="140px">
            <el-form-item label="最小通过率">
              <el-slider
                v-model="config.minPassRate"
                :min="0"
                :max="1"
                :step="0.05"
                show-input
                :format-tooltip="formatPercent"
              />
            </el-form-item>

            <el-form-item label="最大失败数">
              <el-input-number
                v-model="config.maxFailures"
                :min="0"
                :max="100"
              />
            </el-form-item>

            <el-form-item label="不允许失败">
              <el-switch v-model="config.noFailureAllowed" />
            </el-form-item>

            <el-form-item label="P0 用例必须通过">
              <el-switch v-model="config.requireP0Pass" />
            </el-form-item>

            <el-form-item label="关键测试用例">
              <el-select
                v-model="config.criticalTests"
                multiple
                placeholder="请选择关键测试"
                style="width: 100%"
              >
                <el-option label="用户登录测试" value="com.example.UserLoginTest.testNormalLogin" />
                <el-option label="支付接口测试" value="com.example.PaymentTest.testPayment" />
                <el-option label="订单创建测试" value="com.example.OrderTest.testCreateOrder" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave">保存配置</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>当前状态</span>
          </template>

          <div class="status-info">
            <div class="status-item">
              <span class="status-label">门禁状态</span>
              <el-tag :type="status.passed ? 'success' : 'danger'" size="large">
                {{ status.passed ? '通过' : '未通过' }}
              </el-tag>
            </div>

            <div class="status-item">
              <span class="status-label">当前通过率</span>
              <el-progress
                :percentage="status.passRate"
                :status="status.passed ? 'success' : 'exception'"
              />
            </div>

            <el-divider />

            <div class="status-detail">
              <p>总测试数：{{ status.totalTests }}</p>
              <p>通过数：{{ status.passedTests }}</p>
              <p>失败数：{{ status.failedTests }}</p>
              <p>跳过数：{{ status.skippedTests }}</p>
            </div>

            <el-divider />

            <div v-if="status.failedChecks?.length > 0">
              <p class="failed-title">失败检查项：</p>
              <ul class="failed-list">
                <li v-for="(check, index) in status.failedChecks" :key="index">
                  {{ check }}
                </li>
              </ul>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <span>预设配置</span>
          </template>
          <div class="preset-configs">
            <el-button @click="applyPreset('default')">默认配置</el-button>
            <el-button @click="applyPreset('relaxed')">宽松配置</el-button>
            <el-button @click="applyPreset('strict')">严格配置</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const config = reactive({
  minPassRate: 0.8,
  maxFailures: 0,
  noFailureAllowed: true,
  requireP0Pass: true,
  criticalTests: []
})

const status = reactive({
  passed: true,
  passRate: 89.84,
  totalTests: 128,
  passedTests: 115,
  failedTests: 13,
  skippedTests: 0,
  failedChecks: []
})

const formatPercent = (value) => `${Math.round(value * 100)}%`

const handleSave = () => {
  ElMessage.success('配置已保存')
}

const handleReset = () => {
  config.minPassRate = 0.8
  config.maxFailures = 0
  config.noFailureAllowed = true
  config.requireP0Pass = true
  config.criticalTests = []
}

const applyPreset = (type) => {
  const presets = {
    default: { minPassRate: 0.8, maxFailures: 0, noFailureAllowed: true, requireP0Pass: true },
    relaxed: { minPassRate: 0.6, maxFailures: 10, noFailureAllowed: false, requireP0Pass: false },
    strict: { minPassRate: 1.0, maxFailures: 0, noFailureAllowed: true, requireP0Pass: true }
  }
  Object.assign(config, presets[type])
  ElMessage.success(`已应用${type === 'default' ? '默认' : type === 'relaxed' ? '宽松' : '严格'}配置`)
}
</script>

<style scoped>
.quality-gate {
  padding: 0;
}

.status-info {
  padding: 10px 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-label {
  color: #606266;
}

.status-detail {
  color: #606266;
  font-size: 14px;
}

.status-detail p {
  margin: 8px 0;
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

.preset-configs {
  display: flex;
  gap: 10px;
}
</style>
