<template>
  <el-dialog
    v-model="visible"
    title="登录提醒"
    width="450px"
    :close-on-click-modal="false"
    class="login-reminder-dialog"
  >
    <div class="dialog-content">
      <div class="reminder-icon">
        <el-icon :size="48" color="#409EFF"><Bell /></el-icon>
      </div>

      <!-- 临床指标更新提醒 -->
      <div v-if="showClinicalReminder" class="reminder-box">
        <p class="reminder-title">您的临床指标可能需要更新</p>
        <p class="reminder-text">
          您最近一次登录后尚未更新临床指标。为确保评估结果准确，请确认以下信息是否需要更新：
        </p>
        <ul class="reminder-list">
          <li>急性加重次数（一年内）</li>
          <li>住院次数（一年内）</li>
          <li>吸烟状态</li>
        </ul>
      </div>

      <!-- 评估提醒 -->
      <div v-else class="reminder-box">
        <p class="reminder-text">
          您已经超过30天未进行病情评估，为了更好地管理您的健康，请定期进行评估。
        </p>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">稍后提醒</el-button>
        <el-button v-if="showClinicalReminder" type="primary" @click="handleGoUpdateInfo">去更新信息</el-button>
        <el-button v-else type="primary" @click="handleGoAssess">去评估</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const visible = ref(false)
const showClinicalReminder = ref(false)

function show(options = {}) {
  showClinicalReminder.value = options.clinicalReminder || false
  visible.value = true
}

function handleCancel() {
  visible.value = false
}

function handleGoAssess() {
  visible.value = false
  router.push('/patient/assessment/cat')
}

function handleGoUpdateInfo() {
  visible.value = false
  router.push('/patient/patient-info')
}

defineExpose({
  show
})
</script>

<style scoped>
.dialog-content {
  text-align: center;
  padding: 1rem 0;
}

.reminder-icon {
  margin-bottom: 1rem;
}

.reminder-box {
  text-align: left;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 0.5rem;
}

.reminder-title {
  font-size: 1rem;
  font-weight: 600;
  color: #303133;
  margin-bottom: 0.5rem;
}

.reminder-text {
  font-size: 0.9rem;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 0.5rem;
}

.reminder-list {
  margin: 0.5rem 0 0 0;
  padding-left: 1.2rem;
  color: #606266;
  font-size: 0.875rem;
}

.reminder-list li {
  margin-bottom: 0.25rem;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 1rem;
}
</style>
