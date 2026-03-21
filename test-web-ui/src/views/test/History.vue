<template>
  <div class="test-history">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>执行历史</span>
          <el-button type="primary" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="history" style="width: 100%">
        <el-table-column prop="executionId" label="执行 ID" width="200" />
        <el-table-column prop="totalTests" label="测试数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'RUNNING' ? 'warning' : 'danger'">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通过率" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="calculatePassRate(row)"
              :status="row.results?.some(r => r.status === 'FAILURE') ? 'exception' : 'success'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="qualityGateResult" label="质量门禁" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.qualityGateResult" :type="row.qualityGateResult.passed ? 'success' : 'danger'">
              {{ row.qualityGateResult.passed ? '通过' : '未通过' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button link type="primary" @click="handleViewReport(row)">
              查看报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" title="执行详情" width="800px">
      <el-descriptions :column="2" border v-if="selectedItem">
        <el-descriptions-item label="执行 ID">{{ selectedItem.executionId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selectedItem.status }}</el-descriptions-item>
        <el-descriptions-item label="总测试数">{{ selectedItem.totalTests }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="selectedItem?.results" style="margin-top: 20px" max-height="400">
        <el-table-column prop="methodName" label="测试方法" />
        <el-table-column prop="className" label="测试类" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '通过' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时 (ms)" width="100" />
        <el-table-column prop="failureMessage" label="失败信息" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExecutionHistory } from '@/api/test'

const router = useRouter()
const history = ref([])
const dialogVisible = ref(false)
const selectedItem = ref(null)

const loadData = async () => {
  try {
    const res = await getExecutionHistory()
    history.value = res.data || []
  } catch (error) {
    ElMessage.error('加载失败：' + error.message)
  }
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('刷新成功')
}

const handleViewDetail = (item) => {
  selectedItem.value = item
  dialogVisible.value = true
}

const handleViewReport = (item) => {
  router.push(`/report/${item.executionId}`)
}

const getStatusText = (status) => {
  const map = {
    COMPLETED: '已完成',
    RUNNING: '运行中',
    FAILED: '失败'
  }
  return map[status] || status
}

const calculatePassRate = (row) => {
  if (!row.results || row.results.length === 0) return 0
  const passed = row.results.filter(r => r.status === 'SUCCESS').length
  return Math.round((passed / row.results.length) * 100)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.test-history {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
