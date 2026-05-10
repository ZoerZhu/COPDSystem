package com.copd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.copd.entity.CatAssessment;
import com.copd.mapper.CatAssessmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class CatAssessmentService extends ServiceImpl<CatAssessmentMapper, CatAssessment> {

    @Autowired
    private AbeGroupChangeHistoryService abeGroupChangeHistoryService;

    /**
     * 提交CAT评估
     */
    public boolean submitAssessment(CatAssessment assessment) {
        int totalScore = assessment.getQuestion1Cough() + assessment.getQuestion2Sputum()
                + assessment.getQuestion3Chest() + assessment.getQuestion4Breath()
                + assessment.getQuestion5Activity() + assessment.getQuestion6Confidence()
                + assessment.getQuestion7Sleep() + assessment.getQuestion8Energy();
        assessment.setTotalScore(totalScore);
        assessment.setAssessmentDate(new Date());
        boolean saved = this.save(assessment);
        if (saved && assessment.getPatientId() != null) {
            abeGroupChangeHistoryService.recordIfPossible(assessment.getPatientId());
        }
        return saved;
    }

    /**
     * 获取患者的CAT评估历史
     */
    public IPage<CatAssessment> getPatientHistory(
            Long patientId,
            Integer pageNum,
            Integer pageSize,
            String startDate,
            String endDate,
            Integer totalMin,
            Integer totalMax
    ) {
        Page<CatAssessment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CatAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CatAssessment::getPatientId, patientId);

        if (startDate != null && !startDate.isBlank()) {
            LocalDate start = LocalDate.parse(startDate);
            Date startTime = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            wrapper.ge(CatAssessment::getAssessmentDate, startTime);
        }
        if (endDate != null && !endDate.isBlank()) {
            LocalDate end = LocalDate.parse(endDate);
            Date endTime = Date.from(end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            wrapper.lt(CatAssessment::getAssessmentDate, endTime);
        }
        if (totalMin != null) {
            wrapper.ge(CatAssessment::getTotalScore, totalMin);
        }
        if (totalMax != null) {
            wrapper.le(CatAssessment::getTotalScore, totalMax);
        }

        wrapper.orderByDesc(CatAssessment::getAssessmentDate);
        return this.page(page, wrapper);
    }

    public IPage<CatAssessment> getPatientHistory(Long patientId, Integer pageNum, Integer pageSize) {
        return getPatientHistory(patientId, pageNum, pageSize, null, null, null, null);
    }

    /**
     * 获取患者最新的CAT评估
     */
    public CatAssessment getLatestAssessment(Long patientId) {
        LambdaQueryWrapper<CatAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CatAssessment::getPatientId, patientId);
        wrapper.orderByDesc(CatAssessment::getAssessmentDate);
        wrapper.orderByDesc(CatAssessment::getId);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 获取指定时间点之前的上一条 CAT 记录
     */
    public CatAssessment getPreviousAssessmentBefore(Long patientId, Date beforeDate) {
        LambdaQueryWrapper<CatAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CatAssessment::getPatientId, patientId);
        if (beforeDate != null) {
            wrapper.lt(CatAssessment::getAssessmentDate, beforeDate);
        }
        wrapper.orderByDesc(CatAssessment::getAssessmentDate);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 获取患者全部 CAT 记录（按评估时间升序），用于计算 ABE 分组历史
     */
    public List<CatAssessment> listAllByPatientId(Long patientId) {
        LambdaQueryWrapper<CatAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CatAssessment::getPatientId, patientId);
        wrapper.orderByAsc(CatAssessment::getAssessmentDate);
        return this.list(wrapper);
    }

    /**
     * 计算CAT评分
     */
    public int calculateScore(CatAssessment assessment) {
        return assessment.getQuestion1Cough() + assessment.getQuestion2Sputum()
                + assessment.getQuestion3Chest() + assessment.getQuestion4Breath()
                + assessment.getQuestion5Activity() + assessment.getQuestion6Confidence()
                + assessment.getQuestion7Sleep() + assessment.getQuestion8Energy();
    }

    /**
     * 获取CAT评分严重程度
     */
    public String getSeverityLevel(int totalScore) {
        if (totalScore < 10) {
            return "轻微";
        } else if (totalScore < 20) {
            return "中等";
        } else if (totalScore < 30) {
            return "严重";
        } else {
            return "非常严重";
        }
    }
}
