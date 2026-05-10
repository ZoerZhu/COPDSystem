<template>
  <div class="result-page" :style="fontSizeStyle">
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>评估结果</h2>
            <p class="header-subtitle">您的最新评估结果及详细建议</p>
          </div>
          <div class="header-right">
            <voice-player :text="resultText" :show-label="true" />
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="!hasAssessmentData" class="empty-state">
        <el-empty description="暂无评估记录">
          <el-button type="primary" @click="goToAssessment">开始评估</el-button>
        </el-empty>
      </div>

      <div v-else class="result-content">
        <!-- 单量表结果（用于：只做了CAT或只做了mMRC时也能看到反馈） -->
        <div v-if="catSummary" class="result-section">
          <h3 class="section-title">
            <el-icon><Document /></el-icon>
            CAT评估结果
          </h3>
          <p class="block-text">CAT总分：{{ catSummary.totalScore }} 分（{{ catSummary.severityLevel }}）</p>
          <p v-if="!mmrcSummary" class="block-text">
            ABE分组需要同时完成CAT与mMRC评估。您还差 <strong>mMRC</strong> 未完成。
            <el-button link type="primary" @click="goToMmrc">去做mMRC</el-button>
          </p>
        </div>

        <div v-if="mmrcSummary" class="result-section">
          <h3 class="section-title">
            <el-icon><Document /></el-icon>
            mMRC评估结果
          </h3>
          <p class="block-text">mMRC等级：{{ mmrcSummary.grade }}（{{ mmrcSummary.description }}）</p>
          <p v-if="!catSummary" class="block-text">
            ABE分组需要同时完成CAT与mMRC评估。您还差 <strong>CAT</strong> 未完成。
            <el-button link type="primary" @click="goToCat">去做CAT</el-button>
          </p>
        </div>

        <!-- GOLD/ABE结果反馈（严格按文档文本） -->
        <div v-if="goldFeedback" class="result-section">
          <h3 class="section-title">
            <el-icon><Document /></el-icon>
            ABE分组结果反馈
          </h3>

          <p class="block-text">
            根据CAT量表和mMRC量表和GOLD分组结果显示，您目前处于{{ goldFeedback.riskLevel }}组{{ goldFeedback.groupName }}，您的{{ goldFeedback.prognosis }}。
          </p>
          <p class="block-text">建议：{{ goldFeedback.patientAdvice }}</p>

          <div class="treatment-section">
            <h4 class="treatment-title">治疗建议</h4>
            <div class="treatment-box">
              <div class="treatment-goal">
                <span class="goal-label">治疗目标：</span>
                <span class="goal-text">{{ goldFeedback.treatmentGoal }}</span>
              </div>
              <div class="treatment-plan">
                <span class="plan-label">推荐方案：</span>
                <div class="plan-lines" role="list">
                  <div v-for="(item, index) in goldPlanItems" :key="index" class="plan-item" role="listitem">
                    <p class="plan-line plan-line--main">{{ item.line }}</p>
                    <el-collapse v-if="item.details?.length" class="plan-collapse">
                      <el-collapse-item
                        v-for="(d, dIdx) in item.details"
                        :key="`${index}-${dIdx}`"
                        :title="d.label"
                        :name="`${index}-${dIdx}`"
                      >
                        <p class="plan-detail">{{ d.text }}</p>
                      </el-collapse-item>
                    </el-collapse>
                  </div>
                </div>
              </div>
              <div class="visit-suggestion">
                <span class="visit-label">就医建议：</span>
                <span class="visit-text">{{ goldFeedback.visitFrequency }}</span>
              </div>
              <div class="self-monitor">
                <span class="monitor-label">自我检测频率建议：</span>
                <span class="monitor-text">{{ goldFeedback.selfMonitorFrequency }}</span>
              </div>
              <div v-if="goldFeedback.visitTriggers" class="self-monitor">
                <span class="monitor-label">就医触发条件：</span>
                <span class="monitor-text">{{ goldFeedback.visitTriggers }}</span>
              </div>
            </div>
          </div>

          <!-- ABE分组变化的建议与注意 -->
          <div
            v-if="goldFeedback.abeGroupChangeFeedback"
            class="abe-change-section"
            :class="`abe-change-section--${goldFeedback.abeGroupChangeFeedback.changeCategory}`"
          >
            <h4 class="treatment-title">ABE分组变化的建议与注意</h4>

            <div v-if="abeStrategyDisplay" class="abe-change-strategy">
              <p class="abe-change-strategy__line abe-change-strategy__line--title">{{ abeStrategyDisplay.titleLine }}</p>
              <p class="abe-change-strategy__line">{{ abeStrategyDisplay.mainLine }}</p>

              <p v-for="(line, idx) in abeStrategyDisplay.extraLines" :key="idx" class="abe-change-strategy__line">
                {{ line }}
              </p>

              <p v-if="abeStrategyDisplay.hintLine" class="abe-change-strategy__line abe-change-strategy__line--hint">
                <span class="abe-change-strategy__hint-label">提示：</span>{{ abeStrategyDisplay.hintLine }}
              </p>
            </div>

            <div class="abe-change-table-block">
              <p class="block-text abe-change-subtitle">变化建议与注意</p>
              <div class="abe-change-table-scroll">
                <table class="abe-change-table" role="table" aria-label="ABE分组变化建议与注意">
                  <thead>
                    <tr class="abe-change-table-row abe-change-table-row--header">
                      <th class="abe-change-table__cell abe-change-table__cell--left">核心用药变化建议</th>
                      <th class="abe-change-table__cell abe-change-table__cell--danger">关键考量与注意事项</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="abe-change-table-row abe-change-table-row--active">
                      <td class="abe-change-table__cell">
                        <div class="abe-change-table-cell-text">
                          <p class="abe-change-table-cell-paragraph">
                            {{
                              goldFeedback.abeGroupChangeFeedback.coreMedicationAdvice
                                || (goldFeedback.abeGroupChangeFeedback.changeCategory === 'unchanged'
                                  ? '分组并未变化，请谨遵医嘱进行防范治疗。'
                                  : '—')
                            }}
                          </p>
                        </div>
                      </td>
                      <td class="abe-change-table__cell">
                        <div class="abe-change-table-cell-text">
                          <template v-if="abeKeyConsiderationsRich.bullets.length">
                            <p class="abe-change-table-cell-paragraph">{{ abeKeyConsiderationsRich.lead || '—' }}</p>
                            <ul class="abe-change-bullets">
                              <li v-for="(b, idx) in abeKeyConsiderationsRich.bullets" :key="idx" class="abe-change-bullet-item">
                                <template v-for="(p, pIdx) in abeHighlightParts(b)" :key="pIdx">
                                  <span :class="p.highlight ? 'abe-eos-token abe-eos-token--danger' : 'abe-eos-token'">{{ p.text }}</span>
                                </template>
                              </li>
                            </ul>
                          </template>
                          <template v-else>
                            <p class="abe-change-table-cell-paragraph">
                              {{ goldFeedback.abeGroupChangeFeedback.keyConsiderations || '—' }}
                            </p>
                          </template>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div v-if="goldFeedback.redAlerts?.length" class="alert-section">
            <h4 class="alert-title">
              <el-icon><Warning /></el-icon>
              出现以下情况应立即就医：
            </h4>
            <ul class="alert-list">
              <li v-for="(item, idx) in goldFeedback.redAlerts" :key="idx">{{ item }}</li>
            </ul>
          </div>
        </div>

        <!-- 戒烟提示（仅对当前正在吸烟的患者展示） -->
        <div v-if="shouldShowSmokingCessation" class="result-section smoking-cessation-section">
          <h3 class="section-title">
            <el-icon><Warning /></el-icon>
            戒烟提示
            <el-tag class="smoking-tag" :type="smokingTagType">{{ smokingTagLabel }}</el-tag>
          </h3>
          <p class="block-text smoking-cessation-intro">
            吸烟会显著加重慢阻肺（COPD）的疾病进展、症状控制与预后风险。请尽快在医疗专业人员指导下制定并坚持戒烟方案。
          </p>

          <div class="smoking-voice-wrapper smoking-voice-wrapper--two" aria-label="吸烟危害与建议语音播放">
            <div class="smoking-voice-item" role="group" aria-label="危害语音播放">
              <voice-player :text="smokingHarmVoiceText" :show-label="false" />
              <span class="smoking-voice-item__label">危害</span>
            </div>
            <div class="smoking-voice-item" role="group" aria-label="建议语音播放">
              <voice-player :text="smokingAdviceVoiceText" :show-label="false" />
              <span class="smoking-voice-item__label">建议</span>
            </div>
          </div>

          <el-collapse class="smoking-collapse" accordion>
            <el-collapse-item :title="'吸烟对慢阻肺患者的危害'" name="smoking-harm">
              <div class="smoking-advice-text">{{ smokingHarmText }}</div>
            </el-collapse-item>

            <el-collapse-item :title="'吸烟相关核心建议'" name="smoking-advice">
              <div class="smoking-advice-text">{{ smokingAdviceText }}</div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- Dyspnoea-12结果反馈（严格按文档文本） -->
        <div v-if="dyspnoeaFeedback" class="result-section">
          <h3 class="section-title">
            <el-icon><DataAnalysis /></el-icon>
            Dyspnoea-12量表结果反馈
          </h3>

          <p class="block-text">根据Dyspnoea-12量表的结果显示</p>
          <p class="block-text">
            患者的总分：{{ dyspnoeaFeedback.totalScore }}分  严重程度分级：{{ dyspnoeaFeedback.severityLevel }}   临床特征：{{ dyspnoeaFeedback.clinicalFeatures }}
          </p>
          <p class="block-text">患者身体维度的差异</p>
          <p class="block-text">得分：{{ dyspnoeaFeedback.physicalScore }}分  身体感受特征：{{ dyspnoeaFeedback.physicalDescription }}</p>
          <p class="block-text">患者情感维度的差异</p>
          <p class="block-text">
            得分：{{ dyspnoeaFeedback.emotionalScore }}分  心理状态特征：{{ dyspnoeaFeedback.emotionalDescription }}   可能伴随的问题：{{ dyspnoeaFeedback.psychologicalIssues }}
          </p>

          <p v-if="dyspnoeaChangeText" class="block-text">{{ dyspnoeaChangeText }}</p>

          <div v-if="comprehensiveFeedback?.description" class="dyspnoea-feedback">
            <p class="block-text">{{ comprehensiveFeedback.description }}</p>
          </div>
        </div>

        <div class="action-buttons">
          <el-button type="primary" @click="goToAssessment">重新评估</el-button>
          <el-button @click="viewHistory">查看历史记录</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, DataAnalysis, Warning } from '@element-plus/icons-vue'
import { useUserStore, usePatientStore, api } from '@/store'
import VoicePlayer from '@/components/VoicePlayer.vue'

const router = useRouter()
const userStore = useUserStore()
const patientStore = usePatientStore()

const fontSizeStyle = computed(() => {
  return {
    '--patient-font-size': patientStore.fontSize + 'px'
  }
})

const loading = ref(true)
const goldFeedback = ref(null)
const dyspnoeaFeedback = ref(null)
const comprehensiveFeedback = ref(null)
const catLatest = ref(null)
const mmrcLatest = ref(null)

onMounted(async () => {
  try {
    const [catRes, mmrcRes, dyspRes] = await Promise.all([
      api.get('/assessment/cat/latest').catch(() => null),
      api.get('/assessment/mmrc/latest').catch(() => null),
      api.get('/feedback/dyspnoea12').catch(() => null)
    ])

    // 结果页也需要患者的临床信息（用于展示戒烟提示）
    await patientStore.getMyPatientInfo().catch(() => null)

    if (catRes?.code === 200 && catRes.data) {
      catLatest.value = catRes.data
    }
    if (mmrcRes?.code === 200 && mmrcRes.data) {
      mmrcLatest.value = mmrcRes.data
    }
    if (dyspRes?.code === 200) {
      dyspnoeaFeedback.value = dyspRes.data
    }

    // 仅在 CAT + mMRC 均完成时请求 ABE 分组反馈
    if (catLatest.value && mmrcLatest.value) {
      const goldRes = await api.get('/feedback/gold').catch(() => null)
      if (goldRes?.code === 200) {
        goldFeedback.value = goldRes.data
      }
    }

    // 综合反馈仅在两者都存在时才生成
    if (goldFeedback.value && dyspnoeaFeedback.value?.threeLevelSeverity) {
      const compRes = await api.get('/feedback/comprehensive')
      if (compRes?.code === 200) {
        comprehensiveFeedback.value = compRes.data?.comprehensiveFeedback || compRes.data
      }
    }
  } finally {
    loading.value = false
  }
})

const hasAssessmentData = computed(() => {
  return !!goldFeedback.value || !!dyspnoeaFeedback.value || !!catLatest.value || !!mmrcLatest.value
})

const shouldShowSmokingCessation = computed(() => {
  return patientStore.patientInfo?.smokingStatus === 'CURRENT'
})

const smokingTagLabel = computed(() => {
  const status = patientStore.patientInfo?.smokingStatus
  if (status === 'CURRENT') return '正在吸烟'
  if (status === 'FORMER') return '已戒烟'
  if (status === 'NEVER') return '不吸烟'
  return '—'
})

const smokingTagType = computed(() => {
  const status = patientStore.patientInfo?.smokingStatus
  if (status === 'CURRENT') return 'danger'
  if (status === 'FORMER') return 'warning'
  if (status === 'NEVER') return 'info'
  return 'info'
})

const smokingHarmAndAdviceText = `
一、吸烟对慢阻肺（COPD）患者的危害
吸烟是 COPD 最主要的危险因素，对患者的疾病进展、症状控制、预后均存在多维度严重危害，具体包括：
1.	加速肺功能衰退
吸烟会直接损伤气道上皮细胞，加重气道慢性炎症和结构重塑，导致 FEV1（第一秒用力呼气容积）年下降率显著增加（吸烟患者 FEV1 年下降率约 60-80 mL，是非吸烟患者的 2-3 倍），加速气流阻塞进展，使疾病从轻度向中重度快速恶化。
即使是轻度吸烟（<10 包/年），也会增加中年人群 COPD 发病风险，且与重度吸烟患者相比，可能出现更频繁的急性加重和早期死亡。
2.	加重症状与降低生活质量
吸烟会刺激气道黏液高分泌，加重慢性咳嗽、咳痰、喘息和胸闷症状，同时加剧动态肺过度充气，导致活动后呼吸困难加重，限制患者日常活动能力（如行走、家务）。
长期吸烟会降低患者运动耐力，增加疲劳、焦虑和抑郁风险，显著降低健康相关生活质量（如 CAT 评分升高、Dyspnoea-12评分恶化）。
3.	增加急性加重风险与严重程度
吸烟会削弱气道黏膜屏障功能和免疫防御能力，使患者更易发生呼吸道感染（细菌、病毒），诱发 COPD 急性加重。
吸烟患者的急性加重频率更高、严重程度更重，住院率和急诊就诊率显著增加，且急性加重后恢复时间延长（可达 4-6 周），部分患者无法恢复至急性加重前的功能状态。
4.	升高并发症风险与死亡率
吸烟会增加 COPD 患者合并心血管疾病（如冠心病、心力衰竭、高血压）、骨质疏松、肺癌、焦虑抑郁等并发症的风险，且是导致 COPD 患者死亡的关键危险因素之一。
持续吸烟的 COPD 患者，全因死亡率和 COPD 相关死亡率显著高于已戒烟患者，即使是轻度 COPD 患者，长期吸烟也会增加过早死亡风险。
5.	降低治疗效果
吸烟会降低支气管扩张剂、吸入激素（ICS）等药物的治疗应答，使症状控制不佳，同时增加药物不良反应风险（如 ICS 相关肺炎风险在吸烟患者中更高）。

二、针对慢阻肺（COPD）患者的吸烟相关核心建议
1. 强制戒烟：首要且最有效的干预措施
所有 COPD 患者（无论疾病分期）均需立即、完全戒烟，包括主动吸烟、被动吸烟（二手烟、三手烟）和电子烟。
干预措施：
行为干预：采用 “5A” 策略（Ask 询问吸烟状态、Advise 强烈建议戒烟、Assess 评估戒烟意愿、Assist 提供戒烟计划和心理支持、Arrange 随访），结合个性化（如动机性访谈），推荐参与综合戒烟项目。
药物辅助：优先使用一线戒烟药物（如伐尼克兰、安非他酮缓释剂、尼古丁替代疗法，包括贴片、口香糖、鼻喷剂等），提高长期戒烟成功率（单独行为干预戒烟率约 14%-27%，药物 + 行为干预可提升至 30% 以上）。
避免替代吸烟：不推荐电子烟作为戒烟辅助工具，其气溶胶可能损伤气道上皮、诱发炎症，增加肺部损伤风险，且长期安全性未知。
2. 避免烟草相关暴露
远离二手烟、三手烟环境（如家庭、工作场所、公共场所），避免接触烟草烟雾污染物。
不使用其他烟草制品（如烟斗、雪茄、水烟），此类制品同样会加重气道损伤和炎症。
3. 戒烟后的监测与支持
短期随访：戒烟后 1-3 个月内定期随访，评估戒断症状（如烦躁、失眠、注意力不集中），调整戒烟方案（如药物剂量、心理支持），降低复吸风险。
长期监测：戒烟后每 6-12 个月复查肺功能（FEV1、FVC）、症状评分（CAT、mMRC），评估肺功能下降速率是否减缓，及时调整 COPD 治疗方案。
复吸干预：若发生复吸，避免自责，立即重启戒烟计划，必要时增加药物剂量或更换戒烟药物，强化心理支持。
4. 联合风险因素控制
戒烟同时，减少其他气道刺激物暴露（如家庭燃料烟雾、户外空气污染、职业粉尘和化学物质），避免加重气道损伤。
配合 COPD 规范治疗（如规律使用支气管扩张剂、接种流感 / 肺炎球菌 / RSV 疫苗），提升戒烟后的健康获益。
5. 重点人群强化干预
年轻 COPD 患者（20-50 岁）：吸烟是其发病的主要诱因，需强调早期戒烟对阻止肺功能进一步恶化的关键作用。
重度 COPD 患者：即使疾病进展至晚期，戒烟仍可减少急性加重频率、改善症状、延长生存期。
合并其他疾病患者（如心血管疾病、肺癌）：戒烟可降低并发症恶化风险，提升整体治疗效果。
注意：
吸烟是 COPD 患者疾病进展、症状加重、并发症发生和死亡的首要可控危险因素，戒烟是 COPD 管理中最具成本效益的干预措施，可显著减缓肺功能下降、减少急性加重、改善生活质量并降低死亡率。所有 COPD 患者均需在医疗专业人员指导下，通过 “行为干预 + 药物辅助” 的综合方案实现完全戒烟，并长期避免烟草相关暴露。
`.trim()

const smokingAdviceMarker = '二、针对慢阻肺（COPD）患者的吸烟相关核心建议'
const markerIndex = smokingHarmAndAdviceText.indexOf(smokingAdviceMarker)

const smokingHarmText = markerIndex === -1 ? smokingHarmAndAdviceText : smokingHarmAndAdviceText.slice(0, markerIndex).trim()
const smokingAdviceText = markerIndex === -1 ? smokingHarmAndAdviceText : smokingHarmAndAdviceText.slice(markerIndex).trim()

const smokingHarmVoiceText = computed(() => {
  // 语音播报对连续换行不友好，这里将其压缩为更自然的句间停顿
  return smokingHarmText.replace(/\n+/g, ' ').replace(/\s+/g, ' ').trim()
})

const smokingAdviceVoiceText = computed(() => {
  // 语音播报对连续换行不友好，这里将其压缩为更自然的句间停顿
  return smokingAdviceText.replace(/\n+/g, ' ').replace(/\s+/g, ' ').trim()
})

const catSummary = computed(() => {
  if (!catLatest.value) return null
  const totalScore = Number(catLatest.value.totalScore ?? 0)
  let severityLevel = '轻微'
  if (totalScore < 10) severityLevel = '轻微'
  else if (totalScore < 20) severityLevel = '中等'
  else if (totalScore < 30) severityLevel = '严重'
  else severityLevel = '非常严重'
  return { totalScore, severityLevel }
})

const mmrcSummary = computed(() => {
  if (!mmrcLatest.value) return null
  const grade = Number(mmrcLatest.value.grade ?? 0)
  const descMap = {
    0: '只有在做剧烈活动时才感到呼吸困难',
    1: '在平地快步行走或步行爬小坡时出现气短',
    2: '由于气短，平地行走时比同龄人慢或者需要停下来休息',
    3: '在平地行走约100米或数分钟后需要停下来喘气',
    4: '因为严重呼吸困难而不能离开家，或在穿脱衣服时出现呼吸困难'
  }
  return { grade, description: descMap[grade] || '—' }
})

const resultText = computed(() => {
  if (!hasAssessmentData.value) return '暂无评估记录'
  if (goldFeedback.value) {
    const planText = goldPlanTextForVoice.value ? `治疗推荐方案：${goldPlanTextForVoice.value}` : ''
    const goalText = goldFeedback.value.treatmentGoal ? `治疗目标：${goldFeedback.value.treatmentGoal}` : ''
    return `根据CAT量表和mMRC量表和GOLD分组结果显示，您目前处于${goldFeedback.value.riskLevel}组${goldFeedback.value.groupName}，您的${goldFeedback.value.prognosis}。建议：${goldFeedback.value.patientAdvice}。${goalText}。${planText}`.replace(/。+/g, '。')
  }
  if (dyspnoeaFeedback.value) {
    return `根据Dyspnoea-12量表的结果显示，患者的总分：${dyspnoeaFeedback.value.totalScore}分，严重程度分级：${dyspnoeaFeedback.value.severityLevel}，临床特征：${dyspnoeaFeedback.value.clinicalFeatures}。`
  }
  if (catSummary.value && !mmrcSummary.value) {
    return `CAT评估结果：总分${catSummary.value.totalScore}分，严重程度：${catSummary.value.severityLevel}。ABE分组需要同时完成CAT与mMRC评估，建议继续完成mMRC。`
  }
  if (mmrcSummary.value && !catSummary.value) {
    return `mMRC评估结果：等级${mmrcSummary.value.grade}。ABE分组需要同时完成CAT与mMRC评估，建议继续完成CAT。`
  }
  if (catSummary.value && mmrcSummary.value) {
    return 'CAT与mMRC评估已完成，ABE分组结果将展示在页面中。'
  }
  return '您的评估结果如下。'
})

const goldPlanLines = computed(() => {
  const raw = goldFeedback.value?.recommendedPlan
  if (!raw) return []
  return String(raw).split('\n').map(s => s.trim()).filter(Boolean)
})

const goldClinicalLines = computed(() => {
  const raw = goldFeedback.value?.clinicalNotes
  if (!raw) return []
  return String(raw).split('\n').map(s => s.trim()).filter(Boolean)
})

const goldDetailsByMainLine = computed(() => {
  const lines = goldClinicalLines.value
  if (!lines.length) return new Map()

  const prefixes = [
    { label: '理由', key: '理由：' },
    { label: '临床要点', key: '临床要点：' },
    { label: '替代选择', key: '替代选择：' },
    { label: '特别提醒', key: '特别提醒：' }
  ]

  const map = new Map()
  let currentKey = ''

  for (const line of lines) {
    const hit = prefixes.find(p => line.startsWith(p.key))
    if (!hit) {
      currentKey = line
      if (!map.has(currentKey)) map.set(currentKey, [])
      continue
    }

    if (!currentKey) continue
    const list = map.get(currentKey) || []
    list.push({ label: hit.label, text: line.slice(hit.key.length).trim() })
    map.set(currentKey, list)
  }

  return map
})

const goldPlanItems = computed(() => {
  const map = goldDetailsByMainLine.value
  return goldPlanLines.value.map(line => ({
    line,
    details: map.get(line) || []
  }))
})

const goldPlanTextForVoice = computed(() => {
  if (!goldPlanLines.value.length) return ''
  return goldPlanLines.value.join(' ')
})

const abePrevLabel = computed(() => {
  const g = goldFeedback.value?.abeGroupChangeFeedback?.previousGroup
  if (g === 'A') return 'A组'
  if (g === 'B') return 'B组'
  if (g === 'E') return 'E组'
  return '—'
})

const abeCurrentLabel = computed(() => {
  const g = goldFeedback.value?.abeGroupChangeFeedback?.currentGroup
  if (g === 'A') return 'A组'
  if (g === 'B') return 'B组'
  if (g === 'E') return 'E组'
  return '—'
})

const abeStrategyDisplay = computed(() => {
  const change = goldFeedback.value?.abeGroupChangeFeedback
  if (!change) return null

  const titleLine = `您的 ABE 分组从 ${abePrevLabel.value} 变为 ${abeCurrentLabel.value} 。`

  if (change.changeCategory === 'severe') {
    return {
      titleLine,
      mainLine: '您的分组变化显示您的病情变化，需要马上医院确认，谨慎改变用药。',
      extraLines: [],
      hintLine: '改变药物治疗需经过医生诊断后进行。'
    }
  }

  if (change.changeCategory === 'improved') {
    return {
      titleLine,
      mainLine: '您的分组变化显示您的病情好转，需要密切随访，谨慎调整降级治疗。',
      extraLines: ['若降级后出现症状加重或急性加重，需立即恢复原治疗方案。'],
      hintLine: '改变药物治疗需经过医生诊断后进行。'
    }
  }

  if (change.changeCategory === 'unchanged') {
    return {
      titleLine,
      mainLine: '提醒您的分组并未变化，请谨遵医嘱进行防范治疗。',
      extraLines: [],
      hintLine: ''
    }
  }

  return {
    titleLine,
    mainLine: '分组变化信息不完整，请谨遵医嘱随访。',
    extraLines: [],
    hintLine: ''
  }
})

function abeHighlightParts(text) {
  if (!text) return []

  const regex = /血EOS\s*[<>≤≥]\s*\d+\/μL|FEV1\s*<\s*50%/g
  const parts = []
  let lastIndex = 0

  for (const match of text.matchAll(regex)) {
    const start = match.index ?? 0
    const matchedText = match[0]

    if (start > lastIndex) {
      parts.push({ text: text.slice(lastIndex, start), highlight: false })
    }

    parts.push({ text: matchedText, highlight: true })
    lastIndex = start + matchedText.length
  }

  if (lastIndex < text.length) {
    parts.push({ text: text.slice(lastIndex), highlight: false })
  }

  // 清理空片段，避免渲染时出现多余 span
  return parts.filter(p => p.text !== '')
}

// 将后端 keyConsiderations 中以“•”开头的行渲染成项目符号
const abeKeyConsiderationsRich = computed(() => {
  const raw = goldFeedback.value?.abeGroupChangeFeedback?.keyConsiderations
  if (!raw) return { lead: '', bullets: [] }

  const text = String(raw)
  const lines = text
    .split('\n')
    .map(l => l.trim())
    .filter(Boolean)

  const bullets = []
  const leadLines = []

  for (const line of lines) {
    if (line.startsWith('•')) {
      bullets.push(line.replace(/^•\s*/, ''))
    } else {
      leadLines.push(line)
    }
  }

  if (!bullets.length) {
    return { lead: text, bullets: [] }
  }

  return { lead: leadLines.join('\n'), bullets }
})

const dyspnoeaChangeText = computed(() => {
  const f = dyspnoeaFeedback.value
  if (!f) return ''
  if (typeof f.scoreChange !== 'number') return ''
  const sign = f.scoreChange > 0 ? `上升${f.scoreChange}分` : `下降${Math.abs(f.scoreChange)}分`
  if (f.scoreChange === 0) return '患者的总分变化0分，稳定，无明显变化，维持现有管理'
  return `患者的总分变化${sign}，${f.changeMeaning}，${f.intervention}`
})

function goToAssessment() {
  router.push('/patient/assessment/abe/cat')
}

function goToCat() {
  router.push('/patient/assessment/abe/cat')
}

function goToMmrc() {
  router.push('/patient/assessment/abe/mmrc')
}

function viewHistory() {
  router.push('/patient/history')
}
</script>

<style scoped>
.result-page {
  font-size: var(--patient-font-size, 18px);
  padding: 1.5rem;
  max-width: 1000px;
  margin: 0 auto;
}

.result-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.loading-state,
.empty-state {
  padding: 3rem;
}

.result-content {
  padding: 1rem 0;
}

.result-section {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.125rem;
  color: var(--text-primary);
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.score-card {
  text-align: center;
  padding: 1.5rem 1rem;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  border: 2px solid var(--border-color);
  transition: all var(--transition-fast);
}

.card-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.card-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--primary);
  line-height: 1.2;
}

.card-desc {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: 0.5rem;
}

.gold-card.gold-A {
  border-color: var(--success);
  background: hsl(150, 65%, 95%);
}

.gold-card.gold-A .card-value {
  color: var(--success);
}

.gold-card.gold-B {
  border-color: var(--warning);
  background: hsl(35, 80%, 95%);
}

.gold-card.gold-B .card-value {
  color: var(--warning);
}

.gold-card.gold-E {
  border-color: var(--danger);
  background: hsl(0, 70%, 95%);
}

.gold-card.gold-E .card-value {
  color: var(--danger);
}

.feedback-box {
  margin-top: 1.5rem;
  padding: 1.5rem;
  border-radius: var(--radius-md);
  border-left: 4px solid;
}

.feedback-box h4 {
  font-size: 1.125rem;
  margin-bottom: 0.75rem;
}

.feedback-box p {
  line-height: 1.8;
  margin-bottom: 0.5rem;
}

.feedback-A {
  background: hsl(150, 65%, 95%);
  border-color: var(--success);
  color: var(--text-primary);
}

.feedback-B {
  background: hsl(35, 80%, 95%);
  border-color: var(--warning);
  color: var(--text-primary);
}

.feedback-E {
  background: hsl(0, 70%, 95%);
  border-color: var(--danger);
  color: var(--text-primary);
}

.treatment-section {
  margin-top: 1.5rem;
}

.treatment-title {
  font-size: 1rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.treatment-box {
  background: var(--bg-page);
  padding: 1.5rem;
  border-radius: var(--radius-md);
}

.treatment-goal,
.treatment-plan,
.visit-suggestion,
.self-monitor {
  margin-bottom: 1rem;
}

.goal-label,
.plan-label,
.visit-label,
.monitor-label {
  font-weight: 600;
  color: var(--text-primary);
}

.plan-lines {
  margin-top: 0.5rem;
}

.plan-line {
  margin: 0.35rem 0;
  line-height: 1.75;
  color: var(--text-primary);
  white-space: pre-wrap;
}

.plan-item {
  margin: 0.75rem 0;
}

.plan-line--main {
  font-weight: 600;
}

.plan-collapse {
  margin-top: 0.25rem;
}

.plan-collapse :deep(.el-collapse-item__header) {
  font-weight: 600;
  color: var(--text-secondary);
}

.plan-detail {
  margin: 0.35rem 0;
  line-height: 1.75;
  color: var(--text-secondary);
  white-space: pre-wrap;
}


.alert-section {
  margin-top: 1.5rem;
  padding: 1.5rem;
  background: hsl(0, 70%, 95%);
  border-radius: var(--radius-md);
  border-left: 4px solid var(--danger);
}

.alert-title {
  font-size: 1rem;
  color: var(--danger);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.alert-list {
  margin: 0;
  padding-left: 1.5rem;
}

.alert-list li {
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  line-height: 1.6;
}

.dyspnoea-feedback {
  margin-top: 1.5rem;
  padding: 1rem;
  background: var(--bg-page);
  border-radius: var(--radius-md);
}

.dyspnoea-feedback p {
  margin: 0.5rem 0;
  line-height: 1.6;
}

.block-text {
  margin: 0.5rem 0;
  line-height: 1.8;
  color: var(--text-primary);
}

.abe-change-section {
  margin-top: 1.5rem;
  padding: 1.25rem 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  background: var(--bg-page);
}

.abe-change-section--severe {
  background: hsl(0, 70%, 96%);
  border-left: 4px solid var(--danger);
}

.abe-change-section--improved {
  background: hsl(150, 65%, 95%);
  border-left: 4px solid var(--success);
}

.abe-change-section--unchanged {
  background: hsl(215, 70%, 97%);
  border-left: 4px solid var(--primary);
}

.abe-change-subtitle {
  font-weight: 600;
  color: var(--text-secondary);
  margin-top: 0.75rem;
}

.abe-change-text {
  white-space: pre-wrap;
}

.abe-change-strategy {
  margin-top: 0.75rem;
}

.abe-change-strategy__line {
  margin: 0;
  line-height: 1.9;
  white-space: pre-wrap;
}

.abe-change-strategy__line--title {
  font-weight: 700;
}

.abe-change-strategy__line--hint {
  margin-top: 0.25rem;
}

.abe-change-strategy__hint-label {
  color: var(--primary);
  font-weight: 700;
}

.abe-change-table-block {
  margin-top: 0.75rem;
}

.abe-change-table-scroll {
  margin-top: 0.75rem;
  overflow-x: auto;
  padding-bottom: 0.25rem;
}

.abe-change-table {
  width: 100%;
  min-width: 620px;
  border-collapse: collapse;
}

.abe-change-table-row--header .abe-change-table__cell {
  background: hsl(215, 70%, 97%);
  color: var(--text-secondary);
  font-weight: 600;
}

.abe-change-table__cell--danger {
  color: var(--danger);
}

.abe-change-table__cell {
  border: 1px solid var(--border-color);
  padding: 0.75rem;
  vertical-align: top;
  text-align: left;
  line-height: 1.7;
  color: var(--text-primary);
}

.abe-change-table-row--active .abe-change-table__cell {
  background: transparent;
}

.abe-change-section--severe .abe-change-table-row--active .abe-change-table__cell {
  background: hsl(0, 70%, 96%);
}

.abe-change-section--improved .abe-change-table-row--active .abe-change-table__cell {
  background: hsl(150, 65%, 94%);
}

.abe-change-section--unchanged .abe-change-table-row--active .abe-change-table__cell {
  background: hsl(215, 70%, 98%);
}

.abe-change-table-cell-paragraph {
  margin: 0;
  white-space: pre-wrap;
}

.abe-change-bullets {
  margin: 0.35rem 0 0;
  padding-left: 1.25rem;
}

.abe-change-bullets li {
  margin: 0.35rem 0;
  line-height: 1.65;
}

.abe-change-bullet-item {
  list-style-position: outside;
}

.abe-eos-token {
  color: var(--text-primary);
}

.abe-eos-token--danger {
  color: var(--danger);
  font-weight: 700;
}

.action-buttons {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.smoking-cessation-section {
  margin-top: 1.5rem;
  padding: 1.25rem 1.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  border-left: 4px solid var(--danger);
  box-shadow: var(--shadow-sm);
  background: hsl(0, 70%, 96%);
}

.smoking-tag {
  margin-left: 0.5rem;
}

.smoking-cessation-intro {
  color: var(--text-primary);
}

.smoking-voice-wrapper {
  margin: 1rem 0 0.75rem;
}

.smoking-voice-wrapper--two {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.smoking-voice-item {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.smoking-voice-item__label {
  font-size: 0.95rem;
  color: var(--text-secondary);
}

.smoking-collapse :deep(.el-collapse-item__header) {
  font-weight: 700;
  color: var(--text-primary);
}

.smoking-collapse :deep(.el-collapse-item__content) {
  padding-top: 0.25rem;
}

.smoking-advice-text {
  white-space: pre-wrap;
  line-height: 1.85;
  color: var(--text-primary);
}
</style>
