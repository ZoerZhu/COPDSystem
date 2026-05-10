package com.copd.controller;

import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.entity.CatAssessment;
import com.copd.entity.AbeGroupChangeHistory;
import com.copd.entity.Dyspnoea12Assessment;
import com.copd.entity.MmrcAssessment;
import com.copd.entity.PatientInfo;
import com.copd.service.AssessmentService;
import com.copd.service.AbeGroupChangeHistoryService;
import com.copd.service.CatAssessmentService;
import com.copd.service.Dyspnoea12AssessmentService;
import com.copd.service.MmrcAssessmentService;
import com.copd.service.PatientInfoService;
import com.copd.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/assessment")
public class GoldGroupController {

    @Autowired
    private CatAssessmentService catAssessmentService;

    @Autowired
    private MmrcAssessmentService mmrcAssessmentService;

    @Autowired
    private Dyspnoea12AssessmentService dyspnoea12AssessmentService;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AbeGroupChangeHistoryService abeGroupChangeHistoryService;

    /**
     * 获取我的GOLD/ABE分组
     */
    @GetMapping("/my")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getMyGoldGroup(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        Map<String, Object> result = assessmentService.calculateGoldGroup(patientInfo.getId());
        return Result.success(result);
    }

    /**
     * 获取指定患者的GOLD/ABE分组（管理员）
     */
    @GetMapping("/patient/{patientId}")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getPatientGoldGroup(@PathVariable Long patientId) {
        PatientInfo patientInfo = patientInfoService.getById(patientId);
        if (patientInfo == null) {
            return Result.error("患者信息不存在");
        }

        Map<String, Object> result = assessmentService.calculateGoldGroup(patientId);
        return Result.success(result);
    }

    /**
     * 获取我的评估摘要
     */
    @GetMapping("/summary")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getMyAssessmentSummary(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        Map<String, Object> summary = assessmentService.getAssessmentSummary(patientInfo.getId());
        return Result.success(summary);
    }

    /**
     * 获取评估统计数据（首页用）
     */
    @GetMapping("/stats")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getAssessmentStats(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        Map<String, Object> result = new HashMap<>();
        if (patientInfo == null) {
            result.put("assessmentCount", 0);
            result.put("lastAssessmentDate", "");
            result.put("goldGroup", "");
            return Result.success(result);
        }

        // 获取评估次数
        int catCount = catAssessmentService.getPatientHistory(patientInfo.getId(), 1, 1000).getRecords().size();
        int mmrcCount = mmrcAssessmentService.getPatientHistory(patientInfo.getId(), 1, 1000).getRecords().size();
        int dyspnoeaCount = dyspnoea12AssessmentService.getPatientHistory(patientInfo.getId(), 1, 1000).getRecords().size();
        int totalCount = catCount + mmrcCount + dyspnoeaCount;

        // 获取最后一次评估日期
        String lastDate = "";
        CatAssessment latestCat = catAssessmentService.getLatestAssessment(patientInfo.getId());
        MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patientInfo.getId());
        Dyspnoea12Assessment latestDyspnoea = dyspnoea12AssessmentService.getLatestAssessment(patientInfo.getId());

        if (latestCat != null && latestCat.getAssessmentDate() != null) {
            lastDate = new SimpleDateFormat("yyyy-MM-dd").format(latestCat.getAssessmentDate());
        }
        if (latestMmrc != null && latestMmrc.getAssessmentDate() != null) {
            String mmrcDate = new SimpleDateFormat("yyyy-MM-dd").format(latestMmrc.getAssessmentDate());
            if (lastDate.isEmpty() || mmrcDate.compareTo(lastDate) > 0) {
                lastDate = mmrcDate;
            }
        }
        if (latestDyspnoea != null && latestDyspnoea.getAssessmentDate() != null) {
            String dyspnoeaDate = new SimpleDateFormat("yyyy-MM-dd").format(latestDyspnoea.getAssessmentDate());
            if (lastDate.isEmpty() || dyspnoeaDate.compareTo(lastDate) > 0) {
                lastDate = dyspnoeaDate;
            }
        }

        // 获取GOLD分组
        String goldGroup = "";
        Map<String, Object> goldResult = assessmentService.calculateGoldGroup(patientInfo.getId());
        if (goldResult.get("goldGroup") != null) {
            goldGroup = goldResult.get("goldGroup").toString();
        }

        result.put("assessmentCount", totalCount);
        result.put("lastAssessmentDate", lastDate);
        result.put("goldGroup", goldGroup);
        return Result.success(result);
    }

    /**
     * 获取最新评估结果（结果页面用）
     */
    @GetMapping("/latest")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getLatestAssessments(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        Map<String, Object> result = new HashMap<>();
        if (patientInfo == null) {
            result.put("catScore", 0);
            result.put("mmrcGrade", 0);
            result.put("dyspnoeaData", null);
            result.put("previousDyspnoeaScore", null);
            return Result.success(result);
        }

        // 获取最新CAT评估
        CatAssessment latestCat = catAssessmentService.getLatestAssessment(patientInfo.getId());
        int catScore = latestCat != null ? latestCat.getTotalScore() : 0;

        // 获取最新mMRC评估
        MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patientInfo.getId());
        int mmrcGrade = latestMmrc != null ? latestMmrc.getGrade() : 0;

        // 获取最新Dyspnoea-12评估
        Dyspnoea12Assessment latestDyspnoea = dyspnoea12AssessmentService.getLatestAssessment(patientInfo.getId());
        Map<String, Object> dyspnoeaData = null;
        if (latestDyspnoea != null) {
            dyspnoeaData = new HashMap<>();
            dyspnoeaData.put("totalScore", latestDyspnoea.getTotalScore());
            dyspnoeaData.put("severityLevel", latestDyspnoea.getSeverityLevel());
            dyspnoeaData.put("assessmentDate", latestDyspnoea.getAssessmentDate());
        }

        // 获取上一次的Dyspnoea-12评估
        Integer previousDyspnoeaScore = null;
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dyspnoea12Assessment>();
        wrapper.eq(Dyspnoea12Assessment::getPatientId, patientInfo.getId());
        wrapper.orderByDesc(Dyspnoea12Assessment::getAssessmentDate);
        wrapper.last("LIMIT 1, 1");
        Dyspnoea12Assessment previousDyspnoea = dyspnoea12AssessmentService.getOne(wrapper);
        if (previousDyspnoea != null) {
            previousDyspnoeaScore = previousDyspnoea.getTotalScore();
        }

        result.put("catScore", catScore);
        result.put("mmrcGrade", mmrcGrade);
        result.put("dyspnoeaData", dyspnoeaData);
        result.put("previousDyspnoeaScore", previousDyspnoeaScore);
        return Result.success(result);
    }

    /**
     * 获取患者 ABE 分组变化历史（用于历史记录页）
     */
    @GetMapping("/abe/history")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getMyAbeGroupHistory(
        @RequestHeader("Authorization") String token,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "5") Integer pageSize,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        Date startTime = null;
        if (startDate != null && !startDate.isBlank()) {
            LocalDate start = LocalDate.parse(startDate);
            startTime = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Date endTime = null;
        if (endDate != null && !endDate.isBlank()) {
            LocalDate end = LocalDate.parse(endDate);
            endTime = Date.from(end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // 防御式分页参数
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 5 : pageSize;

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AbeGroupChangeHistory> page =
            abeGroupChangeHistoryService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(safePageNum, safePageSize),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AbeGroupChangeHistory>()
                    .eq(AbeGroupChangeHistory::getPatientId, patientInfo.getId())
                    .ge(startTime != null, AbeGroupChangeHistory::getEvaluationTime, startTime)
                    .lt(endTime != null, AbeGroupChangeHistory::getEvaluationTime, endTime)
                    .orderByDesc(AbeGroupChangeHistory::getEvaluationTime)
                    .orderByDesc(AbeGroupChangeHistory::getId)
            );

        List<Map<String, Object>> records = page.getRecords().stream().map(h -> {
            Map<String, Object> row = new HashMap<>();
            row.put("assessmentDate", h.getEvaluationTime());
            row.put("previousGroup", h.getPreviousGroup());
            row.put("currentGroup", h.getCurrentGroup());
            row.put("changeCategory", h.getChangeCategory());
            row.put("changeCategoryText", h.getChangeCategoryText());
            row.put("coreMedicationAdvice", h.getCoreMedicationAdvice());
            row.put("keyConsiderations", h.getKeyConsiderations());
            return row;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        return Result.success(result);
    }
}
