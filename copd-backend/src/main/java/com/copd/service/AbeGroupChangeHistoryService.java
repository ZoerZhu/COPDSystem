package com.copd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.copd.mapper.CatAssessmentMapper;
import com.copd.mapper.MmrcAssessmentMapper;
import com.copd.entity.AbeGroupChangeHistory;
import com.copd.entity.CatAssessment;
import com.copd.entity.MmrcAssessment;
import com.copd.entity.PatientInfo;
import com.copd.mapper.AbeGroupChangeHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class AbeGroupChangeHistoryService extends ServiceImpl<AbeGroupChangeHistoryMapper, AbeGroupChangeHistory> {

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private CatAssessmentMapper catAssessmentMapper;

    @Autowired
    private MmrcAssessmentMapper mmrcAssessmentMapper;

    @Autowired
    private ResultFeedbackService resultFeedbackService;

    /**
     * 当 CAT 或 mMRC 提交后调用：
     * - 若患者已存在一对（最新CAT + 最新mMRC），则计算当前 A/B/E 并写入“分组变化历史”
     */
    public void recordIfPossible(Long patientId) {
        recordInternal(patientId, true);
    }

    /**
     * 当患者基础信息（如急性加重/住院次数）变更后调用：
     * - 若当前 A/B/E 分组与上一条历史记录不同，则写入“分组变化历史”
     */
    public void recordIfGroupChanged(Long patientId) {
        recordInternal(patientId, false);
    }

    private void recordInternal(Long patientId, boolean forceWrite) {
        PatientInfo patientInfo = patientInfoService.getById(patientId);
        if (patientInfo == null) return;

        // 直接用 Mapper 查询，避免再注入 CatAssessmentService/MmrcAssessmentService 导致循环依赖
        LambdaQueryWrapper<CatAssessment> catWrapper = new LambdaQueryWrapper<>();
        catWrapper.eq(CatAssessment::getPatientId, patientId)
            .orderByDesc(CatAssessment::getAssessmentDate)
            .orderByDesc(CatAssessment::getId)
            .last("LIMIT 1");
        CatAssessment latestCat = catAssessmentMapper.selectOne(catWrapper);

        LambdaQueryWrapper<MmrcAssessment> mmrcWrapper = new LambdaQueryWrapper<>();
        mmrcWrapper.eq(MmrcAssessment::getPatientId, patientId)
            .orderByDesc(MmrcAssessment::getAssessmentDate)
            .orderByDesc(MmrcAssessment::getId)
            .last("LIMIT 1");
        MmrcAssessment latestMmrc = mmrcAssessmentMapper.selectOne(mmrcWrapper);

        // ABE分组必须 CAT + mMRC 都存在
        if (latestCat == null || latestMmrc == null) return;

        String currentGroup = calculateGoldGroupByScores(
                patientInfo,
                latestCat.getTotalScore(),
                latestMmrc.getGrade()
        );
        if (currentGroup == null) return;

        // previousGroup 取“最近一条历史记录”的 currentGroup
        LambdaQueryWrapper<AbeGroupChangeHistory> prevWrapper = new LambdaQueryWrapper<>();
        prevWrapper.eq(AbeGroupChangeHistory::getPatientId, patientId)
            .orderByDesc(AbeGroupChangeHistory::getEvaluationTime)
            .orderByDesc(AbeGroupChangeHistory::getId)
            .last("LIMIT 1");
        AbeGroupChangeHistory previous = this.getOne(prevWrapper);
        String previousGroup = previous != null ? previous.getCurrentGroup() : null;

        if (!forceWrite && previousGroup != null && previousGroup.equals(currentGroup)) {
            return;
        }

        // 需求要求：用户信息变化后，CAT/mMRC/Dyspnoea-12 三次问卷提交都要记录当次 ABE 结果；
        // 即使分组未变化，也需要写入“分组未变化”的提醒。
        Map<String, Object> advice = resultFeedbackService.getAbeGroupChangeAdvice(previousGroup, currentGroup);

        AbeGroupChangeHistory history = new AbeGroupChangeHistory();
        history.setPatientId(patientId);
        history.setCatAssessmentId(latestCat.getId());
        history.setMmrcAssessmentId(latestMmrc.getId());

        // 用当前时间保证同日多次提交也能正确排序
        history.setEvaluationTime(new Date());

        history.setPreviousGroup(previousGroup);
        history.setCurrentGroup(currentGroup);

        history.setChangeCategory((String) advice.get("changeCategory"));
        history.setChangeCategoryText((String) advice.get("changeCategoryText"));
        history.setCoreMedicationAdvice((String) advice.get("coreMedicationAdvice"));
        history.setKeyConsiderations((String) advice.get("keyConsiderations"));
        history.setStrategyTitle((String) advice.get("strategyTitle"));
        history.setStrategyParagraph((String) advice.get("strategyParagraph"));

        this.save(history);
    }

    /**
     * 判断是否为 E 组（高风险组）
     * E组：过去一年中，>=2次中度急性加重 或 >=1次导致住院的重度急性加重
     */
    private boolean isEGroup(PatientInfo patientInfo) {
        int acuteExacerbations = patientInfo.getYearlyAcuteExacerbations() != null
                ? patientInfo.getYearlyAcuteExacerbations() : 0;
        int hospitalizations = patientInfo.getYearlyHospitalizations() != null
                ? patientInfo.getYearlyHospitalizations() : 0;
        return acuteExacerbations >= 2 || hospitalizations >= 1;
    }

    /**
     * 基于指定的 CAT/mMRC 分数计算 A/B/E 分组（不依赖最新一条记录）
     */
    private String calculateGoldGroupByScores(PatientInfo patientInfo, int catScore, int mmrcGrade) {
        if (patientInfo == null) return null;

        boolean eGroup = isEGroup(patientInfo);
        if (eGroup) return "E";

        // A组：非E组 且 CAT <10 且 mMRC 0-1级
        if (catScore < 10 && mmrcGrade <= 1) return "A";

        // 其余非E组：B组
        return "B";
    }
}

