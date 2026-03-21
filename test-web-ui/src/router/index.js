import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'test/execute',
        name: 'TestExecute',
        component: () => import('@/views/test/Execute.vue'),
        meta: { title: '测试执行' }
      },
      {
        path: 'test/history',
        name: 'TestHistory',
        component: () => import('@/views/test/History.vue'),
        meta: { title: '执行历史' }
      },
      {
        path: 'quality-gate',
        name: 'QualityGate',
        component: () => import('@/views/quality/Gate.vue'),
        meta: { title: '质量门禁' }
      },
      {
        path: 'quality-history',
        name: 'QualityHistory',
        component: () => import('@/views/quality/History.vue'),
        meta: { title: '门禁历史' }
      },
      {
        path: 'inspection/tasks',
        name: 'InspectionTasks',
        component: () => import('@/views/inspection/Tasks.vue'),
        meta: { title: '巡检任务' }
      },
      {
        path: 'inspection/alerts',
        name: 'InspectionAlerts',
        component: () => import('@/views/inspection/Alerts.vue'),
        meta: { title: '告警历史' }
      },
      {
        path: 'performance',
        name: 'Performance',
        component: () => import('@/views/performance/Index.vue'),
        meta: { title: '性能测试' }
      },
      {
        path: 'report/:id',
        name: 'ReportDetail',
        component: () => import('@/views/report/Detail.vue'),
        meta: { title: '报告详情' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
