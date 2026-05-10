<template>
  <div class="check-patient-info">
    <el-dialog
      v-model="visible"
      title="提示"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="content">
        <el-icon color="#E6A23C" size="48"><Warning /></el-icon>
        <p>您还未填写患者基本信息，无法进行评估。</p>
        <p>请先填写您的基本信息后再进行评估。</p>
      </div>
      <template #footer>
        <el-button @click="handleCancel">稍后再说</el-button>
        <el-button type="primary" @click="handleConfirm">去填写</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { usePatientStore } from '@/store'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const router = useRouter()
const patientStore = usePatientStore()

const visible = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function handleCancel() {
  visible.value = false
  router.push('/patient/home')
}

function handleConfirm() {
  visible.value = false
  router.push('/patient/patient-info')
}

// 检查并弹出提示
async function checkAndShow() {
  await patientStore.getMyPatientInfo()
  if (!patientStore.hasPatientInfo) {
    visible.value = true
    return false
  }
  return true
}

defineExpose({ checkAndShow, visible })
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.content p {
  margin: 15px 0 0 0;
  font-size: 16px;
  color: #303133;
}
</style>
