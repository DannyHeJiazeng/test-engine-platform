<template>
  <div class="inspection-alerts">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警历史</span>
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="alerts" style="width: 100%">
        <el-table-column prop="alertId" label="告警 ID" width="180" />
        <el-table-column prop="taskId" label="任务 ID" width="150" />
        <el-table-column prop="alertLevel" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.alertLevel)">
              {{ getLevelText(row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertType" label="类型" width="100">
          <template #default="{ row }">
            {{ row.alertType === 'TEST_FAILURE' ? '测试失败' : '执行异常' }}
          </template>
        </el-table-column>
        <el-table-column prop="message" label="告警内容" show-overflow-tooltip />
        <el-table-column prop="alertTime" label="告警时间" width="180" />
        <el-table-column prop="handled" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.handled ? 'success' : 'warning'">
              {{ row.handled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleMarkHandled(row)">
              {{ row.handled ? '撤销' : '标记' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 告警统计 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">总告警数</div>
            <div class="stat-value">{{ alerts.length }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">未处理</div>
            <div class="stat-value" style="color: #E6A23C">
              {{ alerts.filter(a => !a.handled).length }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">紧急告警</div>
            <div class="stat-value" style="color: #F56C6C">
              {{ alerts.filter(a => a.alertLevel === 'ERROR').length }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const alerts = ref([
  {
    alertId: 'ALERT-001',
    taskId: 'daily-health-check',
    alertLevel: 'ERROR',
    alertType: 'TEST_FAILURE',
    message: '健康检查失败：API 响应超时',
    alertTime: '2024-03-16 10:30:00',
    handled: false
  },
  {
    alertId: 'ALERT-002',
    taskId: 'api-smoke-test',
    alertLevel: 'WARNING',
    alertType: 'TEST_FAILURE',
    message: '冒烟测试失败：登录接口返回 500',
    alertTime: '2024-03-16 09:15:00',
    handled: true
  },
  {
    alertId: 'ALERT-003',
    taskId: 'daily-health-check',
    alertLevel: 'INFO',
    alertType: 'EXECUTION_ERROR',
    message: '巡检执行异常：数据库连接失败',
    alertTime: '2024-03-15 22:00:00',
    handled: true
  }
])

const getLevelType = (level) => {
  const map = { INFO: 'info', WARNING: 'warning', ERROR: 'danger' }
  return map[level] || 'info'
}

const getLevelText = (level) => {
  const map = { INFO: '普通', WARNING: '重要', ERROR: '紧急' }
  return map[level] || level
}

const handleRefresh = () => {
  ElMessage.success('刷新成功')
}

const handleMarkHandled = (row) => {
  row.handled = !row.handled
  ElMessage.success(row.handled ? '已标记为已处理' : '已撤销处理状态')
}
</script>

<style scoped>
.inspection-alerts {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}
</style>
