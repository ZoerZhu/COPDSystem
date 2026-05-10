<template>
  <el-dialog
    v-model="visible"
    title="急性加重警告"
    width="500px"
    :close-on-click-modal="false"
    class="exacerbation-dialog"
  >
    <div class="dialog-content">
      <div class="warning-icon">
        <el-icon :size="48" color="#E6A23C"><WarningFilled /></el-icon>
      </div>
      <p class="warning-text">
        检测到您近期的评估结果提示可能出现急性加重，请注意以下症状：
      </p>
      <ul class="warning-list">
        <li>呼吸困难：静息状态下也感到气短，无法平卧</li>
        <li>痰液变化：痰量明显增多，颜色变黄/绿，或出现血丝</li>
        <li>发热：体温超过38℃，尤其是伴有寒战</li>
        <li>精神状态改变：嗜睡、意识模糊、烦躁不安</li>
        <li>药物治疗无效：规律使用急救药物后症状无改善</li>
        <li>下肢水肿：新出现的踝部或小腿水肿</li>
        <li>口唇发绀：嘴唇或指甲床变紫</li>
      </ul>
      <p class="warning-hint">
        出现上述任何一项，请立即就医！
      </p>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleConfirm">我知道了</el-button>
        <el-button @click="handleCallDoctor">立即咨询医生</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const visible = ref(false)

function show() {
  visible.value = true
}

function handleConfirm() {
  visible.value = false
}

function handleCallDoctor() {
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

.warning-icon {
  margin-bottom: 1rem;
}

.warning-text {
  font-size: 1rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
  line-height: 1.6;
}

.warning-list {
  text-align: left;
  list-style: none;
  padding: 0 1rem;
  margin-bottom: 1rem;
}

.warning-list li {
  position: relative;
  padding-left: 1.5rem;
  margin-bottom: 0.75rem;
  color: var(--text-secondary);
  font-size: 0.875rem;
  line-height: 1.5;
}

.warning-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.5rem;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--danger);
}

.warning-hint {
  font-size: 1rem;
  font-weight: 600;
  color: var(--danger);
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 1rem;
}
</style>
