import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(async config => {
  if (!['get', 'head', 'options'].includes((config.method || 'get').toLowerCase())) {
    const { data } = await request.get('/auth/csrf')
    config.headers[data.headerName] = data.token
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.success === false || (res.code !== 0 && res.code !== undefined)) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/')) {
      window.location.assign('/login')
    }
    if (!error.config?.silent) ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
