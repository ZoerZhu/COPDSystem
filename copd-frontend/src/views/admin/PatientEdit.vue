<template>
  <div class="patient-edit-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
          <h2>编辑患者信息</h2>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="1" :max="150" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option value="男" label="男" />
                <el-option value="女" label="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24" :xs="24">
            <el-form-item label="居住地" prop="areaCode">
              <div class="area-select">
                <el-select
                  v-model="provinceCode"
                  placeholder="省/直辖市"
                  filterable
                  :validate-event="false"
                  style="width: 100%"
                  @change="handleProvinceChange"
                >
                  <el-option
                    v-for="p in areaOptions"
                    :key="p.value"
                    :label="p.label"
                    :value="p.value"
                  />
                </el-select>

                <el-select
                  v-model="cityCode"
                  placeholder="城市"
                  filterable
                  :validate-event="false"
                  style="width: 100%"
                  :disabled="!provinceCode"
                  @change="handleCityChange"
                >
                  <el-option
                    v-for="c in cityOptions"
                    :key="c.value"
                    :label="c.label"
                    :value="c.value"
                  />
                </el-select>

                <el-select
                  v-model="districtCode"
                  placeholder="区/县（可选）"
                  filterable
                  :validate-event="false"
                  style="width: 100%"
                  :disabled="!cityCode"
                  @change="syncAreaCode"
                >
                  <el-option
                    v-for="d in districtOptions"
                    :key="d.value"
                    :label="d.label"
                    :value="d.value"
                  />
                </el-select>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="吸烟状态" prop="smokingStatus">
              <el-select v-model="form.smokingStatus" placeholder="请选择吸烟状态" style="width: 100%">
                <el-option value="never" label="从未吸烟" />
                <el-option value="former" label="已戒烟" />
                <el-option value="current" label="当前吸烟" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每月吸烟量">
              <el-input-number v-model="form.cigarettesPerMonth" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="中度急性加重次数">
              <el-input-number v-model="form.yearlyAcuteExacerbations" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="导致住院的重度急性加重次数">
              <el-input-number v-model="form.yearlyHospitalizations" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="FEV1占预计值%">
              <el-input-number v-model="form.fev1Pred" :min="0" :max="100" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="FEV1/FVC">
              <el-input-number v-model="form.fev1Fvc" :min="0" :max="100" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '@/store'
import { regionData, codeToText } from 'element-china-area-data'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const submitting = ref(false)

const areaOptions = regionData

function normalizeProvinceLabel(text = '') {
  return text.replace(/省|市|自治区|壮族自治区|回族自治区|维吾尔自治区/g, '')
}

function normalizeCityLabel(text = '') {
  return text.replace(/市|区|县/g, '')
}

function findProvinceOption(provinceValue) {
  return areaOptions.find(p => p.value === provinceValue) || null
}

function findCityOption(provinceValue, cityValue) {
  const p = findProvinceOption(provinceValue)
  const cities = p?.children || []
  return cities.find(c => c.value === cityValue) || null
}

const provinceCode = ref('')
const cityCode = ref('')
const districtCode = ref('')

const cityOptions = computed(() => {
  const p = findProvinceOption(provinceCode.value)
  return p?.children || []
})

const districtOptions = computed(() => {
  const c = findCityOption(provinceCode.value, cityCode.value)
  return c?.children || []
})

function setSplitCodesFromText(provinceText, cityText, districtText) {
  provinceCode.value = ''
  cityCode.value = ''
  districtCode.value = ''
  if (!provinceText) return

  const normalizedProvince = normalizeProvinceLabel(provinceText)
  const matchedProvince = regionData.find(p => normalizeProvinceLabel(p.label) === normalizedProvince) || null
  if (!matchedProvince) return
  provinceCode.value = matchedProvince.value

  if (!cityText) return
  const normalizedCity = normalizeCityLabel(cityText)
  const matchedCity = (matchedProvince.children || []).find(c => normalizeCityLabel(c.label) === normalizedCity) || null
  if (matchedCity) {
    cityCode.value = matchedCity.value
  } else if (matchedProvince.children && matchedProvince.children.length > 0 && (cityText === '市辖区' || cityText === '辖')) {
    cityCode.value = matchedProvince.children[0].value
  }

  if (!districtText) return
  const matchedCityFinal = (matchedProvince.children || []).find(c => c.value === cityCode.value) || null
  const matchedDistrict = (matchedCityFinal?.children || []).find(d => d.label === districtText) || null
  if (matchedDistrict) {
    districtCode.value = matchedDistrict.value
  }
}

function handleAreaChange(areaValues) {
  if (areaValues && areaValues.length >= 2) {
    form.province = codeToText[areaValues[0]]
    form.city = codeToText[areaValues[1]]
    form.district = areaValues.length >= 3 ? codeToText[areaValues[2]] : ''
  } else {
    form.province = ''
    form.city = ''
    form.district = ''
  }
}

function syncAreaCode() {
  const next = []
  if (provinceCode.value) next.push(provinceCode.value)
  if (cityCode.value) next.push(cityCode.value)
  if (districtCode.value) next.push(districtCode.value)
  form.areaCode = next
  handleAreaChange(next)
}

function handleProvinceChange() {
  cityCode.value = ''
  districtCode.value = ''
  syncAreaCode()
}

function handleCityChange() {
  districtCode.value = ''
  syncAreaCode()
}

const form = reactive({
  id: null,
  name: '',
  age: null,
  gender: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  areaCode: [],
  smokingStatus: '',
  cigarettesPerMonth: 0,
  yearlyAcuteExacerbations: 0,
  yearlyHospitalizations: 0,
  fev1Pred: null,
  fev1Fvc: null
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  areaCode: [{ required: true, message: '请选择居住地', trigger: 'blur' }],
  smokingStatus: [{ required: true, message: '请选择吸烟状态', trigger: 'change' }]
}

onMounted(async () => {
  const patientId = route.params.id
  try {
    const res = await api.get(`/patient/${patientId}`)
    if (res.code === 200 && res.data) {
      Object.assign(form, {
        id: res.data.id,
        name: res.data.name || '',
        age: res.data.age || null,
        gender: res.data.gender || '',
        phone: res.data.phone || '',
        province: res.data.province || '',
        city: res.data.city || '',
        district: res.data.district || '',
        areaCode: [],
        smokingStatus: res.data.smokingStatus || '',
        cigarettesPerMonth: res.data.cigarettesPerMonth || 0,
        yearlyAcuteExacerbations: res.data.yearlyAcuteExacerbations || 0,
        yearlyHospitalizations: res.data.yearlyHospitalizations || 0,
        fev1Pred: res.data.fev1Pred || null,
        fev1Fvc: res.data.fev1Fvc || null
      })

      setSplitCodesFromText(form.province, form.city, form.district)
      syncAreaCode()
    }
  } catch (error) {
    ElMessage.error('获取患者信息失败')
  }
})

function goBack() {
  router.back()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    syncAreaCode()
    const submitData = { ...form }
    delete submitData.areaCode
    const res = await api.put('/patient/update', submitData)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      router.push(`/admin/patients/${form.id}`)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.patient-edit-page {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.card-header h2 {
  margin: 0;
}

.area-select {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
  gap: 0.75rem;
  width: 100%;
}

.area-select :deep(.el-select) {
  min-width: 0;
}

@media (max-width: 768px) {
  .area-select {
    grid-template-columns: 1fr;
  }
}
</style>
