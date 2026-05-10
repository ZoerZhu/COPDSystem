package com.copd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.entity.Dyspnoea12Assessment;
import com.copd.entity.PatientInfo;
import com.copd.service.Dyspnoea12AssessmentService;
import com.copd.service.PatientInfoService;
import com.copd.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/assessment/dyspnoea12")
public class Dyspnoea12AssessmentController {

    @Autowired
    private Dyspnoea12AssessmentService dyspnoea12AssessmentService;

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 提交Dyspnoea-12评估（患者或家属）
     */
    @PostMapping("/submit")
    @RequireRole(value = {"PATIENT", "ADMIN"})
    public Result<String> submitAssessment(@RequestBody Dyspnoea12Assessment assessment,
                                          @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));

        PatientInfo patientInfo = null;
        if ("ADMIN".equals(role)) {
            if (assessment.getPatientId() == null) {
                return Result.error("管理员需要指定患者ID");
            }
            patientInfo = patientInfoService.getById(assessment.getPatientId());
        } else {
            patientInfo = patientInfoService.getMyPatientInfo(userId);
        }

        if (patientInfo == null) {
            return Result.error("患者信息不存在");
        }

        assessment.setPatientId(patientInfo.getId());
        boolean success = dyspnoea12AssessmentService.submitAssessment(assessment);

        if (success) {
            return Result.success("提交成功");
        }
        return Result.error("提交失败");
    }

    /**
     * 获取我的Dyspnoea-12评估历史
     */
    @GetMapping("/history")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getMyHistory(@RequestHeader("Authorization") String token,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate,
                                                     @RequestParam(required = false) Integer totalMin,
                                                     @RequestParam(required = false) Integer totalMax,
                                                     @RequestParam(required = false) String severityLevel) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        IPage<Dyspnoea12Assessment> page = dyspnoea12AssessmentService.getPatientHistory(
                patientInfo.getId(),
                pageNum,
                pageSize,
                startDate,
                endDate,
                totalMin,
                totalMax,
                severityLevel
        );

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());

        return Result.success(result);
    }

    /**
     * 获取最新Dyspnoea-12评估
     */
    @GetMapping("/latest")
    @RequireRole(value = {"PATIENT", "ADMIN"})
    public Result<Map<String, Object>> getLatest(@RequestHeader("Authorization") String token,
                                                  @RequestParam(required = false) Long patientId) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));

        PatientInfo patientInfo = null;
        if ("ADMIN".equals(role) && patientId != null) {
            patientInfo = patientInfoService.getById(patientId);
        } else {
            patientInfo = patientInfoService.getMyPatientInfo(userId);
        }

        if (patientInfo == null) {
            return Result.error("患者信息不存在");
        }

        Dyspnoea12Assessment latest = dyspnoea12AssessmentService.getLatestAssessment(patientInfo.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("assessment", latest);
        if (latest != null) {
            result.put("severityLevel", latest.getSeverityLevel());
            result.put("physicalDescription", dyspnoea12AssessmentService.getPhysicalDimensionDescription(latest.getPhysicalScore()));
            result.put("emotionalDescription", dyspnoea12AssessmentService.getEmotionalDimensionDescription(latest.getEmotionalScore()));
        }

        return Result.success(result);
    }
}
