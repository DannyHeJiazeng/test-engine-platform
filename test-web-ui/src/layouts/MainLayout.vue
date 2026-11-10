<template>
  <div class="main-layout">
    <el-container v-if="!isCollapse">
      <el-aside :width="sidebarWidth">
        <div class="logo">
          <el-icon><Monitor /></el-icon>
          <span>Test Engine</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>工作台</span>
          </el-menu-item>

          <el-sub-menu index="test">
            <template #title>
              <el-icon><List /></el-icon>
              <span>测试管理</span>
            </template>
            <el-menu-item index="/test/execute">测试执行</el-menu-item>
            <el-menu-item index="/test/history">执行历史</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="quality">
            <template #title>
              <el-icon><CircleCheck /></el-icon>
              <span>质量门禁</span>
            </template>
            <el-menu-item index="/quality-gate">门禁配置</el-menu-item>
            <el-menu-item index="/quality-history">门禁历史</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="inspection">
            <template #title>
              <el-icon><Timer /></el-icon>
              <span>巡检监控</span>
            </template>
            <el-menu-item index="/inspection/tasks">巡检任务</el-menu-item>
            <el-menu-item index="/inspection/alerts">告警历史</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/performance">
            <el-icon><Lightning /></el-icon>
            <span>性能测试</span>
          </el-menu-item>

          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <el-icon class="collapse-btn" @click="toggleCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item v-if="route.meta.title">
                  {{ route.meta.title }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="user-info">
              <el-dropdown>
                <span class="user-avatar">
                  <el-avatar :size="32" icon="UserFilled" />
                  <span class="username">Admin</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>个人设置</el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>

    <el-container v-else>
      <el-aside width="64px">
        <div class="logo-mini">
          <el-icon><Monitor /></el-icon>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/dashboard">
            <el-tooltip placement="right" content="工作台">
              <el-icon><Odometer /></el-icon>
            </el-tooltip>
          </el-menu-item>
          <el-menu-item index="/test/execute">
            <el-tooltip placement="right" content="测试执行">
              <el-icon><List /></el-icon>
            </el-tooltip>
          </el-menu-item>
          <el-menu-item index="/quality-gate">
            <el-tooltip placement="right" content="质量门禁">
              <el-icon><CircleCheck /></el-icon>
            </el-tooltip>
          </el-menu-item>
          <el-menu-item index="/inspection/tasks">
            <el-tooltip placement="right" content="巡检任务">
              <el-icon><Timer /></el-icon>
            </el-tooltip>
          </el-menu-item>
          <el-menu-item index="/performance">
            <el-tooltip placement="right" content="性能测试">
              <el-icon><Lightning /></el-icon>
            </el-tooltip>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-tooltip placement="right" content="系统设置">
              <el-icon><Setting /></el-icon>
            </el-tooltip>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-content">
            <el-icon class="collapse-btn" @click="toggleCollapse">
              <Expand />
            </el-icon>
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item v-if="route.meta.title">
                  {{ route.meta.title }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="user-info">
              <el-dropdown>
                <span class="user-avatar">
                  <el-avatar :size="32" icon="UserFilled" />
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>个人设置</el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const sidebarWidth = computed(() => isCollapse.value ? '64px' : '220px')

const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  height: 100%;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-mini {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #409EFF;
}

.breadcrumb {
  flex: 1;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.username {
  color: #606266;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
