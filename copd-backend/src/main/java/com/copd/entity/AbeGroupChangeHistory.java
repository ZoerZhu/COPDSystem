package com.copd.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("abe_group_change_history")
public class AbeGroupChangeHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Long catAssessmentId;

    private Long mmrcAssessmentId;

    // 记录“本次 ABE 计算”的时间点（用于历史排序与筛选）
    private Date evaluationTime;

    private String previousGroup;

    private String currentGroup;

    private String changeCategory;

    private String changeCategoryText;

    private String coreMedicationAdvice;

    private String keyConsiderations;

    private String strategyTitle;

    private String strategyParagraph;

    // MySQL 默认 current_timestamp 兜底
    private Date createTime;
}

