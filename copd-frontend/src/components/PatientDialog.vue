<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑患者' : '新增患者'"
    width="600px"
    :close-on-click-modal="false"
    class="patient-dialog"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年龄" prop="age">
            <el-input-number v-model="form.age" :min="1" :max="120" :precision="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio value="男">男</el-radio>
              <el-radio value="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
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
          <el-form-item label="吸烟状态">
            <el-select v-model="form.smokingStatus" placeholder="请选择" style="width: 100%">
              <el-option value="NEVER" label="从不吸烟" />
              <el-option value="FORMER" label="已戒烟" />
              <el-option value="CURRENT" label="正在吸烟" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="每月吸烟量">
            <el-input-number v-model="form.cigarettesPerMonth" :min="0" :precision="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="中度急性加重次数">
            <el-input-number v-model="form.yearlyAcuteExacerbations" :min="0" :max="20" :precision="0" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="导致住院的重度急性加重次数">
            <el-input-number v-model="form.yearlyHospitalizations" :min="0" :max="10" :precision="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="FEV1%pred">
            <el-input-number v-model="form.fev1Pred" :min="0" :max="150" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="FEV1/FVC">
            <el-input-number v-model="form.fev1Fvc" :min="0" :max="150" :precision="1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore, api } from '@/store'
import { regionData, codeToText } from 'element-china-area-data'

const userStore = useUserStore()

const props = defineProps({
  modelValue: Boolean,
  patientData: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.patientData?.id)

const formRef = ref()
const saving = ref(false)

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
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  areaCode: [{ required: true, message: '请选择居住地', trigger: 'blur' }]
}

watch(() => props.patientData, (val) => {
  if (val) {
    Object.assign(form, val)
    form.areaCode = []
    setSplitCodesFromText(form.province, form.city, form.district)
    syncAreaCode()
  } else {
    Object.assign(form, {
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
    provinceCode.value = ''
    cityCode.value = ''
    districtCode.value = ''
  }
}, { immediate: true })

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    syncAreaCode()
    const submitData = { ...form }
    delete submitData.areaCode
    let res
    if (isEdit.value) {
      res = await api.put('/patient/update', submitData)
    } else {
      // 管理员新增患者时，调用带账户创建的管理员专用接口
      res = await api.post('/patient/admin/add', submitData)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      emit('success')
      visible.value = false
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
:deep(.el-dialog__body) {
  padding: 1.5rem;
}

:deep(.el-form-item) {
  margin-bottom: 1rem;
}

.area-select {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(9.5rem, 1fr));
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
