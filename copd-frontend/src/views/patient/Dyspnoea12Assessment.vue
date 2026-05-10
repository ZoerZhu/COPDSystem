<template>
  <div class="assessment-page" :style="fontSizeStyle">
    <el-card class="assessment-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>Dyspnoea-12评估问卷</h2>
            <p class="header-subtitle">呼吸困难评估量表（适用于家属或医生评估）</p>
          </div>
          <div class="header-right">
            <voice-player :text="introText" :show-label="true" />
          </div>
        </div>
      </template>

      <div class="introSection">
        <p>请根据患者目前的情况，选择最符合的选项。每个问题只能选择一个答案。</p>
        <p class="score-tip">评分说明：0分=无，1分=轻度，2分=中度，3分=重度</p>
      </div>

      <el-divider content-position="left">身体维度（7项）</el-divider>

      <div class="question-list">
        <div v-for="(q, index) in physicalQuestions" :key="`p${index}`" class="question-item">
          <div class="question-title">
            <span class="question-num">{{ index + 1 }}</span>
            <span class="question-text">{{ q }}</span>
            <voice-player :text="q" :show-label="false" button-size="small" />
          </div>
          <div class="scale-options">
            <el-radio-group v-model="physicalAnswers[index]">
              <el-radio :value="0">0</el-radio>
              <el-radio :value="1">1</el-radio>
              <el-radio :value="2">2</el-radio>
              <el-radio :value="3">3</el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>

      <el-divider content-position="left">情感维度（5项）</el-divider>

      <div class="question-list">
        <div v-for="(q, index) in emotionalQuestions" :key="`e${index}`" class="question-item">
          <div class="question-title">
            <span class="question-num">{{ index + 1 }}</span>
            <span class="question-text">{{ q }}</span>
            <voice-player :text="q" :show-label="false" button-size="small" />
          </div>
          <div class="scale-options">
            <el-radio-group v-model="emotionalAnswers[index]">
              <el-radio :value="0">0</el-radio>
              <el-radio :value="1">1</el-radio>
              <el-radio :value="2">2</el-radio>
              <el-radio :value="3">3</el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>

      <div class="result-preview">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="score-item">
              <div class="score-label">身体维度</div>
              <div class="score-value">{{ physicalScore }}</div>
              <div class="score-desc">{{ physicalLevel }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="score-item">
              <div class="score-label">情感维度</div>
              <div class="score-value">{{ emotionalScore }}</div>
              <div class="score-desc">{{ emotionalLevel }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="score-item total">
              <div class="score-label">总分</div>
              <div class="score-value">{{ totalScore }}</div>
              <div class="score-desc">{{ totalLevel }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="action-buttons">
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!isComplete" @click="handleSubmit">
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

const fontSizeStyle = computed(() => {
  return {
    '--patient-font-size': patientStore.fontSize + 'px'
  }
})

const submitting = ref(false)

const physicalAnswers = ref([0, 0, 0, 0, 0, 0, 0])
const emotionalAnswers = ref([0, 0, 0, 0, 0])

const introText = computed(() => {
  return 'Dyspnoea-12评估问卷。请根据患者目前的情况，选择最符合的选项。评分说明：0分等于无，1分等于轻度，2分等于中度，3分等于重度。'
})

const physicalQuestions = [
  '我的呼吸需要费力',
  '我感到气短',
  '我无法吸入足够的空气',
  '我感到呼吸急促',
  '我感到窒息',
  '我的呼吸费力/辛苦',
  '我呼吸不畅'
]

const emotionalQuestions = [
  '我的呼吸让我感到沮丧',
  '我的呼吸让我感到痛苦',
  '我的呼吸让我感到烦躁不安',
  '我的呼吸让我感到难受/可怜',
  '我的呼吸让我感到恼人'
]

const physicalScore = computed(() => physicalAnswers.value.reduce((a, b) => a + b, 0))
const emotionalScore = computed(() => emotionalAnswers.value.reduce((a, b) => a + b, 0))
const totalScore = computed(() => physicalScore.value + emotionalScore.value)

const physicalLevel = computed(() => {
  const score = physicalScore.value
  if (score <= 4) return '基本无不适'
  if (score <= 9) return '轻度费力'
  if (score <= 14) return '明显气短'
  return '严重窒息感'
})

const emotionalLevel = computed(() => {
  const score = emotionalScore.value
  if (score <= 3) return '情绪平稳'
  if (score <= 7) return '轻度烦躁'
  if (score <= 11) return '中度痛苦'
  return '严重心理困扰'
})

const totalLevel = computed(() => {
  const score = totalScore.value
  if (score <= 7) return '轻度'
  if (score <= 14) return '轻中度'
  if (score <= 21) return '中度'
  if (score <= 28) return '中重度'
  return '重度'
})

const isComplete = computed(() => {
  return physicalAnswers.value.every(a => a !== null) && emotionalAnswers.value.every(a => a !== null)
})

function handleReset() {
  physicalAnswers.value = [0, 0, 0, 0, 0, 0, 0]
  emotionalAnswers.value = [0, 0, 0, 0, 0]
}

async function handleSubmit() {
  // 检查是否填写了患者信息
  await patientStore.getMyPatientInfo()
  if (!patientStore.hasPatientInfo) {
    ElMessage.warning('请先填写患者基本信息')
    router.push('/patient/patient-info')
    return
  }

  if (!isComplete.value) {
    ElMessage.warning('请完成所有问题')
    return
  }

  submitting.value = true
  try {
    const data = {
      q1BreathlessSudden: physicalAnswers.value[0],
      q2BreathlessHeavy: physicalAnswers.value[1],
      q3BreathlessExhausted: physicalAnswers.value[2],
      q4BreathlessStarve: physicalAnswers.value[3],
      q5ChestTightness: physicalAnswers.value[4],
      q6BreathHeavy: physicalAnswers.value[5],
      q7BreathRapid: physicalAnswers.value[6],
      q8Frustrated: emotionalAnswers.value[0],
      q9Fear: emotionalAnswers.value[1],
      q10Distress: emotionalAnswers.value[2],
      q11Worry: emotionalAnswers.value[3],
      q12Depressed: emotionalAnswers.value[4]
    }

    const res = await api.post('/assessment/dyspnoea12/submit', data)
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
  max-width: 900px;
  margin: 0 auto;
}

.assessment-card {
  border-radius: var(--radius-md);
}

.card-header h2 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.header-subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.introSection {
  background: var(--primary-light);
  padding: 1rem;
  border-radius: var(--radius-sm);
  margin-bottom: 1.5rem;
}

.introSection p {
  margin: 0;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.score-tip {
  margin-top: 0.5rem !important;
  color: var(--text-secondary) !important;
  font-size: 0.8rem !important;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1rem;
}

.question-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.question-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.question-num {
  width: 24px;
  height: 24px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.question-text {
  font-weight: 500;
  color: var(--text-primary);
}

.scale-options {
  flex-shrink: 0;
}

.scale-options :deep(.el-radio-group) {
  display: flex;
  gap: 0.5rem;
}

.scale-options :deep(.el-radio) {
  margin-right: 0;
}

.scale-options :deep(.el-radio__label) {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.scale-options :deep(.el-radio__input.is-checked + .el-radio__label) {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.result-preview {
  margin-top: 1.5rem;
  padding: 1.5rem;
  background: var(--primary-light);
  border-radius: var(--radius-sm);
}

.score-item {
  text-align: center;
  padding: 1rem;
}

.score-item.total {
  background: var(--primary);
  border-radius: var(--radius-sm);
  color: white;
}

.score-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.score-item.total .score-label {
  color: rgba(255, 255, 255, 0.8);
}

.score-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--primary);
}

.score-item.total .score-value {
  color: white;
}

.score-desc {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.score-item.total .score-desc {
  color: rgba(255, 255, 255, 0.8);
}

.action-buttons {
  margin-top: 1.5rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
}

@media (max-width: 768px) {
  .question-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .scale-options :deep(.el-radio-group) {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
