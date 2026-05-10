<template>
  <div class="patient-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h3>患者详情</h3>
        </div>
      </template>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="patient" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ patient.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ patient.gender === 'male' ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ patient.age }}岁</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ patient.phone }}</el-descriptions-item>
          <el-descriptions-item label="省份">{{ patient.province }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ patient.city }}</el-descriptions-item>
          <el-descriptions-item label="吸烟状态">
            <el-tag :type="getSmokingTagType(patient.smokingStatus)">
              {{ getSmokingLabel(patient.smokingStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="每月吸烟量">{{ patient.cigarettesPerMonth || 0 }}支</el-descriptions-item>
          <el-descriptions-item label="GOLD分组">
            <el-tag :type="getGoldTagType(patient.goldGroup)">{{ patient.goldGroup }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="FEV1预计值%">{{ patient.fev1Pred }}%</el-descriptions-item>
          <el-descriptions-item label="FEV1/FVC">{{ patient.fev1Fvc }}</el-descriptions-item>
          <el-descriptions-item label="中度急性加重次数">{{ patient.yearlyAcuteExacerbations || 0 }}次</el-descriptions-item>
          <el-descriptions-item label="导致住院的重度急性加重次数">{{ patient.yearlyHospitalizations || 0 }}次</el-descriptions-item>
          <el-descriptions-item label="总急性加重次数">{{ patient.totalAcuteExacerbations || 0 }}次</el-descriptions-item>
          <el-descriptions-item label="总住院次数">{{ patient.totalHospitalizations || 0 }}次</el-descriptions-item>
          <el-descriptions-item label="最后评估日期">{{ patient.lastAssessmentDate }}</el-descriptions-item>
        </el-descriptions>

        <div class="action-buttons">
          <el-button type="primary" @click="handleEdit">编辑</el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <el-empty v-else description="未找到患者信息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore, api } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const patient = ref(null)
const loading = ref(true)

function getSmokingTagType(status) {
  const map = {
    'never': 'success',
    'former': 'warning',
    'current': 'danger'
  }
  return map[status] || 'info'
}

function getSmokingLabel(status) {
  const map = {
    'never': '从不吸烟',
    'former': '已戒烟',
    'current': '当前吸烟'
  }
  return map[status] || '未知'
}

function getGoldTagType(group) {
  const map = {
    'A': 'success',
    'B': 'warning',
    'E': 'danger'
  }
  return map[group] || 'info'
}

function goBack() {
  router.back()
}

function handleEdit() {
  router.push(`/admin/patients/edit/${patient.value.id}`)
}

function handleDelete() {
  ElMessageBox.confirm('确定要删除该患者信息吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/patient/${patient.value.id}`)
      ElMessage.success('删除成功')
      router.push('/admin/patients')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(async () => {
  try {
    const res = await api.get(`/patient/${route.params.id}`)
    if (res.code === 200) {
      patient.value = res.data
    } else {
      ElMessage.error(res.message || '获取失败')
    }
  } catch (error) {
    ElMessage.error('获取患者信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.patient-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.card-header h3 {
  margin: 0;
}

.loading-state {
  padding: 2rem;
}

.detail-content {
  padding: 1rem 0;
}

.action-buttons {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
  justify-content: center;
}
</style>
