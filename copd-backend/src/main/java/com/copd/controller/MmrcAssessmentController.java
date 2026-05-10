package com.copd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.entity.MmrcAssessment;
import com.copd.entity.PatientInfo;
import com.copd.service.MmrcAssessmentService;
import com.copd.service.PatientInfoService;
import com.copd.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/assessment/mmrc")
public class MmrcAssessmentController {

    @Autowired
    private MmrcAssessmentService mmrcAssessmentService;

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 提交mMRC评估
     */
    @PostMapping("/submit")
    @RequireRole("PATIENT")
    public Result<String> submitAssessment(@RequestBody MmrcAssessment assessment,
                                          @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        assessment.setPatientId(patientInfo.getId());
        assessment.setDescription(mmrcAssessmentService.getGradeDescription(assessment.getGrade()));
        boolean success = mmrcAssessmentService.submitAssessment(assessment);

        if (success) {
            return Result.success("提交成功");
        }
        return Result.error("提交失败");
    }

    /**
     * 获取我的mMRC评估历史
     */
    @GetMapping("/history")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getMyHistory(@RequestHeader("Authorization") String token,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate,
                                                   @RequestParam(required = false) Integer gradeMin,
                                                   @RequestParam(required = false) Integer gradeMax) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        IPage<MmrcAssessment> page = mmrcAssessmentService.getPatientHistory(
                patientInfo.getId(),
                pageNum,
                pageSize,
                startDate,
                endDate,
                gradeMin,
                gradeMax
        );

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());

        return Result.success(result);
    }

    /**
     * 获取最新mMRC评估
     */
    @GetMapping("/latest")
    @RequireRole("PATIENT")
    public Result<MmrcAssessment> getLatest(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        MmrcAssessment latest = mmrcAssessmentService.getLatestAssessment(patientInfo.getId());
        return Result.success(latest);
    }
}
