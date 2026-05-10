<template>
  <div class="profile-page" :style="fontSizeStyle">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="profile-form"
      >
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存修改
          </el-button>
          <el-button @click="handleLogout">退出登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore, usePatientStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const patientStore = usePatientStore()

const fontSizeStyle = computed(() => {
  return {
    '--patient-font-size': patientStore.fontSize + 'px'
  }
})

const formRef = ref()
const saving = ref(false)

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

onMounted(async () => {
  const userInfo = await userStore.getUserInfo()
  if (userInfo) {
    form.username = userInfo.username || ''
    form.realName = userInfo.realName || ''
    form.phone = userInfo.phone || ''
    form.email = userInfo.email || ''
  }
})

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const success = await userStore.updateUserInfo({
      realName: form.realName,
      phone: form.phone,
      email: form.email
    })
    if (success) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

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
.profile-page {
  font-size: var(--patient-font-size, 18px);
  padding: 1.5rem;
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  border-radius: var(--radius-md);
}

.card-header h2 {
  font-size: 1.25rem;
  color: var(--text-primary);
}

.profile-form {
  padding: 1rem 0;
}
</style>
