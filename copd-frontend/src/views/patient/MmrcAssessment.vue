<template>
  <div class="assessment-page">
    <el-card class="assessment-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>mMRC评估问卷</h2>
            <p class="header-subtitle">改良版英国医学研究委员会呼吸问卷</p>
          </div>
          <div class="header-right">
            <voice-player :text="introText" :show-label="true" />
          </div>
        </div>
      </template>

      <div class="introSection">
        <p>请选择最能反映您目前呼吸困难情况的等级（0-4级）。</p>
        <p class="score-tip">评分越高表示呼吸困难越严重</p>
      </div>

      <div class="question-list">
        <div
          v-for="(item, index) in mmrcOptions"
          :key="index"
          class="question-item"
          :class="{ selected: answer === index }"
          @click="answer = index"
        >
          <div class="option-radio">
            <el-radio-group v-model="answer">
              <el-radio :value="index">
                <span class="level-num">{{ index }}</span>
              </el-radio>
            </el-radio-group>
          </div>
          <div class="option-content">
            <div class="level-title">
              {{ item.title }}
              <voice-player :text="item.title + '，' + item.description" :show-label="false" button-size="small" />
            </div>
            <div class="level-desc">{{ item.description }}</div>
          </div>
        </div>
      </div>

      <div class="result-preview">
        <span>当前等级：</span>
        <span class="total-score">{{ answer }}</span>
        <span class="score-level">（{{ answerLevel }}）</span>
      </div>

      <div class="action-buttons">
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" :loading="submitting" :disabled="answer === null" @click="handleSubmit">
          提交评估
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore, usePatientStore, api } from '@/store'
import VoicePlayer from '@/components/VoicePlayer.vue'

const router = useRouter()
const userStore = useUserStore()
const patientStore = usePatientStore()

const submitting = ref(false)
const answer = ref(null)

const introText = computed(() => {
  return 'mMRC评估问卷。请选择最能反映您目前呼吸困难情况的等级。评分越高表示呼吸困难越严重。'
})

const mmrcOptions = [
  { title: '0级', description: '只有在做剧烈活动时才感到呼吸困难' },
  { title: '1级', description: '在平地快步行走或步行爬小坡时出现气短' },
  { title: '2级', description: '由于气短，平地行走时比同龄人慢或者需要停下来休息' },
  { title: '3级', description: '在平地行走约100米或数分钟后需要停下来喘气' },
  { title: '4级', description: '因为严重呼吸困难而不能离开家，或在穿脱衣服时出现呼吸困难' }
]

const answerLevel = computed(() => {
  const level = answer.value
  if (level === null || level === undefined) return '-'
  const levels = ['', '极轻', '轻度', '中度', '重度', '极重度']
  return levels[level] || '-'
})

function handleReset() {
  answer.value = null
  ElMessage.info('已重置')
}

async function handleSubmit() {
  // 检查是否填写了患者信息
  await patientStore.getMyPatientInfo()
  if (!patientStore.hasPatientInfo) {
    ElMessage.warning('请先填写患者基本信息')
    router.push('/patient/patient-info')
    return
  }

  if (answer.value === null) {
    ElMessage.warning('请选择一个选项')
    return
  }

  submitting.value = true
  try {
    const data = {
      grade: answer.value,
      assessmentDate: new Date().toISOString().slice(0, 10)
    }

    const res = await api.post('/assessment/mmrc/submit', data)
    if (res.code === 200) {
      ElMessage.success('评估提交成功')
      router.push('/patient/result')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.assessment-page {
  font-size: var(--patient-font-size, 18px);
  padding: 1.5rem;
  max-width: 800px;
  margin: 0 auto;
}

.assessment-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left h2 {
  font-size: 1.5em;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.header-subtitle {
  font-size: 1em;
  color: var(--text-secondary);
}

.introSection {
  background: #f5f7fa;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.introSection p {
  margin: 0.5rem 0;
  font-size: 1em;
  line-height: 1.6;
}

.score-tip {
  color: #909399;
  font-size: 0.9em;
}

.question-list {
  margin-bottom: 1.5rem;
}

.question-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  margin-bottom: 1rem;
  background: var(--bg-card);
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-item:hover {
  border-color: var(--primary);
}

.question-item.selected {
  border-color: var(--primary);
  background: #ecf5ff;
}

.option-radio {
  display: flex;
  align-items: center;
  padding-left: 0.5rem;
}

.level-num {
  font-size: 1.25em;
  font-weight: bold;
}

.option-content {
  flex: 1;
}

.level-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1em;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.level-desc {
  font-size: 1em;
  color: #606266;
  line-height: 1.5;
}

.result-preview {
  text-align: center;
  padding: 1rem;
  background: #ecf5ff;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-size: 1.1em;
}

.total-score {
  font-size: 1.5em;
  font-weight: bold;
  color: var(--primary);
  margin: 0 0.5rem;
}

.score-level {
  color: #909399;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.action-buttons .el-button {
  padding: 12px 24px;
  font-size: 1em;
}

@media (max-width: 768px) {
  .assessment-page {
    padding: 1rem;
  }

  .question-item {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
