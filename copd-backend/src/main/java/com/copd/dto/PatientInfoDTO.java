package com.copd.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PatientInfoDTO {

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
}
