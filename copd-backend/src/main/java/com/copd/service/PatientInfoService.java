package com.copd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.copd.entity.PatientInfo;
import com.copd.mapper.PatientInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class PatientInfoService extends ServiceImpl<PatientInfoMapper, PatientInfo> {

    /**
     * 患者新增自己的信息
     */
    public boolean addPatientInfo(PatientInfo patientInfo, Long userId) {
        patientInfo.setUserId(userId);
        patientInfo.setGoldGroup("A");
        return this.save(patientInfo);
    }

    /**
     * 获取患者自己的信息
     */
    public PatientInfo getMyPatientInfo(Long userId) {
        LambdaQueryWrapper<PatientInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientInfo::getUserId, userId);
        // 可能存在历史脏数据导致同一 userId 多条记录；此处不抛异常，取第一条
        return this.getOne(wrapper, false);
    }

    /**
     * 管理员获取所有患者列表（分页）
     */
    public IPage<PatientInfo> getPatientList(Integer pageNum, Integer pageSize, String name, String goldGroup) {
        Page<PatientInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PatientInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(PatientInfo::getName, name);
        }
        if (StringUtils.hasText(goldGroup)) {
            wrapper.eq(PatientInfo::getGoldGroup, goldGroup);
        }
        wrapper.orderByDesc(PatientInfo::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 根据ID获取患者信息
     */
    public PatientInfo getPatientById(Long id) {
        return this.getById(id);
    }

    /**
     * 更新患者信息
     */
    public boolean updatePatientInfo(PatientInfo patientInfo) {
        return this.updateById(patientInfo);
    }

    /**
     * 删除患者信息（管理员）
     */
    public boolean deletePatient(Long id) {
        return this.removeById(id);
    }

    /**
     * 根据用户ID删除患者信息
     */
    public boolean deleteByUserId(Long userId) {
        LambdaQueryWrapper<PatientInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientInfo::getUserId, userId);
        return this.remove(wrapper);
    }

    /**
     * 获取患者数量统计
     */
    public Long getPatientCount() {
        return this.count();
    }

    /**
     * 获取各GOLD分组统计
     */
    public List<String> getGoldGroupStatistics() {
        return null;
    }
}
