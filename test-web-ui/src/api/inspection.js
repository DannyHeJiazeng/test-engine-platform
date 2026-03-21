import request from '@/utils/request'

// 获取巡检任务列表
export function getInspectionTasks() {
  return request({
    url: '/inspection/tasks',
    method: 'get'
  })
}

// 创建巡检任务
export function createInspectionTask(data) {
  return request({
    url: '/inspection/tasks',
    method: 'post',
    data
  })
}

// 删除巡检任务
export function deleteInspectionTask(taskId) {
  return request({
    url: `/inspection/tasks/${taskId}`,
    method: 'delete'
  })
}

// 立即执行巡检
export function executeInspectionTask(taskId) {
  return request({
    url: `/inspection/tasks/${taskId}/execute`,
    method: 'post'
  })
}

// 获取告警历史
export function getAlertHistory() {
  return request({
    url: '/inspection/alerts',
    method: 'get'
  })
}

// 健康检查
export function healthCheck() {
  return request({
    url: '/inspection/health-checks',
    method: 'get'
  })
}
