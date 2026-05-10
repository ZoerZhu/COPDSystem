<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="220px">
        <div class="logo">
          <h3>慢阻肺管理</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="admin-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/patients">
            <el-icon><User /></el-icon>
            <span>患者管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataAnalysis, User, TrendCharts, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

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
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-layout .el-container {
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
  margin: 0;
  color: var(--primary);
  font-size: 1.125rem;
}

.admin-menu {
  border-right: none;
}

.el-header {
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 1.5rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  color: var(--text-secondary);
}

.el-main {
  background-color: var(--bg-page);
  padding: 1.5rem;
}
</style>
