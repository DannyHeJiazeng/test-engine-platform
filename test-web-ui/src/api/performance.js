import request from '@/utils/request'

// 执行性能测试
export function runPerformanceTest(data) {
  return request({
    url: '/performance/run',
    method: 'post',
    data
  })
}

// 并发测试
export function runConcurrencyTest(url, concurrency) {
  return request({
    url: '/performance/run/concurrency',
    method: 'post',
    params: { url, concurrency }
  })
}

// 梯度并发测试
export function runGradualTest(url, start, end, step) {
  return request({
    url: '/performance/run/gradual',
    method: 'post',
    params: { url, start, end, step }
  })
}

// 负载测试
export function runLoadTest(url, threads, duration) {
  return request({
    url: '/performance/run/load',
    method: 'post',
    params: { url, threads, duration }
  })
}

// 获取性能测试结果
export function getPerformanceResult(id) {
  return request({
    url: `/performance/result/${id}`,
    method: 'get'
  })
}

// 分析性能结果
export function analyzePerformanceResult(id) {
  return request({
    url: `/performance/analyze/${id}`,
    method: 'post'
  })
}

// 生成 JMX 文件
export function generateJmx(data) {
  return request({
    url: '/performance/jmx/generate',
    method: 'post',
    data
  })
}
