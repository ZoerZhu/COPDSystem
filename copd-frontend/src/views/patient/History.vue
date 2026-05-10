<template>
  <div class="history-page" :style="fontSizeStyle">
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <h2>历史记录查询</h2>
          <p class="header-tip">可查询历史填写的各项信息和评估结果</p>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <div v-loading="infoLoading" class="info-content">
            <el-descriptions v-if="patientInfo" :column="2" border>
              <el-descriptions-item label="姓名">{{ patientInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ patientInfo.age }}岁</el-descriptions-item>
              <el-descriptions-item label="性别">{{ patientInfo.gender }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ patientInfo.birthDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ patientInfo.phone }}</el-descriptions-item>
              <el-descriptions-item label="居住地">{{ patientInfo.province }} {{ patientInfo.city }}</el-descriptions-item>
            </el-descriptions>
            <div v-else class="empty-tip">
              <el-empty description="暂无基本信息" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="临床指标" name="clinical">
          <div v-loading="infoLoading" class="info-content">
            <el-descriptions v-if="patientInfo" :column="2" border>
              <el-descriptions-item label="吸烟状态">
                <el-tag v-if="patientInfo.smokingStatus === 'NEVER'" type="info">不吸烟</el-tag>
                <el-tag v-else-if="patientInfo.smokingStatus === 'FORMER'" type="warning">已戒烟</el-tag>
                <el-tag v-else-if="patientInfo.smokingStatus === 'CURRENT'" type="danger">正在吸烟</el-tag>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="每月吸烟量">{{ patientInfo.cigarettesPerMonth || 0 }} 包/月</el-descriptions-item>
              <el-descriptions-item label="中度急性加重次数">{{ patientInfo.yearlyAcuteExacerbations || 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="导致住院的重度急性加重次数">{{ patientInfo.yearlyHospitalizations || 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="有史以来急性加重">{{ patientInfo.totalAcuteExacerbations || 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="有史以来住院">{{ patientInfo.totalHospitalizations || 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="FEV1%pred">{{ patientInfo.fev1Pred ? patientInfo.fev1Pred + '%' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="FEV1/FVC">{{ patientInfo.fev1Fvc ? patientInfo.fev1Fvc + '%' : '-' }}</el-descriptions-item>
            </el-descriptions>
            <div v-else class="empty-tip">
              <el-empty description="暂无临床指标" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="CAT评估" name="cat">
          <div class="table-tools">
            <el-form :inline="true" class="search-form" @submit.prevent>
              <el-form-item label="日期">
                <el-date-picker
                  v-model="catQuery.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="总分范围">
                <el-input-number v-model="catQuery.totalMin" :min="0" :max="40" :precision="0" controls-position="right" />
                <span class="range-sep">-</span>
                <el-input-number v-model="catQuery.totalMax" :min="0" :max="40" :precision="0" controls-position="right" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleCatSearch">查询</el-button>
                <el-button @click="resetCatSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table
            v-loading="catLoading"
            :data="catHistory"
            stripe
            table-layout="auto"
            style="width: 100%"
          >
            <el-table-column label="评估日期" min-width="140">
              <template #default="{ row }">{{ formatYmd(row.assessmentDate) }}</template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" min-width="90">
              <template #default="{ row }">
                <span class="score-highlight" :class="getScoreClass(row.totalScore)">
                  {{ row.totalScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="严重程度" min-width="120">
              <template #default="{ row }">
                <el-tag :type="getScoreTagType(row.totalScore)">
                  {{ getScoreLevel(row.totalScore) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="CAT变化" min-width="130">
              <template #default="{ row, $index }">
                <span v-if="$index < catHistory.length - 1">
                  <el-tag :type="getChangeType(catHistory[$index + 1].totalScore - row.totalScore)" size="small">
                    {{ getChangeText(catHistory[$index + 1].totalScore - row.totalScore) }}
                  </el-tag>
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="remarks" label="备注" min-width="220" show-overflow-tooltip />
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="catPage.pageNum"
              v-model:page-size="catPage.pageSize"
              :page-sizes="[5]"
              layout="total, prev, pager, next"
              :total="catPage.total"
              @current-change="loadCatHistory"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="mMRC评估" name="mmrc">
          <div class="table-tools">
            <el-form :inline="true" class="search-form" @submit.prevent>
              <el-form-item label="日期">
                <el-date-picker
                  v-model="mmrcQuery.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="等级范围">
                <el-input-number v-model="mmrcQuery.gradeMin" :min="0" :max="4" :precision="0" controls-position="right" />
                <span class="range-sep">-</span>
                <el-input-number v-model="mmrcQuery.gradeMax" :min="0" :max="4" :precision="0" controls-position="right" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleMmrcSearch">查询</el-button>
                <el-button @click="resetMmrcSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table
            v-loading="mmrcLoading"
            :data="mmrcHistory"
            stripe
            table-layout="auto"
            style="width: 100%"
          >
            <el-table-column label="评估日期" min-width="140">
              <template #default="{ row }">{{ formatYmd(row.assessmentDate) }}</template>
            </el-table-column>
            <el-table-column prop="grade" label="等级" min-width="90">
              <template #default="{ row }">
                <span class="score-highlight">{{ row.grade }}</span>
              </template>
            </el-table-column>
            <el-table-column label="严重程度" min-width="160">
              <template #default="{ row }">
                <el-tag :type="getMmrcTagType(row.grade)">
                  {{ getMmrcLevel(row.grade) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="等级变化" min-width="130">
              <template #default="{ row, $index }">
                <span v-if="$index < mmrcHistory.length - 1">
                  <el-tag :type="getChangeType(mmrcHistory[$index + 1].grade - row.grade)" size="small">
                    {{ getChangeText(mmrcHistory[$index + 1].grade - row.grade) }}
                  </el-tag>
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="320" show-overflow-tooltip />
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="mmrcPage.pageNum"
              v-model:page-size="mmrcPage.pageSize"
              :page-sizes="[5]"
              layout="total, prev, pager, next"
              :total="mmrcPage.total"
              @current-change="loadMmrcHistory"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="ABE分组变化" name="abe">
          <div class="table-tools">
            <el-form :inline="true" class="search-form" @submit.prevent>
              <el-form-item label="日期">
                <el-date-picker
                  v-model="abeQuery.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleAbeSearch">查询</el-button>
                <el-button @click="resetAbeSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table
            v-loading="abeLoading"
            :data="abeHistory"
            stripe
            table-layout="auto"
            style="width: 100%"
          >
            <el-table-column label="评估日期" min-width="140">
              <template #default="{ row }">{{ formatYmd(row.assessmentDate) }}</template>
            </el-table-column>
            <el-table-column label="分组变化" min-width="150">
              <template #default="{ row }">
                <span class="abe-group-change">
                  {{ row.previousGroup }} -> {{ row.currentGroup }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="病情提示" min-width="120">
              <template #default="{ row }">
                <el-tag :type="getAbeTagType(row.changeCategory)">
                  {{ row.changeCategoryText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="核心用药变化建议" min-width="260">
              <template #default="{ row }">
                {{ row.coreMedicationAdvice || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="关键考量与注意事项" min-width="340">
              <template #default="{ row }">
                <div class="abe-key-cell">{{ row.keyConsiderations || '-' }}</div>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="abePage.pageNum"
              v-model:page-size="abePage.pageSize"
              :page-sizes="[5]"
              layout="total, prev, pager, next"
              :total="abePage.total"
              @current-change="loadAbeHistory"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="Dyspnoea-12评估" name="dyspnoea">
          <div class="table-tools">
            <el-form :inline="true" class="search-form" @submit.prevent>
              <el-form-item label="日期">
                <el-date-picker
                  v-model="dyspnoeaQuery.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="严重程度">
                <el-select v-model="dyspnoeaQuery.severityLevel" placeholder="全部" clearable style="width: 140px">
                  <el-option label="轻度" value="轻度" />
                  <el-option label="轻中度" value="轻中度" />
                  <el-option label="中等" value="中等" />
                  <el-option label="中重度" value="中重度" />
                  <el-option label="重度" value="重度" />
                </el-select>
              </el-form-item>
              <el-form-item label="总分范围">
                <el-input-number v-model="dyspnoeaQuery.totalMin" :min="0" :max="36" :precision="0" controls-position="right" />
                <span class="range-sep">-</span>
                <el-input-number v-model="dyspnoeaQuery.totalMax" :min="0" :max="36" :precision="0" controls-position="right" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleDyspnoeaSearch">查询</el-button>
                <el-button @click="resetDyspnoeaSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table
            v-loading="dyspnoeaLoading"
            :data="dyspnoeaHistory"
            stripe
            table-layout="auto"
            style="width: 100%"
          >
            <el-table-column label="评估日期" min-width="140">
              <template #default="{ row }">{{ formatYmd(row.assessmentDate) }}</template>
            </el-table-column>
            <el-table-column label="身体维度" min-width="110">
              <template #default="{ row }">
                {{ row.physicalScore }}
              </template>
            </el-table-column>
            <el-table-column label="情感维度" min-width="110">
              <template #default="{ row }">
                {{ row.emotionalScore }}
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" min-width="90">
              <template #default="{ row }">
                <span class="score-highlight" :class="getDyspnoeaClass(row.totalScore)">
                  {{ row.totalScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="严重程度" min-width="120">
              <template #default="{ row }">
                <el-tag :type="getDyspnoeaTagType(row.totalScore)">
                  {{ getDyspnoeaLevel(row.totalScore) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="总分变化" min-width="150">
              <template #default="{ row, $index }">
                <span v-if="$index < dyspnoeaHistory.length - 1">
                  <el-tag :type="getChangeType(dyspnoeaHistory[$index + 1].totalScore - row.totalScore)" size="small">
                    {{ getChangeText(dyspnoeaHistory[$index + 1].totalScore - row.totalScore) }}
                  </el-tag>
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="severityLevel" label="分级" min-width="110" />
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="dyspnoeaPage.pageNum"
              v-model:page-size="dyspnoeaPage.pageSize"
              :page-sizes="[5]"
              layout="total, prev, pager, next"
              :total="dyspnoeaPage.total"
              @current-change="loadDyspnoeaHistory"
            />
          </div>
        </el-tab-pane>
      </el-tabs>

      <div v-if="!hasAnyData" class="empty-tip">
        <el-empty description="暂无记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { usePatientStore, api } from '@/store'

const router = useRouter()
const patientStore = usePatientStore()

const fontSizeStyle = computed(() => {
  return {
    '--patient-font-size': patientStore.fontSize + 'px'
  }
})

const activeTab = ref('info')
const infoLoading = ref(false)
const catLoading = ref(false)
const mmrcLoading = ref(false)
const dyspnoeaLoading = ref(false)
  const abeLoading = ref(false)

const patientInfo = ref(null)
const catHistory = ref([])
const mmrcHistory = ref([])
const dyspnoeaHistory = ref([])
  const abeHistory = ref([])

const catPage = ref({ pageNum: 1, pageSize: 5, total: 0 })
const mmrcPage = ref({ pageNum: 1, pageSize: 5, total: 0 })
const dyspnoeaPage = ref({ pageNum: 1, pageSize: 5, total: 0 })
  const abePage = ref({ pageNum: 1, pageSize: 5, total: 0 })

const catQuery = ref({
  dateRange: [],
  totalMin: null,
  totalMax: null
})

const mmrcQuery = ref({
  dateRange: [],
  gradeMin: null,
  gradeMax: null
})

const dyspnoeaQuery = ref({
  dateRange: [],
  totalMin: null,
  totalMax: null,
  severityLevel: ''
})

  const abeQuery = ref({
    dateRange: []
  })

const hasAnyData = computed(() => {
    return patientInfo.value || catHistory.value.length > 0 || mmrcHistory.value.length > 0 || dyspnoeaHistory.value.length > 0 || abeHistory.value.length > 0
})

onMounted(() => {
  loadHistory()
})

watch(activeTab, (tab) => {
  if (tab === 'cat') {
    loadCatHistory()
  } else if (tab === 'mmrc') {
    loadMmrcHistory()
    } else if (tab === 'abe') {
      loadAbeHistory()
  } else if (tab === 'dyspnoea') {
    loadDyspnoeaHistory()
  }
})

async function loadHistory() {
  infoLoading.value = true
  catLoading.value = true
  mmrcLoading.value = true
  dyspnoeaLoading.value = true
    abeLoading.value = true

  try {
    // 获取患者基本信息
    const infoRes = await api.get('/patient/my')
    if (infoRes.code === 200 && infoRes.data) {
      patientInfo.value = infoRes.data
    }

    // 获取评估历史
      const [catRes, mmrcRes, dyspnoeaRes, abeRes] = await Promise.all([
      api.get('/assessment/cat/history', { params: { pageNum: catPage.value.pageNum, pageSize: catPage.value.pageSize } }),
      api.get('/assessment/mmrc/history', { params: { pageNum: mmrcPage.value.pageNum, pageSize: mmrcPage.value.pageSize } }),
        api.get('/assessment/dyspnoea12/history', { params: { pageNum: dyspnoeaPage.value.pageNum, pageSize: dyspnoeaPage.value.pageSize } }),
        api.get('/assessment/abe/history', { params: { pageNum: abePage.value.pageNum, pageSize: abePage.value.pageSize } })
    ])

    catHistory.value = catRes.data?.records || []
    mmrcHistory.value = mmrcRes.data?.records || []
    dyspnoeaHistory.value = dyspnoeaRes.data?.records || []
      abeHistory.value = abeRes.data?.records || []
    catPage.value.total = catRes.data?.total || 0
    mmrcPage.value.total = mmrcRes.data?.total || 0
    dyspnoeaPage.value.total = dyspnoeaRes.data?.total || 0
      abePage.value.total = abeRes.data?.total || 0
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    infoLoading.value = false
    catLoading.value = false
    mmrcLoading.value = false
    dyspnoeaLoading.value = false
      abeLoading.value = false
  }
}

async function loadCatHistory() {
  catLoading.value = true
  try {
    const [startDate, endDate] = catQuery.value.dateRange || []
    const res = await api.get('/assessment/cat/history', {
      params: {
        pageNum: catPage.value.pageNum,
        pageSize: catPage.value.pageSize,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        totalMin: catQuery.value.totalMin ?? undefined,
        totalMax: catQuery.value.totalMax ?? undefined
      }
    })
    catHistory.value = res.data?.records || []
    catPage.value.total = res.data?.total || 0
  } finally {
    catLoading.value = false
  }
}

function handleCatSearch() {
  catPage.value.pageNum = 1
  loadCatHistory()
}

function resetCatSearch() {
  catQuery.value = { dateRange: [], totalMin: null, totalMax: null }
  catPage.value.pageNum = 1
  loadCatHistory()
}

function pad2(n) {
  return String(n).padStart(2, '0')
}

function formatYmd(val) {
  if (!val) return '-'
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return String(val).slice(0, 10)
  const y = d.getFullYear()
  const m = pad2(d.getMonth() + 1)
  const day = pad2(d.getDate())
  return `${y}-${m}-${day}`
}
async function loadMmrcHistory() {
  mmrcLoading.value = true
  try {
    const [startDate, endDate] = mmrcQuery.value.dateRange || []
    const res = await api.get('/assessment/mmrc/history', {
      params: {
        pageNum: mmrcPage.value.pageNum,
        pageSize: mmrcPage.value.pageSize,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        gradeMin: mmrcQuery.value.gradeMin ?? undefined,
        gradeMax: mmrcQuery.value.gradeMax ?? undefined
      }
    })
    mmrcHistory.value = res.data?.records || []
    mmrcPage.value.total = res.data?.total || 0
  } finally {
    mmrcLoading.value = false
  }
}

async function loadDyspnoeaHistory() {
  dyspnoeaLoading.value = true
  try {
    const [startDate, endDate] = dyspnoeaQuery.value.dateRange || []
    const res = await api.get('/assessment/dyspnoea12/history', {
      params: {
        pageNum: dyspnoeaPage.value.pageNum,
        pageSize: dyspnoeaPage.value.pageSize,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        totalMin: dyspnoeaQuery.value.totalMin ?? undefined,
        totalMax: dyspnoeaQuery.value.totalMax ?? undefined,
        severityLevel: dyspnoeaQuery.value.severityLevel || undefined
      }
    })
    dyspnoeaHistory.value = res.data?.records || []
    dyspnoeaPage.value.total = res.data?.total || 0
  } finally {
    dyspnoeaLoading.value = false
  }
}

  async function loadAbeHistory() {
    abeLoading.value = true
    try {
      const [startDate, endDate] = abeQuery.value.dateRange || []
      const res = await api.get('/assessment/abe/history', {
        params: {
          pageNum: abePage.value.pageNum,
          pageSize: abePage.value.pageSize,
          startDate: startDate || undefined,
          endDate: endDate || undefined
        }
      })
      abeHistory.value = res.data?.records || []
      abePage.value.total = res.data?.total || 0
    } finally {
      abeLoading.value = false
    }
  }

function handleMmrcSearch() {
  mmrcPage.value.pageNum = 1
  loadMmrcHistory()
}

function resetMmrcSearch() {
  mmrcQuery.value = { dateRange: [], gradeMin: null, gradeMax: null }
  mmrcPage.value.pageNum = 1
  loadMmrcHistory()
}

function handleDyspnoeaSearch() {
  dyspnoeaPage.value.pageNum = 1
  loadDyspnoeaHistory()
}

function resetDyspnoeaSearch() {
  dyspnoeaQuery.value = { dateRange: [], totalMin: null, totalMax: null, severityLevel: '' }
  dyspnoeaPage.value.pageNum = 1
  loadDyspnoeaHistory()
}

  function handleAbeSearch() {
    abePage.value.pageNum = 1
    loadAbeHistory()
  }

  function resetAbeSearch() {
    abeQuery.value = { dateRange: [] }
    abePage.value.pageNum = 1
    loadAbeHistory()
  }

// CAT评分相关
function getScoreClass(score) {
  if (score < 10) return 'score-low'
  if (score < 20) return 'score-medium'
  if (score < 30) return 'score-high'
  return 'score-critical'
}

function getScoreLevel(score) {
  if (score < 10) return '轻微'
  if (score < 20) return '中等'
  if (score < 30) return '严重'
  return '非常严重'
}

function getScoreTagType(score) {
  if (score < 10) return 'success'
  if (score < 20) return 'warning'
  if (score < 30) return 'danger'
  return 'danger'
}

// mMRC相关
function getMmrcLevel(grade) {
  const levels = ['', '极轻', '轻度', '中度', '重度', '极重度']
  return levels[grade] || '-'
}

function getMmrcTagType(grade) {
  if (grade <= 1) return 'success'
  if (grade <= 2) return 'warning'
  return 'danger'
}

// Dyspnoea-12相关
function getDyspnoeaClass(score) {
  if (score <= 12) return 'score-low'
  if (score <= 24) return 'score-medium'
  return 'score-high'
}

function getDyspnoeaLevel(score) {
  if (score <= 12) return '轻度'
  if (score <= 24) return '中度'
  return '重度'
}

function getDyspnoeaTagType(score) {
  if (score <= 12) return 'success'
  if (score <= 24) return 'warning'
  return 'danger'
}

// ABE 分组变化相关
function getAbeTagType(changeCategory) {
  if (changeCategory === 'severe') return 'danger'
  if (changeCategory === 'improved') return 'success'
  if (changeCategory === 'unchanged') return 'info'
  return 'info'
}

// 变化相关
function getChangeType(diff) {
  if (diff >= 6) return 'danger'
  if (diff >= 3) return 'warning'
  if (diff >= -2) return 'info'
  if (diff >= -5) return 'success'
  return 'success'
}

function getChangeText(diff) {
  if (diff >= 6) return `上升${diff}分`
  if (diff >= 3) return `上升${diff}分`
  if (diff >= -2) return '稳定'
  if (diff >= -5) return `下降${Math.abs(diff)}分`
  return `下降${Math.abs(diff)}分`
}
</script>

<style scoped>
.history-page {
  font-size: var(--patient-font-size, 18px);
  padding: 1.5rem;
}

.history-card {
  border-radius: var(--radius-md);
}

.card-header h2 {
  font-size: 1.25rem;
  color: var(--text-primary);
}

.header-tip {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 4px;
}

.info-content {
  min-height: 200px;
}

.score-highlight {
  font-weight: 700;
}

.score-low {
  color: var(--success);
}

.score-medium {
  color: var(--warning);
}

.score-high {
  color: var(--danger);
}

.score-critical {
  color: var(--danger);
  font-size: 1.1em;
}

.empty-tip {
  padding: 3rem;
}

.table-tools {
  padding: 0.75rem 0 0.25rem;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0.5rem;
}

.range-sep {
  display: inline-flex;
  align-items: center;
  padding: 0 0.5rem;
  color: var(--text-secondary);
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  padding: 0.75rem 0 0.25rem;
}

.abe-group-change {
  font-weight: 600;
}

.abe-key-cell {
  white-space: pre-wrap;
  line-height: 1.6;
  word-break: break-word;
}
</style>
