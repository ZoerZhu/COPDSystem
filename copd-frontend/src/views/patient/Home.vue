<template>
  <div class="home-page" :style="fontSizeStyle">
    <div class="welcome-section">
      <h1>欢迎回来</h1>
      <p>{{ userName }}</p>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon :size="32" color="#409EFF"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ assessmentCount }}</div>
            <div class="stat-label">评估次数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon :size="32" color="#67C23A"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ lastAssessmentDate || '-' }}</div>
            <div class="stat-label">最后评估</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon :size="32" color="#E6A23C"><Medal /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ goldGroup || '未评估' }}</div>
            <div class="stat-label">GOLD分组</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="action-card">
      <template #header>
        <div class="card-header">
          <h3>快速操作</h3>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="action-item" @click="goToAssessment">
            <el-icon :size="40" color="#409EFF"><CircleCheck /></el-icon>
            <span>CAT评估</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="action-item" @click="goToPatientInfo">
            <el-icon :size="40" color="#67C23A"><User /></el-icon>
            <span>个人信息</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="action-item" @click="goToHistory">
            <el-icon :size="40" color="#E6A23C"><Clock /></el-icon>
            <span>历史记录</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="info-card" v-if="!hasPatientInfo">
      <div class="info-content">
        <el-icon :size="24" color="#E6A23C"><Warning /></el-icon>
        <p>您还未填写患者信息，请先完善基本信息以便进行评估。</p>
        <el-button type="primary" @click="goToPatientInfo">去填写</el-button>
      </div>
    </el-card>

    <LoginReminder ref="loginReminderRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Calendar, Medal, CircleCheck, User, Clock, Warning } from '@element-plus/icons-vue'
import { useUserStore, usePatientStore, api } from '@/store'
import LoginReminder from '@/components/LoginReminder.vue'

const router = useRouter()
const userStore = useUserStore()
const patientStore = usePatientStore()

const fontSizeStyle = computed(() => {
  return {
    '--patient-font-size': patientStore.fontSize + 'px'
  }
})

const loginReminderRef = ref(null)
const assessmentCount = ref(0)
const lastAssessmentDate = ref('')
const goldGroup = ref('')

const userName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '患者')
const hasPatientInfo = computed(() => patientStore.hasPatientInfo)

onMounted(async () => {
  await patientStore.getMyPatientInfo()
  await loadAssessmentStats()

  const lastLogin = localStorage.getItem('lastLoginDate')
  const lastInfoUpdate = localStorage.getItem('lastInfoUpdateDate')
  const now = new Date()

  // 每次登录都提醒患者是否需要更新临床指标
  if (patientStore.hasPatientInfo) {
    // 检查是否超过30天未更新信息或未进行评估
    const hasRecentInfo = lastInfoUpdate && (now - new Date(lastInfoUpdate)) < 30 * 24 * 60 * 60 * 1000
    const hasRecentAssessment = lastLogin && (now - new Date(lastLogin)) < 30 * 24 * 60 * 60 * 1000

    if (!hasRecentInfo) {
      // 提醒更新临床指标
      loginReminderRef.value?.show({ clinicalReminder: true })
    } else if (!hasRecentAssessment) {
      // 提醒进行评估
      loginReminderRef.value?.show()
    }
  } else if (lastLogin) {
    const daysDiff = Math.floor((now - new Date(lastLogin)) / (1000 * 60 * 60 * 24))
    if (daysDiff > 30) {
      loginReminderRef.value?.show()
    }
  }

  localStorage.setItem('lastLoginDate', now.toISOString())
})

async function loadAssessmentStats() {
  try {
    const res = await api.get('/assessment/stats')
    if (res.code === 200) {
      assessmentCount.value = res.data.assessmentCount || 0
      lastAssessmentDate.value = res.data.lastAssessmentDate || ''
      goldGroup.value = res.data.goldGroup || ''
    }
  } catch (e) {
    console.error(e)
  }
}

function goToAssessment() {
  router.push('/patient/assessment/abe/cat')
}

function goToPatientInfo() {
  router.push('/patient/patient-info')
}

function goToHistory() {
  router.push('/patient/history')
}
</script>

<style scoped>
.home-page {
  font-size: var(--patient-font-size, 18px);
  padding: 1.5rem;
}

.welcome-section {
  margin-bottom: 2rem;
}

.welcome-section h1 {
  font-size: 1.75rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.welcome-section p {
  color: var(--text-secondary);
}

.stats-row {
  margin-bottom: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.action-card {
  margin-bottom: 1.5rem;
  border-radius: var(--radius-md);
}

.card-header h3 {
  font-size: 1rem;
  color: var(--text-primary);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 2rem 1rem;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-item:hover {
  background: var(--primary-light);
  transform: scale(1.02);
}

.action-item span {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 500;
}

.info-card {
  border-radius: var(--radius-md);
  border-left: 4px solid var(--warning);
}

.info-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.info-content p {
  flex: 1;
  margin: 0;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 1rem;
  }

  .action-item {
    padding: 1.5rem 1rem;
  }
}
</style>
