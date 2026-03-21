import request from '@/utils/request'

// 获取质量门禁状态
export function getQualityGateStatus() {
  return request({
    url: '/quality-gate/status',
    method: 'get'
  })
}

// 获取质量门禁历史
export function getQualityGateHistory() {
  return request({
    url: '/quality-gate/history',
    method: 'get'
  })
}

// 获取默认阈值
export function getDefaultThreshold() {
  return request({
    url: '/quality-gate/threshold/default',
    method: 'get'
  })
}

// 更新质量门禁配置
export function updateThreshold(data) {
  return request({
    url: '/quality-gate/threshold',
    method: 'post',
    data
  })
}
