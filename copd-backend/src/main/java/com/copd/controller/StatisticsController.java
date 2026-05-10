package com.copd.controller;

import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.entity.PatientInfo;
import com.copd.service.PatientInfoService;
import com.copd.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private PatientInfoService patientInfoService;

    /**
     * 获取Dashboard统计数据
     */
    @GetMapping("/dashboard")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = statisticsService.getDashboardStatistics();
        return Result.success(dashboard);
    }

    /**
     * 获取GOLD分组分布
     */
    @GetMapping("/gold-distribution")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getGoldDistribution() {
        return Result.success(statisticsService.getGoldGroupDistribution());
    }

    /**
     * 获取年龄分布统计
     */
    @GetMapping("/age-distribution")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getAgeDistribution() {
        return Result.success(statisticsService.getAgeDistribution());
    }

    /**
     * 获取性别分布统计
     */
    @GetMapping("/gender-distribution")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getGenderDistribution() {
        return Result.success(statisticsService.getGenderDistribution());
    }

    /**
     * 获取地区分布统计
     */
    @GetMapping("/region-distribution")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getRegionDistribution() {
        return Result.success(statisticsService.getRegionDistribution());
    }

    /**
     * 获取吸烟状况分布
     */
    @GetMapping("/smoking-distribution")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getSmokingDistribution() {
        return Result.success(statisticsService.getSmokingStatusDistribution());
    }

    /**
     * 获取Dyspnoea-12等级分布
     */
    @GetMapping("/dyspnoea12-distribution")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getDyspnoea12Distribution() {
        return Result.success(statisticsService.getDyspnoea12Distribution());
    }

    /**
     * 获取急性加重史统计
     */
    @GetMapping("/exacerbation-statistics")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getExacerbationStatistics() {
        return Result.success(statisticsService.getAcuteExacerbationStatistics());
    }

    /**
     * 获取高风险患者列表（E组）
     */
    @GetMapping("/high-risk-patients")
    @RequireRole(admin = true)
    public Result<List<PatientInfo>> getHighRiskPatients() {
        return Result.success(statisticsService.getHighRiskPatients());
    }

    /**
     * 获取失访患者列表（超过90天无评估记录）
     */
    @GetMapping("/lost-patients")
    @RequireRole(admin = true)
    public Result<List<PatientInfo>> getLostPatients() {
        return Result.success(statisticsService.getLostToFollowUpPatients());
    }

    /**
     * 获取综合概览数据（Statistics页面使用）
     */
    @GetMapping("/overview")
    @RequireRole(admin = true)
    public Result<Map<String, Object>> getOverview(
            @RequestParam(required = false) String goldGroup,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();

        Date start = null;
        Date end = null;
        if (startDate != null && !startDate.isBlank()) {
            start = parseDate(startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            end = parseDate(endDate);
        }

        // Overview数据
        Map<String, Object> overview = new HashMap<>();
        List<PatientInfo> allPatients = patientInfoService.list();
        overview.put("totalPatients", allPatients.size());

        // 高风险患者数量（E组）
        long highRiskCount = statisticsService.getHighRiskPatients(goldGroup, start, end).size();
        overview.put("highRiskCount", highRiskCount);

        // 失访患者数量（超过90天无评估）
        List<PatientInfo> lostPatients = statisticsService.getLostToFollowUpPatients(goldGroup, start, end);
        overview.put("lostFollowCount", lostPatients.size());

        // GOLD分组统计
        Map<String, Object> goldDistribution = statisticsService.getGoldGroupDistribution(goldGroup, start, end);
        Map<String, Long> groupCount = (Map<String, Long>) goldDistribution.get("groupCount");
        overview.put("groupA", groupCount != null ? groupCount.getOrDefault("A", 0L) : 0L);
        overview.put("groupB", groupCount != null ? groupCount.getOrDefault("B", 0L) : 0L);
        overview.put("groupE", groupCount != null ? groupCount.getOrDefault("E", 0L) : 0L);

        // 年龄分布
        Map<String, Object> ageDistribution = statisticsService.getAgeDistribution(goldGroup, start, end);
        Map<String, Long> ageGroupCount = (Map<String, Long>) ageDistribution.get("ageGroupCount");
        overview.put("avgAge", ageDistribution.get("averageAge"));
        overview.put("ageDistribution", ageGroupCount != null ?
            Arrays.asList(
                ageGroupCount.getOrDefault("<50", 0L),
                ageGroupCount.getOrDefault("50-59", 0L),
                ageGroupCount.getOrDefault("60-69", 0L),
                ageGroupCount.getOrDefault("70-79", 0L),
                ageGroupCount.getOrDefault(">=80", 0L)
            ) : Arrays.asList(0, 0, 0, 0, 0));

        // 性别分布
        Map<String, Object> genderDistribution = statisticsService.getGenderDistribution(goldGroup, start, end);
        Map<String, Long> genderCount = (Map<String, Long>) genderDistribution.get("genderCount");
        overview.put("maleCount", genderCount != null ? genderCount.getOrDefault("男", 0L) : 0L);
        overview.put("femaleCount", genderCount != null ? genderCount.getOrDefault("女", 0L) : 0L);

        // 急性加重统计
        Map<String, Object> exacerbationStats = statisticsService.getAcuteExacerbationStatistics(goldGroup, start, end);
        overview.put("exacerbationRate", exacerbationStats.get("exacerbationRate"));
        overview.put("hospitalizationRate", exacerbationStats.get("hospitalizationRate"));

        result.put("overview", overview);

        // 高风险患者列表
        result.put("highRiskPatients", statisticsService.getHighRiskPatients(goldGroup, start, end));

        // 失访患者列表
        result.put("lostFollowPatients", lostPatients);

        // 图表数据
        Map<String, Object> charts = new HashMap<>();
        charts.put("goldGroup", Map.of(
            "A", groupCount != null ? groupCount.getOrDefault("A", 0L) : 0L,
            "B", groupCount != null ? groupCount.getOrDefault("B", 0L) : 0L,
            "E", groupCount != null ? groupCount.getOrDefault("E", 0L) : 0L
        ));
        charts.put("ageDistribution", overview.get("ageDistribution"));
        charts.put("ageGroupGoldDistribution", ageDistribution.get("ageGroupGoldDistribution"));
        charts.put("gender", Map.of(
            "male", genderCount != null ? genderCount.getOrDefault("男", 0L) : 0L,
            "female", genderCount != null ? genderCount.getOrDefault("女", 0L) : 0L
        ));
        charts.put("genderGoldDistribution", genderDistribution.get("genderGoldDistribution"));

        // 吸烟状况分布
        Map<String, Object> smokingDistribution = statisticsService.getSmokingStatusDistribution(goldGroup, start, end);
        Map<String, Long> smokingCount = (Map<String, Long>) smokingDistribution.get("smokingCount");
        charts.put("smoking", Map.of(
            "never", smokingCount != null ? smokingCount.getOrDefault("NEVER", 0L) : 0L,
            "former", smokingCount != null ? smokingCount.getOrDefault("FORMER", 0L) : 0L,
            "current", smokingCount != null ? smokingCount.getOrDefault("CURRENT", 0L) : 0L
        ));
        charts.put("smokingGoldDistribution", smokingDistribution.get("smokingGoldDistribution"));

        // Dyspnoea-12分布
        Map<String, Object> dyspnoeaDistribution = statisticsService.getDyspnoea12Distribution(goldGroup, start, end);
        charts.put("dyspnoea", dyspnoeaDistribution);

        // 地区分布（以及与 A/B/E 关系）
        Map<String, Object> regionDistribution = statisticsService.getRegionDistribution(goldGroup, start, end);
        charts.put("region", regionDistribution);

        // 急性加重/住院统计（包含季节分布）
        charts.put("exacerbation", exacerbationStats);

        result.put("charts", charts);

        return Result.success(result);
    }

    private Date parseDate(String s) {
        // 兼容 yyyy-MM-dd / yyyy/MM/dd / ISO date-time（取前 10 位）
        String v = s.trim();
        if (v.length() >= 10) v = v.substring(0, 10);
        List<String> patterns = Arrays.asList("yyyy-MM-dd", "yyyy/MM/dd");
        for (String p : patterns) {
            try {
                return new SimpleDateFormat(p).parse(v);
            } catch (ParseException ignored) {}
        }
        return null;
    }
}
