<template>
  <div class="patient-list-page">
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <h2>患者管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增患者
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          class="search-bar__keyword"
          placeholder="搜索患者姓名/手机号"
          clearable
          @clear="loadPatients"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="goldGroupFilter"
          class="search-bar__gold"
          placeholder="GOLD分组"
          clearable
          @change="loadPatients"
        >
          <el-option value="A" label="A组" />
          <el-option value="B" label="B组" />
          <el-option value="E" label="E组" />
        </el-select>
      </div>

      <el-table
        v-loading="loading"
        :data="patientList"
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="province" label="省份" min-width="120" />
        <el-table-column prop="smokingStatus" label="吸烟状态" min-width="120">
          <template #default="{ row }">
            <el-tag v-if="row.smokingStatus === 'NEVER'" type="info">不吸烟</el-tag>
            <el-tag v-else-if="row.smokingStatus === 'FORMER'" type="warning">已戒烟</el-tag>
            <el-tag v-else-if="row.smokingStatus === 'CURRENT'" type="danger">正在吸烟</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="goldGroup" label="GOLD分组" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.goldGroup === 'A'" type="success">A组</el-tag>
            <el-tag v-else-if="row.goldGroup === 'B'" type="warning">B组</el-tag>
            <el-tag v-else-if="row.goldGroup === 'E'" type="danger">E组</el-tag>
            <el-tag v-else type="info">未评估</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="yearlyAcuteExacerbations" label="中度急性加重次数" width="140" />
        <el-table-column prop="yearlyHospitalizations" label="导致住院的重度急性加重次数" width="200" />
        <el-table-column prop="lastAssessmentDate" label="最后评估" min-width="160">
          <template #default="{ row }">
            {{ row.lastAssessmentDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadPatients"
          @current-change="loadPatients"
        />
      </div>
    </el-card>

    <PatientDialog
      v-model="dialogVisible"
      :patient-data="currentPatient"
      @success="loadPatients"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useUserStore, api } from '@/store'
import PatientDialog from '@/components/PatientDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const patientList = ref([])
const searchKeyword = ref('')
const goldGroupFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const currentPatient = ref(null)

async function loadPatients() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      goldGroup: goldGroupFilter.value
    }
    const res = await api.get('/patient/list', { params })
    if (res.code === 200) {
      patientList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  currentPatient.value = null
  dialogVisible.value = true
}

function handleEdit(row) {
  currentPatient.value = { ...row }
  dialogVisible.value = true
}

function handleRowClick(row) {
  router.push(`/admin/patients/${row.id}`)
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除患者 ${row.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await api.delete(`/patient/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadPatients()
    } else {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadPatients()
})
</script>

<style scoped>
.patient-list-page {
  padding: 1.5rem;
}

.list-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  font-size: 1.25rem;
  color: var(--text-primary);
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  width: 100%;
}

.search-bar__keyword {
  flex: 1;
  min-width: 14rem;
}

.search-bar__gold {
  width: 10rem;
  flex: 0 0 auto;
}

@media (max-width: 768px) {
  .search-bar {
    flex-wrap: wrap;
  }

  .search-bar__keyword,
  .search-bar__gold {
    width: 100%;
    min-width: 0;
  }
}

.pagination {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-end;
}

.el-table {
  cursor: pointer;
}

.el-table :deep(.el-table__row) {
  transition: background-color var(--transition-fast);
}

.el-table :deep(.el-table__row:hover) {
  background-color: var(--primary-light);
}
</style>
