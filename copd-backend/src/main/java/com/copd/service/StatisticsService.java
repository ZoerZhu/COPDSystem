package com.copd.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.copd.entity.CatAssessment;
import com.copd.entity.Dyspnoea12Assessment;
import com.copd.entity.MmrcAssessment;
import com.copd.entity.PatientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private CatAssessmentService catAssessmentService;

    @Autowired
    private MmrcAssessmentService mmrcAssessmentService;

    @Autowired
    private Dyspnoea12AssessmentService dyspnoea12AssessmentService;

    private List<PatientInfo> filterPatients(List<PatientInfo> patients, String goldGroup, Date startDate, Date endDate) {
        if ((goldGroup == null || goldGroup.isBlank()) && startDate == null && endDate == null) {
            return patients;
        }

        return patients.stream()
            .filter(p -> {
                if (goldGroup != null && !goldGroup.isBlank()) {
                    String g = calculateGoldGroup(p);
                    if (!goldGroup.equalsIgnoreCase(g)) return false;
                }
                if (startDate != null || endDate != null) {
                    Date d = p.getLastAssessmentDate();
                    if (d == null) return false;
                    if (startDate != null && d.before(startDate)) return false;
                    if (endDate != null && d.after(endDate)) return false;
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    private String toSeason(Date date) {
        if (date == null) return "未知";
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int m = cal.get(Calendar.MONTH) + 1; // 1-12
        if (m == 12 || m == 1 || m == 2) return "冬";
        if (m >= 3 && m <= 5) return "春";
        if (m >= 6 && m <= 8) return "夏";
        return "秋";
    }

    /**
     * 获取GOLD分组分布统计
     */
    public Map<String, Object> getGoldGroupDistribution() {
        return getGoldGroupDistribution(null, null, null);
    }

    public Map<String, Object> getGoldGroupDistribution(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        Map<String, Long> groupCount = new HashMap<>();
        groupCount.put("A", 0L);
        groupCount.put("B", 0L);
        groupCount.put("E", 0L);
        groupCount.put("未知", 0L);

        for (PatientInfo patient : allPatients) {
            String computedGoldGroup = calculateGoldGroup(patient);
            groupCount.put(computedGoldGroup, groupCount.getOrDefault(computedGoldGroup, 0L) + 1);
        }

        long total = allPatients.size();
        Map<String, Double> groupPercentage = new HashMap<>();
        for (String group : groupCount.keySet()) {
            groupPercentage.put(group, total > 0 ? (groupCount.get(group) * 100.0 / total) : 0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("groupCount", groupCount);
        result.put("groupPercentage", groupPercentage);

        return result;
    }

    /**
     * 获取年龄分布统计
     */
    public Map<String, Object> getAgeDistribution() {
        return getAgeDistribution(null, null, null);
    }

    public Map<String, Object> getAgeDistribution(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        Map<String, Long> ageGroupCount = new LinkedHashMap<>();
        ageGroupCount.put("<50", 0L);
        ageGroupCount.put("50-59", 0L);
        ageGroupCount.put("60-69", 0L);
        ageGroupCount.put("70-79", 0L);
        ageGroupCount.put(">=80", 0L);
        ageGroupCount.put("未知", 0L);

        double totalAge = 0;
        int ageCount = 0;

        for (PatientInfo patient : allPatients) {
            if (patient.getAge() != null) {
                totalAge += patient.getAge();
                ageCount++;

                if (patient.getAge() < 50) {
                    ageGroupCount.put("<50", ageGroupCount.get("<50") + 1);
                } else if (patient.getAge() < 60) {
                    ageGroupCount.put("50-59", ageGroupCount.get("50-59") + 1);
                } else if (patient.getAge() < 70) {
                    ageGroupCount.put("60-69", ageGroupCount.get("60-69") + 1);
                } else if (patient.getAge() < 80) {
                    ageGroupCount.put("70-79", ageGroupCount.get("70-79") + 1);
                } else {
                    ageGroupCount.put(">=80", ageGroupCount.get(">=80") + 1);
                }
            } else {
                ageGroupCount.put("未知", ageGroupCount.get("未知") + 1);
            }
        }

        Map<String, Map<String, Long>> ageGroupGoldDistribution = new LinkedHashMap<>();
        for (String ageGroup : ageGroupCount.keySet()) {
            ageGroupGoldDistribution.put(ageGroup, new HashMap<>());
            ageGroupGoldDistribution.get(ageGroup).put("A", 0L);
            ageGroupGoldDistribution.get(ageGroup).put("B", 0L);
            ageGroupGoldDistribution.get(ageGroup).put("E", 0L);
        }

        for (PatientInfo patient : allPatients) {
            String computedGoldGroup = calculateGoldGroup(patient);
            String ageGroup = getAgeGroup(patient.getAge());
            if (ageGroupGoldDistribution.containsKey(ageGroup)) {
                ageGroupGoldDistribution.get(ageGroup).put(computedGoldGroup,
                    ageGroupGoldDistribution.get(ageGroup).getOrDefault(computedGoldGroup, 0L) + 1);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ageGroupCount", ageGroupCount);
        result.put("averageAge", ageCount > 0 ? Math.round(totalAge * 10.0 / ageCount) / 10.0 : 0);
        result.put("ageGroupGoldDistribution", ageGroupGoldDistribution);

        return result;
    }

    /**
     * 获取性别分布统计
     */
    public Map<String, Object> getGenderDistribution() {
        return getGenderDistribution(null, null, null);
    }

    public Map<String, Object> getGenderDistribution(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        Map<String, Long> genderCount = new HashMap<>();
        genderCount.put("男", 0L);
        genderCount.put("女", 0L);
        genderCount.put("未知", 0L);

        Map<String, Map<String, Long>> genderGoldDistribution = new HashMap<>();
        genderGoldDistribution.put("男", new HashMap<>());
        genderGoldDistribution.put("女", new HashMap<>());
        genderGoldDistribution.put("未知", new HashMap<>());

        for (String g : Arrays.asList("A", "B", "E")) {
            genderGoldDistribution.get("男").put(g, 0L);
            genderGoldDistribution.get("女").put(g, 0L);
            genderGoldDistribution.get("未知").put(g, 0L);
        }

        for (PatientInfo patient : allPatients) {
            String gender = patient.getGender() != null ? patient.getGender() : "未知";
            if (!genderCount.containsKey(gender)) {
                gender = "未知";
            }
            genderCount.put(gender, genderCount.getOrDefault(gender, 0L) + 1);

            String computedGoldGroup = calculateGoldGroup(patient);
            genderGoldDistribution.get(gender).put(computedGoldGroup,
                genderGoldDistribution.get(gender).getOrDefault(computedGoldGroup, 0L) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("genderCount", genderCount);
        result.put("genderGoldDistribution", genderGoldDistribution);

        return result;
    }

    /**
     * 获取地区分布统计
     */
    public Map<String, Object> getRegionDistribution() {
        return getRegionDistribution(null, null, null);
    }

    public Map<String, Object> getRegionDistribution(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        Map<String, Long> regionCount = new HashMap<>();
        Map<String, Map<String, Long>> regionGoldDistribution = new HashMap<>();

        for (PatientInfo patient : allPatients) {
            String region = patient.getProvince() != null ? patient.getProvince() : "未知";
            if (patient.getCity() != null) {
                region += "-" + patient.getCity();
            }
            regionCount.put(region, regionCount.getOrDefault(region, 0L) + 1);

            if (!regionGoldDistribution.containsKey(region)) {
                regionGoldDistribution.put(region, new HashMap<>());
                regionGoldDistribution.get(region).put("A", 0L);
                regionGoldDistribution.get(region).put("B", 0L);
                regionGoldDistribution.get(region).put("E", 0L);
            }

            String computedGoldGroup = calculateGoldGroup(patient);
            regionGoldDistribution.get(region).put(computedGoldGroup,
                regionGoldDistribution.get(region).getOrDefault(computedGoldGroup, 0L) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("regionCount", regionCount);
        result.put("regionGoldDistribution", regionGoldDistribution);

        return result;
    }

    /**
     * 获取吸烟状况分布
     */
    public Map<String, Object> getSmokingStatusDistribution() {
        return getSmokingStatusDistribution(null, null, null);
    }

    public Map<String, Object> getSmokingStatusDistribution(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        Map<String, Long> smokingCount = new HashMap<>();
        smokingCount.put("CURRENT", 0L);
        smokingCount.put("FORMER", 0L);
        smokingCount.put("NEVER", 0L);
        smokingCount.put("UNKNOWN", 0L);

        Map<String, Map<String, Long>> smokingGoldDistribution = new HashMap<>();
        for (String status : Arrays.asList("CURRENT", "FORMER", "NEVER", "UNKNOWN")) {
            smokingGoldDistribution.put(status, new HashMap<>());
            smokingGoldDistribution.get(status).put("A", 0L);
            smokingGoldDistribution.get(status).put("B", 0L);
            smokingGoldDistribution.get(status).put("E", 0L);
        }

        for (PatientInfo patient : allPatients) {
            String smokingStatus = patient.getSmokingStatus();
            if (smokingStatus == null) {
                smokingStatus = "UNKNOWN";
            }
            // 统一转为大写匹配
            smokingStatus = smokingStatus.toUpperCase();
            if (!smokingCount.containsKey(smokingStatus)) {
                smokingStatus = "UNKNOWN";
            }
            smokingCount.put(smokingStatus, smokingCount.getOrDefault(smokingStatus, 0L) + 1);

            String computedGoldGroup = calculateGoldGroup(patient);
            if (smokingGoldDistribution.containsKey(smokingStatus)) {
                smokingGoldDistribution.get(smokingStatus).put(computedGoldGroup,
                    smokingGoldDistribution.get(smokingStatus).getOrDefault(computedGoldGroup, 0L) + 1);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("smokingCount", smokingCount);
        result.put("smokingGoldDistribution", smokingGoldDistribution);

        return result;
    }

    /**
     * 获取Dyspnoea-12等级分布
     */
    public Map<String, Object> getDyspnoea12Distribution() {
        return getDyspnoea12Distribution(null, null, null);
    }

    public Map<String, Object> getDyspnoea12Distribution(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        Map<String, Long> severityCount = new LinkedHashMap<>();
        severityCount.put("轻度", 0L);
        severityCount.put("轻中度", 0L);
        severityCount.put("中等", 0L);
        severityCount.put("中重度", 0L);
        severityCount.put("重度", 0L);
        severityCount.put("无评估", 0L);

        Map<String, Map<String, Long>> severityGoldDistribution = new LinkedHashMap<>();
        for (String severity : severityCount.keySet()) {
            severityGoldDistribution.put(severity, new HashMap<>());
            severityGoldDistribution.get(severity).put("A", 0L);
            severityGoldDistribution.get(severity).put("B", 0L);
            severityGoldDistribution.get(severity).put("E", 0L);
        }

        for (PatientInfo patient : allPatients) {
            Dyspnoea12Assessment latest = dyspnoea12AssessmentService.getLatestAssessment(patient.getId());
            String severity = "无评估";

            if (latest != null) {
                int score = latest.getTotalScore();
                if (score <= 7) {
                    severity = "轻度";
                } else if (score <= 14) {
                    severity = "轻中度";
                } else if (score <= 21) {
                    severity = "中等";
                } else if (score <= 28) {
                    severity = "中重度";
                } else {
                    severity = "重度";
                }
            }

            severityCount.put(severity, severityCount.getOrDefault(severity, 0L) + 1);

            String computedGoldGroup = calculateGoldGroup(patient);
            severityGoldDistribution.get(severity).put(computedGoldGroup,
                severityGoldDistribution.get(severity).getOrDefault(computedGoldGroup, 0L) + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("severityCount", severityCount);
        result.put("severityGoldDistribution", severityGoldDistribution);

        return result;
    }

    /**
     * 获取急性加重史统计
     */
    public Map<String, Object> getAcuteExacerbationStatistics() {
        return getAcuteExacerbationStatistics(null, null, null);
    }

    public Map<String, Object> getAcuteExacerbationStatistics(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        int totalExacerbations = 0;
        int totalHospitalizations = 0;
        int patientsWithExacerbation = 0;
        int patientsWithHospitalization = 0;

        Map<String, Long> exacerbationGroupCount = new HashMap<>();
        exacerbationGroupCount.put("0次", 0L);
        exacerbationGroupCount.put("1次", 0L);
        exacerbationGroupCount.put("2次", 0L);
        exacerbationGroupCount.put(">=3次", 0L);

        Map<String, Long> exacerbationSeasonCount = new LinkedHashMap<>();
        exacerbationSeasonCount.put("春", 0L);
        exacerbationSeasonCount.put("夏", 0L);
        exacerbationSeasonCount.put("秋", 0L);
        exacerbationSeasonCount.put("冬", 0L);
        exacerbationSeasonCount.put("未知", 0L);

        Map<String, Long> hospitalizationSeasonCount = new LinkedHashMap<>();
        hospitalizationSeasonCount.put("春", 0L);
        hospitalizationSeasonCount.put("夏", 0L);
        hospitalizationSeasonCount.put("秋", 0L);
        hospitalizationSeasonCount.put("冬", 0L);
        hospitalizationSeasonCount.put("未知", 0L);

        for (PatientInfo patient : allPatients) {
            int exacerbations = patient.getYearlyAcuteExacerbations() != null ? patient.getYearlyAcuteExacerbations() : 0;
            int hospitalizations = patient.getYearlyHospitalizations() != null ? patient.getYearlyHospitalizations() : 0;

            totalExacerbations += exacerbations;
            totalHospitalizations += hospitalizations;

            if (exacerbations > 0) {
                patientsWithExacerbation++;
                String season = toSeason(patient.getLastAssessmentDate());
                exacerbationSeasonCount.put(season, exacerbationSeasonCount.getOrDefault(season, 0L) + 1);
            }
            if (hospitalizations > 0) {
                patientsWithHospitalization++;
                String season = toSeason(patient.getLastAssessmentDate());
                hospitalizationSeasonCount.put(season, hospitalizationSeasonCount.getOrDefault(season, 0L) + 1);
            }

            if (exacerbations == 0) {
                exacerbationGroupCount.put("0次", exacerbationGroupCount.get("0次") + 1);
            } else if (exacerbations == 1) {
                exacerbationGroupCount.put("1次", exacerbationGroupCount.get("1次") + 1);
            } else if (exacerbations == 2) {
                exacerbationGroupCount.put("2次", exacerbationGroupCount.get("2次") + 1);
            } else {
                exacerbationGroupCount.put(">=3次", exacerbationGroupCount.get(">=3次") + 1);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalExacerbations", totalExacerbations);
        result.put("totalHospitalizations", totalHospitalizations);
        result.put("averageExacerbations", allPatients.size() > 0 ? (double) totalExacerbations / allPatients.size() : 0);
        result.put("averageHospitalizations", allPatients.size() > 0 ? (double) totalHospitalizations / allPatients.size() : 0);
        result.put("patientsWithExacerbation", patientsWithExacerbation);
        result.put("patientsWithHospitalization", patientsWithHospitalization);
        result.put("exacerbationRate", allPatients.size() > 0 ? (double) patientsWithExacerbation / allPatients.size() * 100 : 0);
        result.put("hospitalizationRate", allPatients.size() > 0 ? (double) patientsWithHospitalization / allPatients.size() * 100 : 0);
        result.put("exacerbationGroupCount", exacerbationGroupCount);
        result.put("exacerbationSeasonCount", exacerbationSeasonCount);
        result.put("hospitalizationSeasonCount", hospitalizationSeasonCount);

        return result;
    }

    /**
     * 获取高风险患者列表（E组）
     */
    public List<PatientInfo> getHighRiskPatients() {
        return getHighRiskPatients(null, null, null);
    }

    public List<PatientInfo> getHighRiskPatients(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);

        return allPatients.stream()
            .filter(patient -> "E".equals(calculateGoldGroup(patient)))
            .collect(Collectors.toList());
    }

    /**
     * 获取失访患者列表（超过90天无评估记录）
     */
    public List<PatientInfo> getLostToFollowUpPatients() {
        return getLostToFollowUpPatients(null, null, null);
    }

    public List<PatientInfo> getLostToFollowUpPatients(String goldGroup, Date startDate, Date endDate) {
        List<PatientInfo> allPatients = filterPatients(patientInfoService.list(), goldGroup, startDate, endDate);
        List<PatientInfo> lostPatients = new ArrayList<>();

        long ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);

        for (PatientInfo patient : allPatients) {
            // 检查CAT评估
            CatAssessment latestCat = catAssessmentService.getLatestAssessment(patient.getId());
            MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patient.getId());
            Dyspnoea12Assessment latestDyspnoea = dyspnoea12AssessmentService.getLatestAssessment(patient.getId());

            Date lastAssessment = null;
            if (latestCat != null && latestCat.getAssessmentDate() != null) {
                lastAssessment = latestCat.getAssessmentDate();
            }
            if (latestMmrc != null && latestMmrc.getAssessmentDate() != null) {
                if (lastAssessment == null || latestMmrc.getAssessmentDate().after(lastAssessment)) {
                    lastAssessment = latestMmrc.getAssessmentDate();
                }
            }
            if (latestDyspnoea != null && latestDyspnoea.getAssessmentDate() != null) {
                if (lastAssessment == null || latestDyspnoea.getAssessmentDate().after(lastAssessment)) {
                    lastAssessment = latestDyspnoea.getAssessmentDate();
                }
            }

            if (lastAssessment == null || lastAssessment.getTime() < ninetyDaysAgo) {
                lostPatients.add(patient);
            }
        }

        return lostPatients;
    }

    /**
     * 综合统计Dashboard数据
     */
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("goldDistribution", getGoldGroupDistribution());
        dashboard.put("ageDistribution", getAgeDistribution());
        dashboard.put("genderDistribution", getGenderDistribution());
        dashboard.put("dyspnoea12Distribution", getDyspnoea12Distribution());
        dashboard.put("acuteExacerbationStatistics", getAcuteExacerbationStatistics());
        dashboard.put("recentAssessmentTrend", getRecentAssessmentTrend(6));

        List<PatientInfo> highRiskPatients = getHighRiskPatients();
        dashboard.put("highRiskPatientCount", highRiskPatients.size());

        List<PatientInfo> lostPatients = getLostToFollowUpPatients();
        dashboard.put("lostToFollowUpPatientCount", lostPatients.size());

        return dashboard;
    }

    private Map<String, Object> getRecentAssessmentTrend(int months) {
        int m = Math.max(1, Math.min(months, 24));

        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // 从最早月份到当前月份
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -(m - 1));

        for (int i = 0; i < m; i++) {
            Date start = cal.getTime();
            Calendar endCal = (Calendar) cal.clone();
            endCal.add(Calendar.MONTH, 1);
            Date end = endCal.getTime();

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            labels.add(String.format("%04d-%02d", year, month));

            long catCount = catAssessmentService.count(new LambdaQueryWrapper<CatAssessment>()
                .ge(CatAssessment::getAssessmentDate, start)
                .lt(CatAssessment::getAssessmentDate, end));
            long mmrcCount = mmrcAssessmentService.count(new LambdaQueryWrapper<MmrcAssessment>()
                .ge(MmrcAssessment::getAssessmentDate, start)
                .lt(MmrcAssessment::getAssessmentDate, end));
            long dyspCount = dyspnoea12AssessmentService.count(new LambdaQueryWrapper<Dyspnoea12Assessment>()
                .ge(Dyspnoea12Assessment::getAssessmentDate, start)
                .lt(Dyspnoea12Assessment::getAssessmentDate, end));

            counts.add(catCount + mmrcCount + dyspCount);
            cal.add(Calendar.MONTH, 1);
        }

        Map<String, Object> trend = new HashMap<>();
        trend.put("labels", labels);
        trend.put("counts", counts);
        return trend;
    }

    /**
     * 计算患者GOLD分组
     */
    private String calculateGoldGroup(PatientInfo patient) {
        int acuteExacerbations = patient.getYearlyAcuteExacerbations() != null ? patient.getYearlyAcuteExacerbations() : 0;
        int hospitalizations = patient.getYearlyHospitalizations() != null ? patient.getYearlyHospitalizations() : 0;

        // E组判断
        if (acuteExacerbations >= 2 || hospitalizations >= 1) {
            return "E";
        }

        // 获取最新评估
        CatAssessment latestCat = catAssessmentService.getLatestAssessment(patient.getId());
        MmrcAssessment latestMmrc = mmrcAssessmentService.getLatestAssessment(patient.getId());

        int catScore = latestCat != null ? latestCat.getTotalScore() : 0;
        int mmrcGrade = latestMmrc != null ? latestMmrc.getGrade() : 0;

        // A/B组判断
        if (catScore < 10 && mmrcGrade <= 1) {
            return "A";
        } else {
            return "B";
        }
    }

    /**
     * 获取年龄分组
     */
    private String getAgeGroup(Integer age) {
        if (age == null) return "未知";
        if (age < 50) return "<50";
        if (age < 60) return "50-59";
        if (age < 70) return "60-69";
        if (age < 80) return "70-79";
        return ">=80";
    }
}
