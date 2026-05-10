<template>
  <div class="patient-info-page" :style="fontSizeStyle">
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <h2>患者信息登记</h2>
          <p class="header-subtitle">请填写您的基本信息，以便进行病情评估</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-width="labelWidth"
        :label-position="labelPosition"
        class="patient-form"
      >
        <el-divider content-position="left">基本信息管理</el-divider>

        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="1" :max="120" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="联系方式" prop="phone">
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

        <el-divider content-position="left">临床指标管理</el-divider>

        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="吸烟状态" prop="smokingStatus">
              <el-select v-model="form.smokingStatus" placeholder="请选择" style="width: 100%">
                <el-option value="NEVER" label="不吸烟" />
                <el-option value="FORMER" label="已戒烟" />
                <el-option value="CURRENT" label="正在吸烟" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24" v-if="form.smokingStatus === 'CURRENT'">
            <el-form-item label="吸烟量">
              <el-input-number v-model="form.cigarettesPerMonth" :min="0" :precision="0" style="width: 100%" />
              <span class="form-unit"> 包/月</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="中度急性加重次数">
              <div class="patient-form__numberField">
                <el-input-number v-model="form.yearlyAcuteExacerbations" :min="0" :max="20" :precision="0" style="width: 100%" />
                <span class="form-unit">近一年内</span>
              </div>
              <div class="form-tip">
                <el-tooltip content="中度急性加重：指呼吸道症状急性恶化，需要额外药物治疗（如短效支气管扩张剂、抗生素、口服激素）但通常不需要住院。仅有严重感冒但未就医、未用额外药物的情况不算急性加重。" placement="top">
                  <el-icon><InfoFilled /></el-icon>
                </el-tooltip>
                <span class="tip-text">什么是中度急性加重？</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="导致住院的重度急性加重次数">
              <div class="patient-form__numberField">
                <el-input-number v-model="form.yearlyHospitalizations" :min="0" :max="10" :precision="0" style="width: 100%" />
                <span class="form-unit">近一年内</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="有史以来急性加重">
              <el-input-number v-model="form.totalAcuteExacerbations" :min="0" :max="100" :precision="0" style="width: 100%" />
              <span class="form-unit"> 次</span>
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="有史以来住院次数">
              <el-input-number v-model="form.totalHospitalizations" :min="0" :max="50" :precision="0" style="width: 100%" />
              <span class="form-unit"> 次</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12" :xs="24">
            <el-form-item label="FEV1%pred">
              <el-input-number v-model="form.fev1Pred" :min="0" :max="150" :precision="1" style="width: 100%" />
              <span class="form-unit"> %</span>
              <div class="form-tip">
                <span class="tip-text">（由医生填写或根据病历）</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12" :xs="24">
            <el-form-item label="FEV1/FVC">
              <el-input-number v-model="form.fev1Fvc" :min="0" :max="150" :precision="1" style="width: 100%" />
              <span class="form-unit"> %</span>
              <div class="form-tip">
                <span class="tip-text">（由医生填写或根据病历）</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" size="large" :loading="saving" @click="handleSubmit">
            保存信息
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { usePatientStore } from '@/store'
import { regionData, codeToText } from 'element-china-area-data'

const router = useRouter()
const patientStore = usePatientStore()

const fontSizeStyle = computed(() => {
  return {
    '--patient-font-size': patientStore.fontSize + 'px'
  }
})

const isNarrow = ref(false)

function syncViewport() {
  isNarrow.value = window.innerWidth <= 900
}

const labelPosition = computed(() => (isNarrow.value ? 'top' : 'right'))
const labelWidth = computed(() => (isNarrow.value ? 'auto' : '200px'))

// 省市区数据（element-china-area-data 的 value 为行政区划 code）
const areaOptions = regionData

// 构建反向查找表：文字 -> code
function buildTextToCode(data) {
  const result = {}
  for (const province of data) {
    result[province.label] = province.value
    if (province.children) {
      result[province.label] = {}
      for (const city of province.children) {
        result[province.label][city.label] = city.value
        if (city.children) {
          result[province.label][city.label] = {}
          for (const district of city.children) {
            result[province.label][city.label][district.label] = district.value
          }
        }
      }
    }
  }
  return result
}

const textToCode = buildTextToCode(regionData)

function findProvinceOption(provinceValue) {
  return areaOptions.find(p => p.value === provinceValue) || null
}

function findCityOption(provinceValue, cityValue) {
  const p = findProvinceOption(provinceValue)
  const cities = p?.children || []
  return cities.find(c => c.value === cityValue) || null
}

// 拆分选择：省/市/区 code
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

// 根据已有省市区数据设置拆分选择器的值（文字 -> code）
function setSplitCodesFromText(provinceText, cityText, districtText) {
  if (!provinceText) {
    provinceCode.value = ''
    cityCode.value = ''
    districtCode.value = ''
    return
  }

  let matchedProvince = null
  let matchedCity = null
  let matchedDistrict = null

  // 直接匹配省份
  if (textToCode[provinceText]) {
    provinceCode.value = textToCode[provinceText]
    matchedProvince = regionData.find(p => p.value === textToCode[provinceText])
  }

  // 如果没找到，尝试模糊匹配
  if (!matchedProvince) {
    const normalizedProvince = provinceText.replace(/省|市|自治区|壮族自治区|回族自治区|维吾尔自治区/g, '')
    for (const p of regionData) {
      const normalizedLabel = p.label.replace(/省|市|自治区|壮族自治区|回族自治区|维吾尔自治区/g, '')
      if (p.label === provinceText || normalizedLabel === normalizedProvince) {
        matchedProvince = p
        provinceCode.value = p.value
        break
      }
    }
  }

  // 查找城市
  if (matchedProvince && cityText) {
    const normalizedCity = cityText.replace(/市|区|县/g, '')
    for (const c of matchedProvince.children || []) {
      const normalizedLabel = c.label.replace(/市|区|县/g, '')
      if (c.label === cityText || normalizedLabel === normalizedCity) {
        matchedCity = c
        cityCode.value = c.value
        break
      }
    }

    // 如果没找到精确匹配，尝试第一个城市（对于直辖市的"市辖区"情况）
    if (!matchedCity && matchedProvince.children && matchedProvince.children.length > 0) {
      // 对于"市辖区"这种情况，直接用第一个子节点
      if (cityText === '市辖区' || cityText === '辖' || !cityText) {
        matchedCity = matchedProvince.children[0]
        cityCode.value = matchedCity.value
      }
    }
  }

  // 查找区县
  if (matchedCity && districtText) {
    for (const d of matchedCity.children || []) {
      if (d.label === districtText) {
        matchedDistrict = d
        districtCode.value = d.value
        break
      }
    }

    // 如果没找到，尝试第一个
    if (!matchedDistrict && matchedCity.children && matchedCity.children.length > 0) {
      matchedDistrict = matchedCity.children[0]
      districtCode.value = matchedDistrict.value
    }
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

function setSplitCodesFromAreaCode(areaCode = []) {
  provinceCode.value = areaCode?.[0] || ''
  cityCode.value = areaCode?.[1] || ''
  districtCode.value = areaCode?.[2] || ''
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

const formRef = ref()
const saving = ref(false)

const form = reactive({
  name: '',
  age: null,
  gender: '',
  birthDate: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  areaCode: [], // 省市区级联选择器绑定的值
  smokingStatus: '',
  cigarettesPerMonth: 0,
  totalAcuteExacerbations: 0,
  totalHospitalizations: 0,
  yearlyAcuteExacerbations: 0,
  yearlyHospitalizations: 0,
  fev1Pred: null,
  fev1Fvc: null
})

watch(() => form.smokingStatus, (next) => {
  if (next !== 'CURRENT') {
    form.cigarettesPerMonth = 0
  }
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
  syncViewport()
  window.addEventListener('resize', syncViewport, { passive: true })

  const patientInfo = await patientStore.getMyPatientInfo()
  if (patientInfo) {
    Object.assign(form, {
      name: patientInfo.name || '',
      age: patientInfo.age || null,
      gender: patientInfo.gender || '',
      birthDate: patientInfo.birthDate || '',
      phone: patientInfo.phone || '',
      province: patientInfo.province || '',
      city: patientInfo.city || '',
      district: patientInfo.district || '',
      areaCode: [],
      smokingStatus: patientInfo.smokingStatus || '',
      cigarettesPerMonth: patientInfo.cigarettesPerMonth || 0,
      totalAcuteExacerbations: patientInfo.totalAcuteExacerbations || 0,
      totalHospitalizations: patientInfo.totalHospitalizations || 0,
      yearlyAcuteExacerbations: patientInfo.yearlyAcuteExacerbations || 0,
      yearlyHospitalizations: patientInfo.yearlyHospitalizations || 0,
      fev1Pred: patientInfo.fev1Pred || null,
      fev1Fvc: patientInfo.fev1Fvc || null
    })

    // 回显到拆分选择器（根据省市区文字查找对应code）
    setSplitCodesFromText(form.province, form.city, form.district)
    // 不修改地址也能保存：将回显的省市区同步到表单校验字段 areaCode
    syncAreaCode()
  }

  // 若由问卷拦截跳转而来，展示说明（字号跟随设置）
  const redirectReason = sessionStorage.getItem('patientInfoRedirectReason')
  if (redirectReason) {
    sessionStorage.removeItem('patientInfoRedirectReason')
    await ElMessageBox.alert(redirectReason, '提示', {
      type: 'warning',
      confirmButtonText: '知道了',
      customClass: 'patient-fontsize-messagebox'
    }).catch(() => {})
  }

  if (!patientInfo) {
    ElMessage.warning('患者信息加载失败，请稍后重试或联系管理员')
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', syncViewport)
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // 防止用户未触发 change 导致省市区文字未同步
    syncAreaCode()

    // 排除areaCode字段，只提交省市区文字
    const submitData = { ...form }
    delete submitData.areaCode

    let success
    if (patientStore.hasPatientInfo) {
      success = await patientStore.updatePatientInfo(submitData)
    } else {
      success = await patientStore.savePatientInfo(submitData)
    }

    if (success) {
      ElMessage.success('保存成功')
      // 记录信息更新时间
      localStorage.setItem('lastInfoUpdateDate', new Date().toISOString())
      const redirectTo = router.currentRoute.value.query.redirect
      if (typeof redirectTo === 'string' && redirectTo.startsWith('/')) {
        router.push(redirectTo)
      } else {
        router.push('/patient/home')
      }
    } else {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.patient-info-page {
  font-size: var(--patient-font-size, 18px);
  /* Element Plus 控件字号随设置变化 */
  --el-font-size-base: var(--patient-font-size, 18px);
  --el-font-size-medium: var(--patient-font-size, 18px);
  --el-font-size-small: calc(var(--patient-font-size, 18px) * 0.9);
  --el-font-size-extra-small: calc(var(--patient-font-size, 18px) * 0.8);
  padding: 1.5rem;
  max-width: 900px;
  margin: 0 auto;
}

.info-card {
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

.patient-form :deep(.el-form-item__label) {
  font-weight: 500;
  font-size: inherit;
}

.patient-form__numberField {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  min-width: 0;
}

.patient-form__numberField :deep(.el-input-number) {
  flex: 1;
  min-width: 0;
}

.patient-form :deep(.el-input__inner),
.patient-form :deep(.el-input-number),
.patient-form :deep(.el-select),
.patient-form :deep(.el-select__wrapper),
.patient-form :deep(.el-textarea__inner) {
  font-size: inherit;
}

.patient-form :deep(.el-divider__text) {
  font-weight: 600;
  color: var(--text-primary);
}

.area-select {
  display: grid;
  grid-template-columns: minmax(12rem, 1.2fr) minmax(12rem, 1fr) minmax(12rem, 1fr);
  gap: 0.75rem;
  width: 100%;
}

.area-select :deep(.el-select) {
  min-width: 0;
}

.patient-info-page :deep(.el-select__selected-item),
.patient-info-page :deep(.el-select__placeholder),
.patient-info-page :deep(.el-input__inner) {
  font-size: inherit;
  text-overflow: ellipsis;
}

.form-unit {
  margin-left: 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
  white-space: nowrap;
}

.form-tip {
  display: flex;
  align-items: center;
  margin-top: 4px;
  font-size: 0.75rem;
  color: #909399;
}

.form-tip .el-icon {
  margin-right: 4px;
  cursor: pointer;
  color: #E6A23C;
}

.tip-text {
  color: #E6A23C;
}

@media (max-width: 768px) {
  .patient-info-page {
    padding: 1rem;
  }

  .area-select {
    grid-template-columns: 1fr;
  }

  .patient-form :deep(.el-col) {
    margin-bottom: 0;
  }

  /* 窄屏下 label 在上方，给控件留足横向空间 */
  .patient-form :deep(.el-form-item__label) {
    justify-content: flex-start;
    line-height: 1.25;
    padding: 0 0 0.35rem 0;
    white-space: normal;
  }
}
</style>
