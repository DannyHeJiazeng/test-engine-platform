<template>
  <div class="inspection-tasks">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>巡检任务</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建任务
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" style="width: 100%">
        <el-table-column prop="taskId" label="任务 ID" width="200" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="packagePath" label="包路径" />
        <el-table-column prop="intervalSeconds" label="执行间隔" width="120">
          <template #default="{ row }">
            {{ formatInterval(row.intervalSeconds) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '运行中' : '已停止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertOnFailure" label="失败告警" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertOnFailure ? 'warning' : 'info'">
              {{ row.alertOnFailure ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleExecute(row)">
              立即执行
            </el-button>
            <el-button link type="warning" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑任务' : '新建任务'"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="任务 ID" required>
          <el-input v-model="form.taskId" placeholder="例如：daily-health-check" />
        </el-form-item>
        <el-form-item label="任务名称" required>
          <el-input v-model="form.taskName" placeholder="例如：每日健康检查" />
        </el-form-item>
        <el-form-item label="包路径" required>
          <el-input v-model="form.packagePath" placeholder="例如：com.example.inspection" />
        </el-form-item>
        <el-form-item label="执行间隔" required>
          <el-select v-model="form.intervalSeconds" placeholder="请选择">
            <el-option label="1 分钟" :value="60" />
            <el-option label="5 分钟" :value="300" />
            <el-option label="10 分钟" :value="600" />
            <el-option label="30 分钟" :value="1800" />
            <el-option label="1 小时" :value="3600" />
            <el-option label="6 小时" :value="21600" />
            <el-option label="12 小时" :value="43200" />
            <el-option label="24 小时" :value="86400" />
          </el-select>
        </el-form-item>
        <el-form-item label="失败告警">
          <el-switch v-model="form.alertOnFailure" />
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="form.alertLevel" placeholder="请选择">
            <el-option label="普通" value="INFO" />
            <el-option label="重要" value="WARNING" />
            <el-option label="紧急" value="ERROR" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const tasks = ref([
  {
    taskId: 'daily-health-check',
    taskName: '每日健康检查',
    packagePath: 'com.example.inspection',
    intervalSeconds: 300,
    status: 'active',
    alertOnFailure: true,
    alertLevel: 'WARNING'
  },
  {
    taskId: 'api-smoke-test',
    taskName: 'API 冒烟测试',
    packagePath: 'com.example.smoke',
    intervalSeconds: 600,
    status: 'active',
    alertOnFailure: true,
    alertLevel: 'ERROR'
  }
])

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
  taskId: '',
  taskName: '',
  packagePath: '',
  intervalSeconds: 300,
  alertOnFailure: true,
  alertLevel: 'WARNING'
})

const formatInterval = (seconds) => {
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}小时`
  return `${Math.floor(seconds / 86400)}天`
}

const resetForm = () => {
  form.taskId = ''
  form.taskName = ''
  form.packagePath = ''
  form.intervalSeconds = 300
  form.alertOnFailure = true
  form.alertLevel = 'WARNING'
}

const handleCreate = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  isEdit.value = true
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (isEdit.value) {
    ElMessage.success('任务已更新')
  } else {
    tasks.value.push({ ...form, status: 'active' })
    ElMessage.success('任务已创建')
  }
  dialogVisible.value = false
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    tasks.value = tasks.value.filter(t => t.taskId !== row.taskId)
    ElMessage.success('删除成功')
  })
}

const handleExecute = (row) => {
  ElMessage.success(`任务 ${row.taskId} 已开始执行`)
}
</script>

<style scoped>
.inspection-tasks {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
