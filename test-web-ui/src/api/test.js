import request from '@/utils/request'

// 执行测试
export function runTest(data) {
  return request({
    url: '/test/run',
    method: 'post',
    data
  })
}

// 异步执行测试
export function runTestAsync(data) {
  return request({
    url: '/test/run-async',
    method: 'post',
    data
  })
}

// 执行功能测试
export function runFunctionalTests(packagePath) {
  return request({
    url: '/test/run/functional',
    method: 'post',
    params: { packagePath }
  })
}

// 执行性能测试
export function runPerformanceTests(packagePath) {
  return request({
    url: '/test/run/performance',
    method: 'post',
    params: { packagePath }
  })
}

// 执行巡检测试
export function runInspectionTests(packagePath) {
  return request({
    url: '/test/run/inspection',
    method: 'post',
    params: { packagePath }
  })
}

// 获取执行状态
export function getExecutionStatus(executionId) {
  return request({
    url: `/test/status/${executionId}`,
    method: 'get'
  })
}

// 获取执行历史
export function getExecutionHistory() {
  return request({
    url: '/test/history',
    method: 'get'
  })
}
