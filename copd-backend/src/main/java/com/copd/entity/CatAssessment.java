package com.copd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("cat_assessment")
public class CatAssessment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Integer question1Cough;

    private Integer question2Sputum;

    private Integer question3Chest;

    private Integer question4Breath;

    private Integer question5Activity;

    private Integer question6Confidence;

    private Integer question7Sleep;

    private Integer question8Energy;

    private Integer totalScore;

    private Date assessmentDate;

    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
