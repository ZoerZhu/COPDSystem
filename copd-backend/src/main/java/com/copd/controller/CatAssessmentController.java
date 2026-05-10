package com.copd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.entity.CatAssessment;
import com.copd.entity.PatientInfo;
import com.copd.service.CatAssessmentService;
import com.copd.service.PatientInfoService;
import com.copd.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/assessment/cat")
public class CatAssessmentController {

    @Autowired
    private CatAssessmentService catAssessmentService;

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 提交CAT评估
     */
    @PostMapping("/submit")
    @RequireRole("PATIENT")
    public Result<String> submitAssessment(@RequestBody CatAssessment assessment,
                                           @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        assessment.setPatientId(patientInfo.getId());
        boolean success = catAssessmentService.submitAssessment(assessment);

        if (success) {
            return Result.success("提交成功");
        }
        return Result.error("提交失败");
    }

    /**
     * 获取我的CAT评估历史
     */
    @GetMapping("/history")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getMyHistory(@RequestHeader("Authorization") String token,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate,
                                                     @RequestParam(required = false) Integer totalMin,
                                                     @RequestParam(required = false) Integer totalMax) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        IPage<CatAssessment> page = catAssessmentService.getPatientHistory(
                patientInfo.getId(),
                pageNum,
                pageSize,
                startDate,
                endDate,
                totalMin,
                totalMax
        );

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());

        return Result.success(result);
    }

    /**
     * 获取最新CAT评估
     */
    @GetMapping("/latest")
    @RequireRole("PATIENT")
    public Result<CatAssessment> getLatest(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        PatientInfo patientInfo = patientInfoService.getMyPatientInfo(userId);

        if (patientInfo == null) {
            return Result.error("请先填写患者信息");
        }

        CatAssessment latest = catAssessmentService.getLatestAssessment(patientInfo.getId());
        return Result.success(latest);
    }

    /**
     * 管理员获取指定患者的CAT评估
     */
    @GetMapping("/patient/{patientId}")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getPatientHistory(@PathVariable Long patientId,
                                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CatAssessment> page = catAssessmentService.getPatientHistory(patientId, pageNum, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());

        return Result.success(result);
    }
}
