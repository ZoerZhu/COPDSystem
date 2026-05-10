package com.copd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("mmrc_assessment")
public class MmrcAssessment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Integer grade;

    private String description;

    private Date assessmentDate;

    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
