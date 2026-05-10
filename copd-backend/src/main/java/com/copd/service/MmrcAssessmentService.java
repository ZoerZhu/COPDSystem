package com.copd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.copd.entity.MmrcAssessment;
import com.copd.mapper.MmrcAssessmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class MmrcAssessmentService extends ServiceImpl<MmrcAssessmentMapper, MmrcAssessment> {

    @Autowired
    private AbeGroupChangeHistoryService abeGroupChangeHistoryService;

    /**
     * 提交mMRC评估
     */
    public boolean submitAssessment(MmrcAssessment assessment) {
        assessment.setAssessmentDate(new Date());
        boolean saved = this.save(assessment);
        if (saved && assessment.getPatientId() != null) {
            abeGroupChangeHistoryService.recordIfPossible(assessment.getPatientId());
        }
        return saved;
    }

    /**
     * 获取患者的mMRC评估历史
     */
    public IPage<MmrcAssessment> getPatientHistory(
            Long patientId,
            Integer pageNum,
            Integer pageSize,
            String startDate,
            String endDate,
            Integer gradeMin,
            Integer gradeMax
    ) {
        Page<MmrcAssessment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MmrcAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MmrcAssessment::getPatientId, patientId);

        if (startDate != null && !startDate.isBlank()) {
            LocalDate start = LocalDate.parse(startDate);
            Date startTime = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            wrapper.ge(MmrcAssessment::getAssessmentDate, startTime);
        }
        if (endDate != null && !endDate.isBlank()) {
            LocalDate end = LocalDate.parse(endDate);
            Date endTime = Date.from(end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            wrapper.lt(MmrcAssessment::getAssessmentDate, endTime);
        }
        if (gradeMin != null) {
            wrapper.ge(MmrcAssessment::getGrade, gradeMin);
        }
        if (gradeMax != null) {
            wrapper.le(MmrcAssessment::getGrade, gradeMax);
        }

        wrapper.orderByDesc(MmrcAssessment::getAssessmentDate);
        return this.page(page, wrapper);
    }

    public IPage<MmrcAssessment> getPatientHistory(Long patientId, Integer pageNum, Integer pageSize) {
        return getPatientHistory(patientId, pageNum, pageSize, null, null, null, null);
    }

    /**
     * 获取患者最新的mMRC评估
     */
    public MmrcAssessment getLatestAssessment(Long patientId) {
        LambdaQueryWrapper<MmrcAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MmrcAssessment::getPatientId, patientId);
        wrapper.orderByDesc(MmrcAssessment::getAssessmentDate);
        wrapper.orderByDesc(MmrcAssessment::getId);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 获取指定时间点之前的上一条 mMRC 记录
     */
    public MmrcAssessment getPreviousAssessmentBefore(Long patientId, Date beforeDate) {
        LambdaQueryWrapper<MmrcAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MmrcAssessment::getPatientId, patientId);
        if (beforeDate != null) {
            wrapper.lt(MmrcAssessment::getAssessmentDate, beforeDate);
        }
        wrapper.orderByDesc(MmrcAssessment::getAssessmentDate);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    /**
     * 获取患者全部 mMRC 记录（按评估时间升序），用于计算 ABE 分组历史
     */
    public List<MmrcAssessment> listAllByPatientId(Long patientId) {
        LambdaQueryWrapper<MmrcAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MmrcAssessment::getPatientId, patientId);
        wrapper.orderByAsc(MmrcAssessment::getAssessmentDate);
        return this.list(wrapper);
    }

    /**
     * 获取mMRC等级描述
     */
    public String getGradeDescription(int grade) {
        switch (grade) {
            case 0: return "只有在剧烈活动时才感到呼吸困难";
            case 1: return "在平地快步行走或步行爬小坡时出现气短";
            case 2: return "由于气短，平地行走时比同龄人慢或者需要停下来休息";
            case 3: return "在平地行走约100m或数分钟后需要停下来喘气";
            case 4: return "因为严重呼吸困难而不能离开家，或在穿脱衣服时出现呼吸困难";
            default: return "未知";
        }
    }
}
