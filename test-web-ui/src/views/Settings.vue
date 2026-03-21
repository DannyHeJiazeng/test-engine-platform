<template>
  <div class="settings">
    <el-card>
      <template #header>
        <span>系统设置</span>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicConfig" label-width="150px">
            <el-form-item label="工作目录">
              <el-input v-model="basicConfig.workspace" placeholder="./test-workspace" />
            </el-form-item>
            <el-form-item label="输出目录">
              <el-input v-model="basicConfig.outputDir" placeholder="./test-results" />
            </el-form-item>
            <el-form-item label="默认分支">
              <el-input v-model="basicConfig.defaultBranch" placeholder="main" />
            </el-form-item>
            <el-form-item label="克隆超时 (秒)">
              <el-input-number v-model="basicConfig.cloneTimeout" :min="60" :max="600" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('basic')">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 告警配置 -->
        <el-tab-pane label="告警配置" name="alert">
          <el-form :model="alertConfig" label-width="150px">
            <el-divider content-position="left">钉钉告警</el-divider>
            <el-form-item label="启用">
              <el-switch v-model="alertConfig.dingtalk.enabled" />
            </el-form-item>
            <el-form-item label="Webhook URL">
              <el-input v-model="alertConfig.dingtalk.webhookUrl" placeholder="https://oapi.dingtalk.com/robot/send?access_token=xxx" />
            </el-form-item>
            <el-form-item label="Secret">
              <el-input v-model="alertConfig.dingtalk.secret" type="password" show-password />
            </el-form-item>

            <el-divider content-position="left">企业微信告警</el-divider>
            <el-form-item label="启用">
              <el-switch v-model="alertConfig.wecom.enabled" />
            </el-form-item>
            <el-form-item label="Webhook URL">
              <el-input v-model="alertConfig.wecom.webhookUrl" placeholder="https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx" />
            </el-form-item>

            <el-divider content-position="left">邮件告警</el-divider>
            <el-form-item label="启用">
              <el-switch v-model="alertConfig.email.enabled" />
            </el-form-item>
            <el-form-item label="SMTP 主机">
              <el-input v-model="alertConfig.email.smtpHost" placeholder="smtp.example.com" />
            </el-form-item>
            <el-form-item label="SMTP 端口">
              <el-input-number v-model="alertConfig.email.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="alertConfig.email.username" placeholder="notify@example.com" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="alertConfig.email.password" type="password" show-password />
            </el-form-item>
            <el-form-item label="接收人">
              <el-select v-model="alertConfig.email.receivers" multiple placeholder="请选择">
                <el-option label="admin@example.com" value="admin@example.com" />
                <el-option label="dev@example.com" value="dev@example.com" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave('alert')">保存配置</el-button>
              <el-button @click="handleTestAlert">发送测试告警</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Maven 配置 -->
        <el-tab-pane label="Maven 配置" name="maven">
          <el-form :model="mavenConfig" label-width="150px">
            <el-form-item label="Maven _home">
              <el-input v-model="mavenConfig.home" placeholder="/usr/local/maven" />
            </el-form-item>
            <el-form-item label="构建超时 (秒)">
              <el-input-number v-model="mavenConfig.buildTimeout" :min="60" :max="1800" />
            </el-form-item>
            <el-form-item label="Maven 参数">
              <el-input v-model="mavenConfig.args" placeholder="-DskipTests -B" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('maven')">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 关于 -->
        <el-tab-pane label="关于" name="about">
          <div class="about-content">
            <h2>Test Engine Platform</h2>
            <p>版本：1.0.0-SNAPSHOT</p>
            <p>一站式测试中台平台 - 质量门禁自动化、测试用例执行、性能测试、发布后监控</p>

            <el-divider />

            <h3>技术栈</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="后端框架">Spring Boot 3.2.4</el-descriptions-item>
              <el-descriptions-item label="测试框架">TestNG 7.9.0</el-descriptions-item>
              <el-descriptions-item label="性能测试">jmeter-java-dsl 1.26.1</el-descriptions-item>
              <el-descriptions-item label="Git 操作">JGit 6.8.0</el-descriptions-item>
              <el-descriptions-item label="数据库">MySQL / H2</el-descriptions-item>
              <el-descriptions-item label="前端">Vue 3 + Element Plus</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

const basicConfig = reactive({
  workspace: './test-workspace',
  outputDir: './test-results',
  defaultBranch: 'main',
  cloneTimeout: 300
})

const alertConfig = reactive({
  dingtalk: {
    enabled: false,
    webhookUrl: '',
    secret: ''
  },
  wecom: {
    enabled: false,
    webhookUrl: ''
  },
  email: {
    enabled: false,
    smtpHost: '',
    smtpPort: 587,
    username: '',
    password: '',
    receivers: []
  }
})

const mavenConfig = reactive({
  home: '',
  buildTimeout: 600,
  args: '-DskipTests -B'
})

const handleSave = (type) => {
  ElMessage.success(`${type === 'basic' ? '基础' : type === 'alert' ? '告警' : 'Maven'}配置已保存`)
}

const handleTestAlert = () => {
  ElMessage.success('测试告警已发送')
}
</script>

<style scoped>
.settings {
  padding: 0;
}

.about-content {
  padding: 20px 0;
}

.about-content h2 {
  margin-bottom: 20px;
  color: #303133;
}

.about-content p {
  color: #606266;
  margin: 10px 0;
}

.about-content h3 {
  margin: 20px 0 15px;
  color: #303133;
}
</style>
