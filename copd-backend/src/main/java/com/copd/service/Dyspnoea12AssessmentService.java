package com.copd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.copd.entity.Dyspnoea12Assessment;
import com.copd.mapper.Dyspnoea12AssessmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.copd.service.AbeGroupChangeHistoryService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class Dyspnoea12AssessmentService extends ServiceImpl<Dyspnoea12AssessmentMapper, Dyspnoea12Assessment> {

    @Autowired
    private AbeGroupChangeHistoryService abeGroupChangeHistoryService;

    /**
     * 提交Dyspnoea-12评估
     */
    public boolean submitAssessment(Dyspnoea12Assessment assessment) {
        int physicalScore = safe(assessment.getQ1BreathlessSudden()) + safe(assessment.getQ2BreathlessHeavy())
                + safe(assessment.getQ3BreathlessExhausted()) + safe(assessment.getQ4BreathlessStarve())
                + safe(assessment.getQ5ChestTightness()) + safe(assessment.getQ6BreathHeavy())
                + safe(assessment.getQ7BreathRapid());

        int emotionalScore = safe(assessment.getQ8Frustrated()) + safe(assessment.getQ9Fear())
                + safe(assessment.getQ10Distress()) + safe(assessment.getQ11Worry())
                + safe(assessment.getQ12Depressed());

        assessment.setPhysicalScore(physicalScore);
        assessment.setEmotionalScore(emotionalScore);
        assessment.setTotalScore(physicalScore + emotionalScore);
        assessment.setAssessmentDate(new Date());
        assessment.setSeverityLevel(getSeverityLevel(physicalScore + emotionalScore));

        boolean saved = this.save(assessment);
        if (saved && assessment.getPatientId() != null) {
            abeGroupChangeHistoryService.recordIfPossible(assessment.getPatientId());
        }
        return saved;
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }

    /**
     * 获取患者的Dyspnoea-12评估历史
     */
    public IPage<Dyspnoea12Assessment> getPatientHistory(
            Long patientId,
            Integer pageNum,
            Integer pageSize,
            String startDate,
            String endDate,
            Integer totalMin,
            Integer totalMax,
            String severityLevel
    ) {
        Page<Dyspnoea12Assessment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Dyspnoea12Assessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dyspnoea12Assessment::getPatientId, patientId);

        if (startDate != null && !startDate.isBlank()) {
            LocalDate start = LocalDate.parse(startDate);
            Date startTime = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            wrapper.ge(Dyspnoea12Assessment::getAssessmentDate, startTime);
        }
        if (endDate != null && !endDate.isBlank()) {
            LocalDate end = LocalDate.parse(endDate);
            Date endTime = Date.from(end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            wrapper.lt(Dyspnoea12Assessment::getAssessmentDate, endTime);
        }
        if (totalMin != null) {
            wrapper.ge(Dyspnoea12Assessment::getTotalScore, totalMin);
        }
        if (totalMax != null) {
            wrapper.le(Dyspnoea12Assessment::getTotalScore, totalMax);
        }
        if (severityLevel != null && !severityLevel.isBlank()) {
            wrapper.eq(Dyspnoea12Assessment::getSeverityLevel, severityLevel);
        }

        wrapper.orderByDesc(Dyspnoea12Assessment::getAssessmentDate);
        return this.page(page, wrapper);
    }

    public IPage<Dyspnoea12Assessment> getPatientHistory(Long patientId, Integer pageNum, Integer pageSize) {
        return getPatientHistory(patientId, pageNum, pageSize, null, null, null, null, null);
    }

    /**
     * 获取患者最新的Dyspnoea-12评估
     */
    public Dyspnoea12Assessment getLatestAssessment(Long patientId) {
        LambdaQueryWrapper<Dyspnoea12Assessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dyspnoea12Assessment::getPatientId, patientId);
        wrapper.orderByDesc(Dyspnoea12Assessment::getAssessmentDate);
        wrapper.orderByDesc(Dyspnoea12Assessment::getId);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 根据总分获取严重程度等级
     */
    public String getSeverityLevel(int totalScore) {
        if (totalScore <= 7) {
            return "轻度";
        } else if (totalScore <= 14) {
            return "轻中度";
        } else if (totalScore <= 21) {
            return "中等";
        } else if (totalScore <= 28) {
            return "中重度";
        } else {
            return "重度";
        }
    }

    /**
     * 根据身体维度得分获取特征描述
     */
    public String getPhysicalDimensionDescription(int physicalScore) {
        if (physicalScore <= 4) {
            return "基本无呼吸不适，仅在极度劳累时出现轻微气短";
        } else if (physicalScore <= 9) {
            return "活动时感觉呼吸费力，但可自行缓解";
        } else if (physicalScore <= 14) {
            return "明显气短感，需放慢活动节奏";
        } else {
            return "严重窒息感，即使轻微活动也难以完成";
        }
    }

    /**
     * 根据情感维度得分获取特征描述
     */
    public String getEmotionalDimensionDescription(int emotionalScore) {
        if (emotionalScore <= 3) {
            return "情绪平稳，能接受疾病状态，无明显心理问题";
        } else if (emotionalScore <= 7) {
            return "轻度烦躁，偶尔因呼吸问题感到沮丧，轻微焦虑倾向";
        } else if (emotionalScore <= 11) {
            return "中度痛苦，常因呼吸困难感到无助，中度焦虑/抑郁，需心理支持";
        } else {
            return "严重心理困扰，感到窒息般的恐惧，重度焦虑/抑郁，需专科干预";
        }
    }
}
