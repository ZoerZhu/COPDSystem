<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalPatients || 0 }}</div>
            <div class="stat-label">患者总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon :size="30"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.groupA || 0 }}</div>
            <div class="stat-label">GOLD A组</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon :size="30"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.groupB || 0 }}</div>
            <div class="stat-label">GOLD B组</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon :size="30"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.groupE || 0 }}</div>
            <div class="stat-label">GOLD E组</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>GOLD分组分布</span>
            </div>
          </template>
          <div ref="goldChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近评估趋势</span>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { User, TrendCharts, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore, api } from '@/store'
import * as echarts from 'echarts'

const userStore = useUserStore()
const statistics = ref({})
const goldChartRef = ref(null)
const trendChartRef = ref(null)
const trendLabels = ref([])
const trendCounts = ref([])

function initGoldChart() {
  const chart = echarts.init(goldChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: [
        { value: statistics.value.groupA || 0, name: 'GOLD A', itemStyle: { color: '#67c23a' } },
        { value: statistics.value.groupB || 0, name: 'GOLD B', itemStyle: { color: '#e6a23c' } },
        { value: statistics.value.groupE || 0, name: 'GOLD E', itemStyle: { color: '#f56c6c' } }
      ]
    }]
  }
  chart.setOption(option)
}

function initTrendChart() {
  const chart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trendLabels.value?.length ? trendLabels.value : [] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      data: trendCounts.value?.length ? trendCounts.value : [],
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
      itemStyle: { color: '#409eff' }
    }]
  }
  chart.setOption(option)
}

onMounted(async () => {
  try {
    const res = await api.get('/statistics/dashboard')
    if (res.code === 200) {
      const goldDist = res.data.goldDistribution || {}
      const groupCount = goldDist.groupCount || {}
      const trend = res.data.recentAssessmentTrend || {}
      statistics.value = {
        totalPatients: goldDist.total || 0,
        groupA: groupCount.A || 0,
        groupB: groupCount.B || 0,
        groupE: groupCount.E || 0
      }
      trendLabels.value = trend.labels || []
      trendCounts.value = trend.counts || []
      await nextTick()
      initGoldChart()
      initTrendChart()
    } else {
      ElMessage.error(res.message || '获取统计失败')
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.card-header {
  font-weight: 500;
}
</style>
