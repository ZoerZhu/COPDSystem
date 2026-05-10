package com.copd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("dyspnoea12_assessment")
public class Dyspnoea12Assessment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Integer q1BreathlessSudden;
    private Integer q2BreathlessHeavy;
    private Integer q3BreathlessExhausted;
    private Integer q4BreathlessStarve;
    private Integer q5ChestTightness;
    private Integer q6BreathHeavy;
    private Integer q7BreathRapid;

    private Integer q8Frustrated;
    private Integer q9Fear;
    private Integer q10Distress;
    private Integer q11Worry;
    private Integer q12Depressed;

    private Integer physicalScore;
    private Integer emotionalScore;
    private Integer totalScore;

    private String severityLevel;

    private Date assessmentDate;

    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
