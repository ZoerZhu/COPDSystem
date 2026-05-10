<template>
  <div class="assessment-page">
    <el-card class="assessment-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>CAT评估问卷</h2>
            <p class="header-subtitle">慢性阻塞性肺疾病患者自我评估测试</p>
          </div>
          <div class="header-right">
            <voice-player :text="introText" :show-label="true" />
          </div>
        </div>
      </template>

      <div class="introSection">
        <p>请根据您目前的情况，选择最符合的选项。每个问题只能选择一个答案。</p>
        <p class="score-tip">评分范围：0-40分，分数越高表示症状越严重</p>
      </div>

      <div class="question-list">
        <div
          v-for="(q, index) in questions"
          :key="index"
          class="question-item"
          role="group"
          :aria-labelledby="`question-${index}`"
        >
          <div class="question-title" :id="`question-${index}`">
            <span class="question-num" aria-hidden="true">{{ index + 1 }}</span>
            <span class="question-text">{{ q.text }}</span>
            <voice-player :text="q.text" :show-label="false" button-size="small" />
          </div>
          <div class="question-options" role="radiogroup" :aria-label="q.text">
            <div class="option-label left">{{ q.left }}</div>
            <div class="scale-wrapper">
              <div class="scale-header" aria-hidden="true">
                <span v-for="n in 6" :key="`tick-${index}-${n}`" class="scale-tick">
                  {{ n - 1 }}
                </span>
              </div>
              <el-radio-group
                v-model="answers[index]"
                @change="handleAnswerChange(index)"
                role="radiogroup"
                :aria-label="`问题${index + 1}: ${q.text}`"
              >
                <el-radio-button
                  v-for="n in 6"
                  :key="n-1"
                  :value="n-1"
                  :aria-label="`${n - 1}分`"
                >
                  {{ n - 1 }}
                </el-radio-button>
              </el-radio-group>
            </div>
            <div class="option-label right">{{ q.right }}</div>
          </div>
        </div>
      </div>

      <div class="result-preview">
        <span>当前总分：</span>
        <span class="total-score">{{ totalScore }}</span>
        <span class="score-level">（{{ scoreLevel }}）</span>
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

const submitting = ref(false)
const answers = ref([0, 0, 0, 0, 0, 0, 0, 0])

const introText = computed(() => {
  return 'CAT评估问卷。请根据您目前的情况，选择最符合的选项。评分范围0到40分，分数越高表示症状越严重。'
})

const questions = [
  { text: '我从不咳嗽', left: '我从不咳嗽', right: '我总是咳嗽' },
  { text: '我肺里一点也没有痰', left: '我一点痰也没有', right: '我有很多痰' },
  { text: '我一点也没有胸闷的感觉', left: '我一点也没有胸闷', right: '我有很严重的胸闷' },
  { text: '当我在爬坡或爬一层楼梯时没有喘不过气的感觉', left: '没有气短', right: '严重喘不上气' },
  { text: '我在家里的任何活动都不受到慢阻肺的影响', left: '不受影响', right: '严重影响' },
  { text: '尽管有肺病我仍有信心外出', left: '有信心外出', right: '没有信心外出' },
  { text: '我睡得好', left: '睡得好', right: '睡得不好' },
  { text: '我精力旺盛', left: '精力旺盛', right: '一点精力都没有' }
]

const totalScore = computed(() => answers.value.reduce((a, b) => a + b, 0))

const scoreLevel = computed(() => {
  const score = totalScore.value
  if (score < 10) return '轻微'
  if (score < 20) return '中等'
  if (score < 30) return '严重'
  return '非常严重'
})

const isComplete = computed(() => answers.value.every(a => a !== null && a !== undefined))

function handleAnswerChange(index) {
  // 强制触发响应式更新
  answers.value = [...answers.value]
}

function handleReset() {
  answers.value = [0, 0, 0, 0, 0, 0, 0, 0]
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

  if (!isComplete.value) {
    ElMessage.warning('请完成所有问题')
    return
  }

  submitting.value = true
  try {
    const data = {
      question1Cough: answers.value[0],
      question2Sputum: answers.value[1],
      question3Chest: answers.value[2],
      question4Breath: answers.value[3],
      question5Activity: answers.value[4],
      question6Confidence: answers.value[5],
      question7Sleep: answers.value[6],
      question8Energy: answers.value[7],
      totalScore: totalScore.value,
      assessmentDate: new Date().toISOString().slice(0, 10)
    }

    const res = await api.post('/assessment/cat/submit', data)
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
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: var(--bg-card);
  border-radius: 8px;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 1.1em;
  font-weight: 500;
}

.question-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  font-size: 0.9em;
}

.question-text {
  flex: 1;
  font-size: inherit;
}

.question-options {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.option-label {
  font-size: 0.9em;
  color: #606266;
  width: 100px;
  text-align: center;
}

.scale-wrapper {
  flex: 1;
  min-width: 260px;
}

.scale-header {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0.25rem;
  margin-bottom: 0.35rem;
  color: var(--text-secondary);
  font-size: 0.85em;
  user-select: none;
}

.scale-tick {
  text-align: center;
  line-height: 1;
}

.scale-wrapper :deep(.el-radio-group) {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0.25rem;
}

.scale-wrapper :deep(.el-radio-button) {
  width: 100%;
}

.scale-wrapper :deep(.el-radio-button__inner) {
  width: 100%;
  justify-content: center;
  border-radius: 8px !important;
}

.scale-wrapper :deep(.el-radio-button__inner) {
  padding: 12px 16px;
  font-size: 1em;
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

  .question-options {
    flex-direction: column;
  }

  .option-label {
    width: 100%;
    text-align: center;
    margin-bottom: 0.5rem;
  }

  .scale-wrapper {
    width: 100%;
    min-width: 0;
  }
}
</style>
