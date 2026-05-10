package com.copd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("patient_info")
public class PatientInfo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private Integer age;

    private String gender;

    private Date birthDate;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String smokingStatus;

    private Integer cigarettesPerMonth;

    private Integer totalAcuteExacerbations;

    private Integer totalHospitalizations;

    private Integer yearlyAcuteExacerbations;

    private Integer yearlyHospitalizations;

    private BigDecimal fev1Pred;

    private BigDecimal fev1Fvc;

    private String goldGroup;

    private Date lastAssessmentDate;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
