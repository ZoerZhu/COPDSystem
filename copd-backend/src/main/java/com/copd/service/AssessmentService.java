package com.copd.service;

import com.copd.entity.CatAssessment;
import com.copd.entity.MmrcAssessment;
import com.copd.entity.PatientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AssessmentService {

    @Autowired
    private CatAssessmentService catAssessmentService;

    @Autowired
    private MmrcAssessmentService mmrcAssessmentService;

    @Autowired
    private PatientInfoService patientInfoService;

    /**
     * 计算GOLD/ABE分组
     * E组：过去一年中，≥2次中度急性加重 或 ≥1次导致住院的重度急性加重
     * A组：非E组 且 CAT <10 且 mMRC 0-1级
     * B组：非E组 但 CAT ≥10 或 mMRC ≥2级
     */
    public Map<String, Object> calculateGoldGroup(Long patientId) {
        Map<String, Object> result = new HashMap<>();

        PatientInfo patientInfo = patientInfoService.getById(patientId);
        if (patientInfo == null) {
            result.put("error", "患者信息不存在");
            return result;
        }

        CatAssessment latestCat = catAssessmentService.getLatestAssessment(patientId);
        MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patientId);

        // A/B/E 分组属于“病情评估（自测）”，需要 CAT 与 mMRC 均完成后才给出分组
        if (latestCat == null || latestMmrc == null) {
            result.put("goldGroup", null);
            result.put("groupDescription", "未完成ABE分组评估");
            result.put("riskLevel", "");
            result.put("catScore", latestCat != null ? latestCat.getTotalScore() : null);
            result.put("mmrcGrade", latestMmrc != null ? latestMmrc.getGrade() : null);
            result.put("needAssessments", new String[]{"CAT", "mMRC"});
            result.put("error", "需同时完成CAT与mMRC评估后才能生成ABE分组结果");
            result.put("yearlyAcuteExacerbations", patientInfo.getYearlyAcuteExacerbations());
            result.put("yearlyHospitalizations", patientInfo.getYearlyHospitalizations());
            return result;
        }

        int catScore = latestCat.getTotalScore();
        int mmrcGrade = latestMmrc.getGrade();

        boolean isEGroup = isEGroup(patientInfo);

        if (isEGroup) {
            result.put("goldGroup", "E");
            result.put("groupDescription", "高风险组 - 急性加重风险高");
            result.put("riskLevel", "高");
        } else if (catScore < 10 && mmrcGrade <= 1) {
            result.put("goldGroup", "A");
            result.put("groupDescription", "低风险组 - 症状轻微");
            result.put("riskLevel", "低");
        } else {
            result.put("goldGroup", "B");
            result.put("groupDescription", "低风险组 - 症状严重");
            result.put("riskLevel", "低");
        }

        result.put("catScore", catScore);
        result.put("mmrcGrade", mmrcGrade);
        result.put("yearlyAcuteExacerbations", patientInfo.getYearlyAcuteExacerbations());
        result.put("yearlyHospitalizations", patientInfo.getYearlyHospitalizations());

        return result;
    }

    /**
     * 判断是否为E组（高风险组）
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
    public String calculateGoldGroupByScores(PatientInfo patientInfo, int catScore, int mmrcGrade) {
        if (patientInfo == null) return null;

        boolean isEGroup = isEGroup(patientInfo);
        if (isEGroup) return "E";

        // A组：非E组且 CAT < 10 且 mMRC 0-1级
        if (catScore < 10 && mmrcGrade <= 1) return "A";

        // 其余非E组：B组
        return "B";
    }

    /**
     * 获取评估结果摘要
     */
    public Map<String, Object> getAssessmentSummary(Long patientId) {
        Map<String, Object> summary = new HashMap<>();

        CatAssessment latestCat = catAssessmentService.getLatestAssessment(patientId);
        MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patientId);

        if (latestCat != null) {
            summary.put("catScore", latestCat.getTotalScore());
            summary.put("catSeverity", catAssessmentService.getSeverityLevel(latestCat.getTotalScore()));
            summary.put("catAssessmentDate", latestCat.getAssessmentDate());
        }

        if (latestMmrc != null) {
            summary.put("mmrcGrade", latestMmrc.getGrade());
            summary.put("mmrcDescription", mmrcAssessmentService.getGradeDescription(latestMmrc.getGrade()));
            summary.put("mmrcAssessmentDate", latestMmrc.getAssessmentDate());
        }

        summary.put("goldGroup", calculateGoldGroup(patientId));

        return summary;
    }
}
