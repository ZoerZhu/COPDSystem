<template>
  <div class="statistics-page">
    <el-row :gutter="20" class="filter-row">
      <el-col :span="24">
        <el-card class="filter-card">
          <el-form inline>
            <el-form-item label="GOLD分组">
              <el-select v-model="filters.goldGroup" placeholder="全部" clearable style="width: 120px">
                <el-option value="A" label="A组" />
                <el-option value="B" label="B组" />
                <el-option value="E" label="E组" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间段">
              <el-date-picker
                v-model="filters.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="loadStatistics">查询</el-button>
              <el-button :disabled="loading" @click="resetFilters">清空</el-button>
              <el-button :disabled="loading" @click="handleExport">导出数据</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: hsl(210, 80%, 95%)">
            <el-icon :size="28" color="#409EFF"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalPatients }}</div>
            <div class="stat-label">患者总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: hsl(0, 70%, 95%)">
            <el-icon :size="28" color="#F56C6C"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.highRiskCount }}</div>
            <div class="stat-label">高风险患者</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: hsl(35, 80%, 95%)">
            <el-icon :size="28" color="#E6A23C"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.lostFollowCount }}</div>
            <div class="stat-label">失访患者</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: hsl(150, 65%, 95%)">
            <el-icon :size="28" color="#67C23A"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.avgAge }}</div>
            <div class="stat-label">平均年龄</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- GOLD分组分布 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>GOLD分组分布</span>
            </div>
          </template>
          <div class="chart-container" ref="goldChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>年龄分布</span>
            </div>
          </template>
          <div class="chart-container" ref="ageChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 性别和吸烟状况 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>性别构成</span>
            </div>
          </template>
          <div class="chart-container" ref="genderChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>吸烟状况</span>
            </div>
          </template>
          <div class="chart-container" ref="smokingChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 吸烟状态与ABE分组关系 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>吸烟状态与ABE分组分布</span>
            </div>
          </template>
          <div class="chart-container" ref="smokingAbeChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Dyspnoea-12分布 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>Dyspnoea-12评分分布</span>
            </div>
          </template>
          <div class="chart-container" ref="dyspnoeaChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 地区分布（中国地图） -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>地区分布（与A/B/E关系）</span>
            </div>
          </template>
          <div class="chart-container chart-container--map" ref="regionChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 急性加重史 / 住院 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>急性加重史</span>
            </div>
          </template>
          <div class="chart-container" ref="exacerbationChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>住院与季节分布</span>
            </div>
          </template>
          <div class="chart-container" ref="hospitalSeasonChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 高风险患者和失访患者 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>高风险患者（E组）</span>
              <el-tag type="danger" size="small">{{ overview.highRiskCount }}人</el-tag>
            </div>
          </template>
          <el-table :data="highRiskPatients" max-height="300">
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="age" label="年龄" width="60" />
            <el-table-column prop="gender" label="性别" width="60" />
            <el-table-column prop="phone" label="电话" width="120" />
            <el-table-column prop="yearlyAcuteExacerbations" label="年加重" width="80" />
            <el-table-column prop="yearlyHospitalizations" label="年住院" width="80" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewPatient(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>失访患者（90天无评估）</span>
              <el-tag type="warning" size="small">{{ overview.lostFollowCount }}人</el-tag>
            </div>
          </template>
          <el-table :data="lostFollowPatients" max-height="300">
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="age" label="年龄" width="60" />
            <el-table-column prop="gender" label="性别" width="60" />
            <el-table-column prop="phone" label="电话" width="120" />
            <el-table-column prop="lastAssessmentDate" label="最后评估" width="120" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewPatient(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Warning, Clock, TrendCharts } from '@element-plus/icons-vue'
import { useUserStore, api } from '@/store'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const filters = reactive({
  goldGroup: '',
  dateRange: []
})

const loading = ref(false)

const overview = ref({
  totalPatients: 0,
  highRiskCount: 0,
  lostFollowCount: 0,
  avgAge: 0
})

const highRiskPatients = ref([])
const lostFollowPatients = ref([])

const goldChartRef = ref(null)
const ageChartRef = ref(null)
const genderChartRef = ref(null)
const smokingChartRef = ref(null)
const smokingAbeChartRef = ref(null)
const dyspnoeaChartRef = ref(null)
const regionChartRef = ref(null)
const exacerbationChartRef = ref(null)
const hospitalSeasonChartRef = ref(null)

let goldChart = null
let ageChart = null
let genderChart = null
let smokingChart = null
let smokingAbeChart = null
let dyspnoeaChart = null
let regionChart = null
let exacerbationChart = null
let hospitalSeasonChart = null

let latestChartsPayload = null
let chinaMapRegistered = false
let chinaProvinceNameMap = new Map()

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

function handleResize() {
  goldChart?.resize()
  ageChart?.resize()
  genderChart?.resize()
  smokingChart?.resize()
  smokingAbeChart?.resize()
  dyspnoeaChart?.resize()
  regionChart?.resize()
  exacerbationChart?.resize()
  hospitalSeasonChart?.resize()
}

function disposeCharts() {
  goldChart?.dispose()
  ageChart?.dispose()
  genderChart?.dispose()
  smokingChart?.dispose()
  smokingAbeChart?.dispose()
  dyspnoeaChart?.dispose()
  regionChart?.dispose()
  exacerbationChart?.dispose()
  hospitalSeasonChart?.dispose()
  goldChart = null
  ageChart = null
  genderChart = null
  smokingChart = null
  smokingAbeChart = null
  dyspnoeaChart = null
  regionChart = null
  exacerbationChart = null
  hospitalSeasonChart = null
}

function formatDateOnly(d) {
  if (!d) return ''
  const dt = new Date(d)
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const day = String(dt.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function normalizeProvinceName(name) {
  if (!name) return ''
  let n = String(name).trim()
  // 省级口径统一：去掉常见后缀，保持“北京/广西/内蒙古/新疆/香港/澳门”这类短名
  n = n
    .replace(/特别行政区$/u, '')
    .replace(/维吾尔自治区$/u, '')
    .replace(/壮族自治区$/u, '')
    .replace(/回族自治区$/u, '')
    .replace(/自治区$/u, '')
    .replace(/省$/u, '')
    .replace(/市$/u, '')
  return n
}

function buildProvinceAgg(region) {
  const regionCount = region?.regionCount || {}
  const regionGold = region?.regionGoldDistribution || {}

  const provinceAgg = new Map()
  for (const [k, total] of Object.entries(regionCount)) {
    const province = normalizeProvinceName(k.split('-')[0] || '')
    if (!province || province === '未知') continue
    const g = regionGold?.[k] || {}
    const prev = provinceAgg.get(province) || { total: 0, A: 0, B: 0, E: 0 }
    provinceAgg.set(province, {
      total: prev.total + (Number(total) || 0),
      A: prev.A + (Number(g.A) || 0),
      B: prev.B + (Number(g.B) || 0),
      E: prev.E + (Number(g.E) || 0)
    })
  }
  return provinceAgg
}

async function ensureChinaMapRegistered() {
  if (chinaMapRegistered) return
  try {
    const cacheKey = 'copd_china_geojson_v1'
    const cached = sessionStorage.getItem(cacheKey)
    let geo
    if (cached) {
      geo = JSON.parse(cached)
    } else {
      // 省级边界 GeoJSON（DataV 常用资源）
      const res = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
      if (!res.ok) throw new Error(`geojson http ${res.status}`)
      geo = await res.json()
      sessionStorage.setItem(cacheKey, JSON.stringify(geo))
    }
    // 建立 “规范化省名 -> GeoJSON 省名” 映射，确保数据 name 能匹配地图
    chinaProvinceNameMap = new Map()
    if (geo?.features?.length) {
      for (const f of geo.features) {
        const rawName = f?.properties?.name || f?.properties?.NAME || f?.properties?.fullname || f?.properties?.CNAME
        if (!rawName) continue
        const norm = normalizeProvinceName(rawName)
        if (norm) chinaProvinceNameMap.set(norm, rawName)
      }
    }

    echarts.registerMap('china', geo)
    chinaMapRegistered = true
  } catch (e) {
    console.error(e)
    ElMessage.error('中国地图资源加载失败，请检查网络')
  }
}

async function loadStatistics() {
  loading.value = true
  try {
    const params = {
      goldGroup: filters.goldGroup,
      startDate: filters.dateRange?.[0] ? formatDateOnly(filters.dateRange?.[0]) : '',
      endDate: filters.dateRange?.[1] ? formatDateOnly(filters.dateRange?.[1]) : ''
    }

    const res = await api.get('/statistics/overview', { params })
    if (res.code === 200) {
      overview.value = res.data.overview
      highRiskPatients.value = res.data.highRiskPatients || []
      lostFollowPatients.value = res.data.lostFollowPatients || []
      latestChartsPayload = res.data.charts || null
      initCharts(res.data.charts)
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取统计数据失败，已显示示例数据')
    initDemoCharts()
  } finally {
    loading.value = false
  }
}

function initCharts(chartData) {
  const data = chartData || getDemoData()

  if (goldChartRef.value) {
    goldChart?.dispose()
    goldChart = echarts.init(goldChartRef.value)
    goldChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      legend: { bottom: 10 },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}人' },
        data: [
          { value: data.goldGroup.A || 0, name: 'A组', itemStyle: { color: '#67C23A' } },
          { value: data.goldGroup.B || 0, name: 'B组', itemStyle: { color: '#E6A23C' } },
          { value: data.goldGroup.E || 0, name: 'E组', itemStyle: { color: '#F56C6C' } }
        ]
      }]
    })
  }

  if (ageChartRef.value) {
    ageChart?.dispose()
    ageChart = echarts.init(ageChartRef.value)
    const ageGold = data.ageGroupGoldDistribution || null
    const ageCats = ['<50', '50-59', '60-69', '70-79', '≥80']
    const ageStack = ageGold
      ? {
          A: ageCats.map(k => (ageGold[k === '≥80' ? '>=80' : k]?.A ?? 0)),
          B: ageCats.map(k => (ageGold[k === '≥80' ? '>=80' : k]?.B ?? 0)),
          E: ageCats.map(k => (ageGold[k === '≥80' ? '>=80' : k]?.E ?? 0))
        }
      : null
    ageChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: ageStack ? { data: ['A组', 'B组', 'E组'], bottom: 0 } : undefined,
      grid: { left: 40, right: 20, top: 20, bottom: ageStack ? 45 : 30 },
      xAxis: { type: 'category', data: ageCats },
      yAxis: { type: 'value', interval: 1, minInterval: 1 },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      series: ageStack
        ? [
            { name: 'A组', type: 'bar', stack: 'age', data: ageStack.A, itemStyle: { color: '#67C23A' } },
            { name: 'B组', type: 'bar', stack: 'age', data: ageStack.B, itemStyle: { color: '#E6A23C' } },
            { name: 'E组', type: 'bar', stack: 'age', data: ageStack.E, itemStyle: { color: '#F56C6C' } }
          ]
        : [{
            type: 'bar',
            barWidth: '50%',
            data: data.ageDistribution || [0, 0, 0, 0, 0],
            itemStyle: { color: '#409EFF' }
          }]
    })
  }

  if (genderChartRef.value) {
    genderChart?.dispose()
    genderChart = echarts.init(genderChartRef.value)
    genderChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      series: [{
        type: 'pie',
        radius: '65%',
        data: [
          { value: data.gender.male || 0, name: '男', itemStyle: { color: '#409EFF' } },
          { value: data.gender.female || 0, name: '女', itemStyle: { color: '#E6A23C' } }
        ]
      }]
    })
  }

  if (smokingChartRef.value) {
    smokingChart?.dispose()
    smokingChart = echarts.init(smokingChartRef.value)
    smokingChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      series: [{
        type: 'pie',
        radius: '65%',
        data: [
          { value: data.smoking.never || 0, name: '不吸烟', itemStyle: { color: '#67C23A' } },
          { value: data.smoking.former || 0, name: '已戒烟', itemStyle: { color: '#E6A23C' } },
          { value: data.smoking.current || 0, name: '正在吸烟', itemStyle: { color: '#F56C6C' } }
        ]
      }]
    })
  }

  if (smokingAbeChartRef.value) {
    smokingAbeChart?.dispose()
    smokingAbeChart = echarts.init(smokingAbeChartRef.value)

    const smokingGoldDistribution = data.smokingGoldDistribution || {}
    const categories = [
      { status: 'NEVER', label: '不吸烟' },
      { status: 'FORMER', label: '已戒烟' },
      { status: 'CURRENT', label: '正在吸烟' }
    ]

    const aVals = categories.map(c => Number(smokingGoldDistribution?.[c.status]?.A ?? 0))
    const bVals = categories.map(c => Number(smokingGoldDistribution?.[c.status]?.B ?? 0))
    const eVals = categories.map(c => Number(smokingGoldDistribution?.[c.status]?.E ?? 0))
    const sumAt = idx => aVals[idx] + bVals[idx] + eVals[idx]

    smokingAbeChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: params => {
          const header = params?.[0]?.axisValue || ''
          const total = params.reduce((acc, p) => acc + (Number(p?.value) || 0), 0)
          const lines = params.map(p => {
            const v = Number(p.value) || 0
            const pct = total ? ((v / total) * 100).toFixed(1) : '0.0'
            return `${p.marker}${p.seriesName}：${v}人（${pct}%）`
          })
          return [header, ...lines].join('<br/>')
        }
      },
      legend: { data: ['A组', 'B组', 'E组'], bottom: 0 },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      grid: { left: 40, right: 20, top: 28, bottom: 40 },
      xAxis: { type: 'category', data: categories.map(c => c.label) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: 'A组', type: 'bar', stack: 'abe', data: aVals, itemStyle: { color: '#67C23A' }, barWidth: '55%' },
        { name: 'B组', type: 'bar', stack: 'abe', data: bVals, itemStyle: { color: '#E6A23C' }, barWidth: '55%' },
        { name: 'E组', type: 'bar', stack: 'abe', data: eVals, itemStyle: { color: '#F56C6C' }, barWidth: '55%' }
      ]
    })
  }

  if (dyspnoeaChartRef.value) {
    // 处理后端返回的 dyspnoea 数据格式
    let dyspnoeaData = { A: [0, 0, 0, 0, 0], B: [0, 0, 0, 0, 0], E: [0, 0, 0, 0, 0] }
    if (data.dyspnoea && data.dyspnoea.severityGoldDistribution) {
      const severityMap = ['轻度', '轻中度', '中等', '中重度', '重度']
      const severityGold = data.dyspnoea.severityGoldDistribution
      for (let i = 0; i < 5; i++) {
        const sev = severityMap[i]
        if (severityGold[sev]) {
          dyspnoeaData.A[i] = severityGold[sev].A || 0
          dyspnoeaData.B[i] = severityGold[sev].B || 0
          dyspnoeaData.E[i] = severityGold[sev].E || 0
        }
      }
    }
    dyspnoeaChart?.dispose()
    dyspnoeaChart = echarts.init(dyspnoeaChartRef.value)
    dyspnoeaChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['A组', 'B组', 'E组'], bottom: 10 },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      xAxis: { type: 'category', data: ['轻度(0-7)', '轻中度(8-14)', '中度(15-21)', '中重度(22-28)', '重度(29-36)'] },
      yAxis: { type: 'value' },
      series: [
        { name: 'A组', type: 'bar', data: dyspnoeaData.A, itemStyle: { color: '#67C23A' } },
        { name: 'B组', type: 'bar', data: dyspnoeaData.B, itemStyle: { color: '#E6A23C' } },
        { name: 'E组', type: 'bar', data: dyspnoeaData.E, itemStyle: { color: '#F56C6C' } }
      ]
    })
  }

  // 地区分布（中国地图：省级人数 + A/B/E 关系）
  if (regionChartRef.value) {
    regionChart?.dispose()
    regionChart = echarts.init(regionChartRef.value)
    const region = data.region || {}
    const provinceAgg = buildProvinceAgg(region)

    regionChart.showLoading('default', { text: '加载地图中…' })
    ensureChinaMapRegistered().then(() => {
      regionChart.hideLoading()
      if (!chinaMapRegistered) return

      const mapData = Array.from(provinceAgg.entries()).map(([province, v]) => ({
        name: chinaProvinceNameMap.get(province) || province,
        value: v.total
      }))

      const maxValue = Math.max(1, ...mapData.map(i => Number(i.value) || 0))

      regionChart.setOption({
        toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
        visualMap: {
          min: 0,
          max: maxValue,
          left: 10,
          bottom: 10,
          text: ['高', '低'],
          inRange: {
            color: ['hsl(210, 80%, 96%)', 'hsl(215, 70%, 52%)']
          },
          textStyle: { color: '#5a667a' }
        },
        tooltip: {
          trigger: 'item',
          formatter: params => {
            const name = normalizeProvinceName(params.name)
            const v = provinceAgg.get(name)
            if (!v) return `${params.name}<br/>患者数：0`
            const total = v.total || 0
            const a = v.A || 0
            const b = v.B || 0
            const e = v.E || 0
            const pct = x => (total ? ((x / total) * 100).toFixed(1) : '0.0')
            return [
              `<div style="font-weight:600;margin-bottom:6px">${params.name}</div>`,
              `患者数：<b>${total}</b>`,
              `<span style="color:#67C23A">A组</span>：${a}（${pct(a)}%）`,
              `<span style="color:#E6A23C">B组</span>：${b}（${pct(b)}%）`,
              `<span style="color:#F56C6C">E组</span>：${e}（${pct(e)}%）`
            ].join('<br/>')
          }
        },
        series: [
          {
            name: '省级患者分布',
            type: 'map',
            map: 'china',
            roam: true,
            zoom: 1.1,
            itemStyle: {
              borderColor: 'rgba(32, 44, 63, 0.18)',
              borderWidth: 1
            },
            emphasis: {
              label: { show: true },
              itemStyle: {
                borderColor: 'rgba(32, 44, 63, 0.6)',
                borderWidth: 1.5,
                areaColor: 'hsl(215, 70%, 70%)'
              }
            },
            data: mapData
          }
        ]
      })
    })
  }

  // 急性加重史：0/1/2/>=3 分布 + 平均值
  if (exacerbationChartRef.value) {
    exacerbationChart?.dispose()
    exacerbationChart = echarts.init(exacerbationChartRef.value)
    const ex = data.exacerbation || {}
    const group = ex.exacerbationGroupCount || {}
    const cats = ['0次', '1次', '2次', '>=3次']
    const vals = cats.map(k => group[k] ?? 0)
    const avg = Number(ex.averageExacerbations ?? 0)
    exacerbationChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 45, right: 20, top: 28, bottom: 35 },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      title: { text: `年均急性加重次数：${avg.toFixed(2)}`, left: 10, top: 4, textStyle: { fontSize: 12, fontWeight: 600 } },
      xAxis: { type: 'category', data: cats },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'bar', data: vals, itemStyle: { color: '#409EFF' }, barWidth: '55%' }]
    })
  }

  // 住院率 + 季节分布（用 lastAssessmentDate 作为季节归属口径）
  if (hospitalSeasonChartRef.value) {
    hospitalSeasonChart?.dispose()
    hospitalSeasonChart = echarts.init(hospitalSeasonChartRef.value)
    const ex = data.exacerbation || {}
    const hospRate = Number(ex.hospitalizationRate ?? 0)
    const season = ex.hospitalizationSeasonCount || {}
    const scats = ['春', '夏', '秋', '冬', '未知']
    const svals = scats.map(k => season[k] ?? 0)
    hospitalSeasonChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 45, right: 20, top: 28, bottom: 35 },
      toolbox: { right: 10, feature: { saveAsImage: { title: '保存' } } },
      title: { text: `住院率：${hospRate.toFixed(2)}%`, left: 10, top: 4, textStyle: { fontSize: 12, fontWeight: 600 } },
      xAxis: { type: 'category', data: scats },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'bar', data: svals, itemStyle: { color: '#E6A23C' }, barWidth: '55%' }]
    })
  }
}

function getDemoData() {
  return {
    goldGroup: { A: 45, B: 30, E: 25 },
    ageDistribution: [15, 35, 40, 25, 5],
    gender: { male: 65, female: 35 },
    smoking: { never: 30, former: 25, current: 45 },
    smokingGoldDistribution: {
      NEVER: { A: 14, B: 10, E: 6 },
      FORMER: { A: 10, B: 8, E: 7 },
      CURRENT: { A: 10, B: 16, E: 19 },
      UNKNOWN: { A: 0, B: 0, E: 0 }
    },
    dyspnoea: {
      A: [30, 10, 5, 0, 0],
      B: [5, 15, 8, 2, 0],
      E: [2, 8, 10, 3, 2]
    },
    region: {
      regionCount: { '江苏-南京': 18, '江苏-苏州': 12, '浙江-杭州': 10, '未知': 5 },
      regionGoldDistribution: {
        '江苏-南京': { A: 6, B: 8, E: 4 },
        '江苏-苏州': { A: 4, B: 6, E: 2 },
        '浙江-杭州': { A: 2, B: 6, E: 2 },
        '未知': { A: 1, B: 2, E: 2 }
      }
    },
    exacerbation: {
      averageExacerbations: 0.8,
      hospitalizationRate: 12.5,
      exacerbationGroupCount: { '0次': 70, '1次': 18, '2次': 8, '>=3次': 4 },
      hospitalizationSeasonCount: { 春: 2, 夏: 3, 秋: 4, 冬: 3, 未知: 0 }
    }
  }
}

function initDemoCharts() {
  overview.value = { totalPatients: 100, highRiskCount: 25, lostFollowCount: 15, avgAge: 65 }
  initCharts(null)
}

function handleExport() {
  try {
    const charts = latestChartsPayload || getDemoData()
    const lines = []
    lines.push(['模块', '指标', '值'].join(','))
    lines.push(['概览', '患者总数', overview.value.totalPatients].join(','))
    lines.push(['概览', '高风险患者(E)', overview.value.highRiskCount].join(','))
    lines.push(['概览', '失访患者(>90天)', overview.value.lostFollowCount].join(','))
    lines.push(['概览', '平均年龄', overview.value.avgAge].join(','))

    const gold = charts.goldGroup || {}
    lines.push(['GOLD分组', 'A组人数', gold.A ?? 0].join(','))
    lines.push(['GOLD分组', 'B组人数', gold.B ?? 0].join(','))
    lines.push(['GOLD分组', 'E组人数', gold.E ?? 0].join(','))

    const ex = charts.exacerbation || {}
    lines.push(['急性加重', '年均急性加重次数', ex.averageExacerbations ?? 0].join(','))
    lines.push(['急性加重', '住院率(%)', ex.hospitalizationRate ?? 0].join(','))

    lines.push('')
    lines.push('高风险患者(E)名单')
    lines.push(['id', '姓名', '年龄', '性别', '电话', '年加重', '年住院'].join(','))
    for (const p of (highRiskPatients.value || [])) {
      lines.push([p.id, p.name, p.age, p.gender, p.phone, p.yearlyAcuteExacerbations, p.yearlyHospitalizations].map(v => `"${v ?? ''}"`).join(','))
    }

    lines.push('')
    lines.push('失访患者(>90天无评估)名单')
    lines.push(['id', '姓名', '年龄', '性别', '电话', '最后评估'].join(','))
    for (const p of (lostFollowPatients.value || [])) {
      lines.push([p.id, p.name, p.age, p.gender, p.phone, p.lastAssessmentDate].map(v => `"${v ?? ''}"`).join(','))
    }

    const blob = new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `管理员数据统计_${formatDateOnly(new Date())}.csv`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    ElMessage.success('已导出 CSV')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

function viewPatient(row) {
  router.push(`/admin/patients/${row.id}`)
}

function resetFilters() {
  filters.goldGroup = ''
  filters.dateRange = []
  loadStatistics()
}
</script>

<style scoped>
.statistics-page {
  padding: 1.5rem;
}

.filter-row {
  margin-bottom: 1.5rem;
}

.filter-card {
  border-radius: var(--radius-md);
}

.overview-row {
  margin-bottom: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.chart-card,
.table-card {
  margin-bottom: 1.5rem;
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.chart-container {
  height: 280px;
  width: 100%;
}

.chart-container--tall {
  height: 360px;
}

.chart-container--map {
  height: 520px;
}

.chart-container--region-bar {
  height: 560px;
}

@media (max-width: 768px) {
  .overview-row .el-col {
    margin-bottom: 1rem;
  }
}
</style>
