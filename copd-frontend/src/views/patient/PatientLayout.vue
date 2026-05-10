<template>
  <div class="patient-layout">
    <el-container>
      <el-aside width="240px">
        <div class="logo">
          <h3>慢阻肺评估</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="patient-menu"
        >
          <el-menu-item index="/patient/home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/patient/patient-info">
            <el-icon><User /></el-icon>
            <span>我的信息</span>
          </el-menu-item>
          <el-sub-menu index="assessment">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>病情评估</span>
            </template>
            <el-sub-menu index="assessment-abe">
              <template #title>
                <span>ABE分组</span>
              </template>
              <el-menu-item index="/patient/assessment/abe/cat">CAT评估</el-menu-item>
              <el-menu-item index="/patient/assessment/abe/mmrc">mMRC评估</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/patient/assessment/dyspnoea12">Dyspnoea-12</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/patient/result">
            <el-icon><DataAnalysis /></el-icon>
            <span>评估结果</span>
          </el-menu-item>
          <el-menu-item index="/patient/history">
            <el-icon><Clock /></el-icon>
            <span>历史记录</span>
          </el-menu-item>
          <el-menu-item index="/patient/profile">
            <el-icon><Setting /></el-icon>
            <span>个人中心</span>
          </el-menu-item>

        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-left">
            <span class="font-size-label">字体：</span>
            <el-button
              size="small"
              :type="patientStore.fontSize === 14 ? 'primary' : 'default'"
              @click="patientStore.setFontSize(14)"
            >
              小
            </el-button>
            <el-button
              size="small"
              :type="patientStore.fontSize === 18 ? 'primary' : 'default'"
              @click="patientStore.setFontSize(18)"
            >
              中
            </el-button>
            <el-button
              size="small"
              :type="patientStore.fontSize === 22 ? 'primary' : 'default'"
              @click="patientStore.setFontSize(22)"
            >
              大
            </el-button>
            <el-button
              size="small"
              :type="patientStore.fontSize === 26 ? 'primary' : 'default'"
              @click="patientStore.setFontSize(26)"
            >
              特大
            </el-button>
          </div>
          <div class="header-right">
            <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
            <el-button type="danger" link @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出
            </el-button>
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
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { House, User, Document, DataAnalysis, Clock, Setting, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore, usePatientStore } from '@/store'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const patientStore = usePatientStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  // 初始化字体大小
  patientStore.initFontSize()
})
</script>

<style scoped>
.patient-layout {
  min-height: 100vh;
  font-size: var(--patient-font-size, 18px);
}

.patient-layout :deep(.el-button) {
  font-size: calc(var(--patient-font-size, 18px) * 0.85);
}

.patient-layout :deep(.el-input__inner),
.patient-layout :deep(.el-form-item__label),
.patient-layout :deep(.el-table),
.patient-layout :deep(.el-card__body),
.patient-layout :deep(.el-menu-item),
.patient-layout :deep(.el-dialog__body) {
  font-size: inherit;
}

.patient-layout .el-container {
  min-height: 100vh;
}

.el-aside {
  background-color: var(--bg-card);
  border-right: 1px solid var(--border-color);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border-color);
}

.logo h3 {
  font-size: 1.25rem;
  color: var(--primary);
}

.patient-menu {
  border-right: none;
}

.patient-menu :deep(.el-menu-item),
.patient-menu :deep(.el-sub-menu__title) {
  height: 56px;
  line-height: 56px;
  font-size: 1rem;
}

.el-header {
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.font-size-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-right: 0.25rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  color: var(--text-secondary);
  font-size: 1rem;
}

.el-main {
  background-color: var(--bg-page);
  padding: 0;
}

@media (max-width: 768px) {
  .el-aside {
    display: none;
  }
}
</style>
