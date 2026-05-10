package com.copd.controller;

import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.entity.AbeGroupChangeHistory;
import com.copd.entity.CatAssessment;
import com.copd.entity.Dyspnoea12Assessment;
import com.copd.entity.MmrcAssessment;
import com.copd.entity.PatientInfo;
import com.copd.service.*;
import com.copd.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/feedback")
public class ResultFeedbackController {

    @Autowired
    private ResultFeedbackService resultFeedbackService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private CatAssessmentService catAssessmentService;

    @Autowired
    private MmrcAssessmentService mmrcAssessmentService;

    @Autowired
    private Dyspnoea12AssessmentService dyspnoea12AssessmentService;

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AbeGroupChangeHistoryService abeGroupChangeHistoryService;

    /**
     * 获取GOLD/ABE分组结果反馈
     */
    @GetMapping("/gold")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getGoldGroupFeedback(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        Map<String, Object> goldGroupResult = assessmentService.calculateGoldGroup(patientInfo.getId());
        String goldGroup = (String) goldGroupResult.get("goldGroup");

        CatAssessment latestCat = catAssessmentService.getLatestAssessment(patientInfo.getId());
        MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patientInfo.getId());

        // ABE分组反馈需要 CAT 与 mMRC 均完成
        if (latestCat == null || latestMmrc == null || goldGroup == null) {
            return Result.error("需同时完成CAT与mMRC评估后才能生成ABE分组结果");
        }

        int catScore = latestCat.getTotalScore();
        int mmrcGrade = latestMmrc.getGrade();

        // ABE 分组变化：优先读取 abe_group_change_history 的“最新一条记录”
        // 只要数据库里存在“当前 goldGroup 对应”的记录，就强制展示（即使 previousGroup 为空，也按“未变化”兜底）。
        String previousGoldGroup = null;
        AbeGroupChangeHistory latestHistory = null;

        try {
            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AbeGroupChangeHistory>();
            latestHistory = abeGroupChangeHistoryService.getOne(
                wrapper
                    .eq(AbeGroupChangeHistory::getPatientId, patientInfo.getId())
                    .orderByDesc(AbeGroupChangeHistory::getEvaluationTime)
                    .orderByDesc(AbeGroupChangeHistory::getId)
                    .last("LIMIT 1")
            );
        } catch (Exception ignored) {
            // 查询失败时回退到旧的推算逻辑，保证接口可用
            latestHistory = null;
        }

        if (latestHistory != null
            && latestHistory.getCurrentGroup() != null
            && goldGroup != null
            && goldGroup.equals(latestHistory.getCurrentGroup())) {
            previousGoldGroup = latestHistory.getPreviousGroup();
            if (previousGoldGroup == null) {
                // 服务端要求 previousGoldGroup 非空才能返回 abeGroupChangeFeedback；
                // 此处按“首次记录/未对比”兜底为当前组，前端将展示“分组未变化”的内容。
                previousGoldGroup = goldGroup;
            }
        } else {
            // 回退：计算“上一轮”分组，用于生成 ABE 分组变化的建议与注意
            java.util.Date catDate = latestCat.getAssessmentDate();
            java.util.Date mmrcDate = latestMmrc.getAssessmentDate();
            java.util.Date currentMoment;
            if (catDate == null) {
                currentMoment = mmrcDate;
            } else if (mmrcDate == null) {
                currentMoment = catDate;
            } else {
                currentMoment = catDate.compareTo(mmrcDate) >= 0 ? catDate : mmrcDate;
            }

            CatAssessment previousCat = catAssessmentService.getPreviousAssessmentBefore(patientInfo.getId(), currentMoment);
            MmrcAssessment previousMmrc = mmrcAssessmentService.getPreviousAssessmentBefore(patientInfo.getId(), currentMoment);
            if (previousCat != null && previousMmrc != null) {
                previousGoldGroup = assessmentService.calculateGoldGroupByScores(patientInfo, previousCat.getTotalScore(), previousMmrc.getGrade());
            }
        }

        Map<String, Object> feedback = resultFeedbackService.getGoldGroupFeedback(goldGroup, catScore, mmrcGrade, previousGoldGroup);
        return Result.success(feedback);
    }

    /**
     * 获取Dyspnoea-12量表结果反馈
     */
    @GetMapping("/dyspnoea12")
    @RequireRole(value = {"PATIENT", "ADMIN"})
    public Result<Map<String, Object>> getDyspnoea12Feedback(@RequestHeader("Authorization") String token,
                                                             @RequestParam(required = false) Long patientId) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));

        PatientInfo patientInfo;
        if ("ADMIN".equals(role) && patientId != null) {
            patientInfo = patientInfoService.getById(patientId);
        } else {
            patientInfo = patientInfoService.getMyPatientInfo(userId);
        }

        if (patientInfo == null) {
            return Result.error("患者信息不存在");
        }

        Dyspnoea12Assessment current = dyspnoea12AssessmentService.getLatestAssessment(patientInfo.getId());
        if (current == null) {
            return Result.error("暂无Dyspnoea-12评估记录");
        }

        // 获取上一次的评估记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dyspnoea12Assessment> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Dyspnoea12Assessment::getPatientId, patientInfo.getId());
        wrapper.orderByDesc(Dyspnoea12Assessment::getAssessmentDate);
        wrapper.last("LIMIT 1, 1");
        Dyspnoea12Assessment previous = dyspnoea12AssessmentService.getOne(wrapper);

        Map<String, Object> feedback = resultFeedbackService.getDyspnoea12Feedback(current, previous);
        return Result.success(feedback);
    }

    /**
     * 获取综合结果反馈（GOLD + Dyspnoea-12）
     */
    @GetMapping("/comprehensive")
    @RequireRole(value = {"PATIENT", "ADMIN"})
    public Result<Map<String, Object>> getComprehensiveFeedback(@RequestHeader("Authorization") String token,
                                                                @RequestParam(required = false) Long patientId) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));

        PatientInfo patientInfo;
        if ("ADMIN".equals(role) && patientId != null) {
            patientInfo = patientInfoService.getById(patientId);
        } else {
            patientInfo = patientInfoService.getMyPatientInfo(userId);
        }

        if (patientInfo == null) {
            return Result.error("患者信息不存在");
        }

        // 获取GOLD分组
        Map<String, Object> goldGroupResult = assessmentService.calculateGoldGroup(patientInfo.getId());
        String goldGroup = (String) goldGroupResult.get("goldGroup");

        // 获取Dyspnoea-12评估
        Dyspnoea12Assessment dyspnoea12 = dyspnoea12AssessmentService.getLatestAssessment(patientInfo.getId());
        if (dyspnoea12 == null) {
            return Result.error("暂无Dyspnoea-12评估记录，无法生成综合反馈");
        }

        // 计算Dyspnoea-12三分级
        int totalScore = dyspnoea12.getTotalScore();
        String dyspnoea12Severity;
        if (totalScore <= 12) {
            dyspnoea12Severity = "轻度";
        } else if (totalScore <= 24) {
            dyspnoea12Severity = "中度";
        } else {
            dyspnoea12Severity = "重度";
        }

        // 获取综合反馈
        Map<String, Object> comprehensiveFeedback = resultFeedbackService.getComprehensiveFeedback(goldGroup, dyspnoea12Severity);

        // 整合所有信息
        Map<String, Object> result = new HashMap<>();
        result.put("goldFeedback", resultFeedbackService.getGoldGroupFeedback(goldGroup, 0, 0, null));
        result.put("dyspnoea12Feedback", resultFeedbackService.getDyspnoea12Feedback(dyspnoea12, null));
        result.put("comprehensiveFeedback", comprehensiveFeedback);

        return Result.success(result);
    }

    /**
     * 获取完整评估报告
     */
    @GetMapping("/report")
    @RequireRole(value = {"PATIENT", "ADMIN"})
    public Result<Map<String, Object>> getFullReport(@RequestHeader("Authorization") String token,
                                                     @RequestParam(required = false) Long patientId) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));

        PatientInfo patientInfo;
        if ("ADMIN".equals(role) && patientId != null) {
            patientInfo = patientInfoService.getById(patientId);
        } else {
            patientInfo = patientInfoService.getMyPatientInfo(userId);
        }

        if (patientInfo == null) {
            return Result.error("患者信息不存在");
        }

        Map<String, Object> report = new HashMap<>();

        // 患者基本信息
        report.put("patientInfo", patientInfo);

        // GOLD分组
        Map<String, Object> goldGroupResult = assessmentService.calculateGoldGroup(patientInfo.getId());
        report.put("goldGroup", goldGroupResult);

        // CAT评估
        CatAssessment latestCat = catAssessmentService.getLatestAssessment(patientInfo.getId());
        report.put("catAssessment", latestCat);

        // mMRC评估
        MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patientInfo.getId());
        report.put("mmrcAssessment", latestMmrc);

        // Dyspnoea-12评估
        Dyspnoea12Assessment dyspnoea12 = dyspnoea12AssessmentService.getLatestAssessment(patientInfo.getId());
        report.put("dyspnoea12Assessment", dyspnoea12);

        // 生成反馈
        if (goldGroupResult.get("goldGroup") != null) {
            String goldGroup = (String) goldGroupResult.get("goldGroup");
            int catScore = latestCat != null ? latestCat.getTotalScore() : 0;
            int mmrcGrade = latestMmrc != null ? latestMmrc.getGrade() : 0;
            report.put("goldFeedback", resultFeedbackService.getGoldGroupFeedback(goldGroup, catScore, mmrcGrade, null));
        }

        if (dyspnoea12 != null) {
            report.put("dyspnoea12Feedback", resultFeedbackService.getDyspnoea12Feedback(dyspnoea12, null));
        }

        return Result.success(report);
    }
}
