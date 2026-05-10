package com.copd.service;

import com.copd.entity.Dyspnoea12Assessment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResultFeedbackService {

    /**
     * 获取GOLD/ABE分组的结果反馈
     */
    public Map<String, Object> getGoldGroupFeedback(String goldGroup, Integer catScore, Integer mmrcGrade, String previousGoldGroup) {
        Map<String, Object> feedback = new HashMap<>();

        switch (goldGroup) {
            case "A":
                feedback.put("groupName", "A组");
                feedback.put("riskLevel", "低症状 + 低加重风险");
                feedback.put("prognosis", "疾病进展缓慢，预后相对良好");
                feedback.put("patientAdvice", "您现在病情相对稳定，症状轻，急性加重少。但如果有感冒、咳嗽加重、痰量增多、呼吸困难明显加重，应及时就医。建议您常规随访一年1-2次，自我检测频率每周1-2次，当您的CAT评分持续上升5分以上，或mMRC升级时，建议您近期寻找时间即使就医。");
                feedback.put("treatmentGoal", "主要是在症状出现时快速缓解，提高日常活动自由度。");
                feedback.put("recommendedPlan", "1.按需使用短效支气管扩张剂：\n"
                        + "SABA（如沙丁胺醇）或 SAMA（如异丙托溴铵），或二者复方制剂。\n"
                        + "这是最基础、经济的选择，适用于症状偶发的患者。\n"
                        + "2.或考虑一种长效支气管扩张剂：\n"
                        + "如果患者虽症状总分低，但仍有每日的、持续的背景性呼吸困难，可起始一种 LABA（如福莫特罗、茚达特罗）或 LAMA（如噻托溴铵、格隆溴铵）。");
                feedback.put("clinicalNotes", "如果患者虽症状总分低，但仍有每日的、持续的背景性呼吸困难，可起始一种 LABA（如福莫特罗、茚达特罗）或 LAMA（如噻托溴铵、格隆溴铵）。\n"
                        + "理由：每日一次/两次的规律用药可能比频繁使用短效药更方便，提供更稳定的症状控制。\n"
                        + "临床要点：A组患者不推荐起始联合长效支气管扩张剂或吸入激素，因为过度治疗的副作用风险可能超过获益。");
                feedback.put("visitFrequency", "常规随访（1-2次/年）");
                feedback.put("selfMonitorFrequency", "每周1-2次症状评估");
                feedback.put("visitTriggers", "CAT评分持续上升5分以上，或mMRC升级");
                break;

            case "B":
                feedback.put("groupName", "B组");
                feedback.put("riskLevel", "高症状 + 低加重风险");
                feedback.put("prognosis", "生活质量差，但急性事件少");
                feedback.put("patientAdvice", "虽然您急性加重不多，但日常呼吸困难已经影响生活质量。建议您密切随访每半年一次，自我检测频率每日或隔日一次，直至病情稳定，要以症状管理为主，如果症状突然恶化，呼吸困难加重影响日常生活或在规律用药下呼吸困难持续加重，应及时就医。");
                feedback.put("treatmentGoal", "最大化、持久地缓解症状，改善健康状态和运动耐力。");
                feedback.put("recommendedPlan", "首选：双支气管扩张剂。\n"
                        + "药物：LABA + LAMA（例如：福莫特罗/格隆溴铵、茚达特罗/格隆溴铵、维兰特罗/乌美溴铵等）。");
                feedback.put("clinicalNotes", "药物：LABA + LAMA（例如：福莫特罗/格隆溴铵、茚达特罗/格隆溴铵、维兰特罗/乌美溴铵等）。\n"
                        + "理由：对于有症状的患者，LABA+LAMA在改善肺功能、缓解呼吸困难、提高生活质量方面显著优于单用LAMA或LABA。其机制是协同扩张气道，作用更全面、更持久。\n"
                        + "替代选择：如果因费用、可及性或不耐受无法使用联合制剂，可起始一种LABA或LAMA。但应告知患者，单药治疗可能无法达到最佳症状控制，需密切随访。");
                feedback.put("visitFrequency", "密切随访每半年一次");
                feedback.put("selfMonitorFrequency", "每日或隔日一次");
                feedback.put("visitTriggers", "呼吸困难加重影响日常生活");
                break;

            case "E":
                feedback.put("groupName", "E组");
                feedback.put("riskLevel", "任何症状 + 高加重风险");
                feedback.put("prognosis", "高风险组：未来住院、死亡风险显著增高");
                feedback.put("patientAdvice", "您属于高风险组，未来再次急性加重和住院的风险高。建议您强化随访，每3-6个月一次，自我检测频率每日一次，请注意以下‘红色警报’：1.呼吸困难：静息状态下也感到气短，无法平卧；2.痰液变化：痰量明显增多，颜色变黄/绿，或出现血丝；3.发热：体温超过38℃，尤其是伴有寒战；4.精神状态改变：嗜睡、意识模糊、烦躁不安；5.药物治疗无效：规律使用急救药物（如沙丁胺醇）后症状无改善；6.下肢水肿：新出现的踝部或小腿水肿；7.口唇发绀：嘴唇或指甲床变紫。出现任何一项，应立即就医。");
                feedback.put("treatmentGoal", "首要目标是预防未来急性加重，其次才是控制症状。");
                feedback.put("recommendedPlan", "1.基石治疗：双支气管扩张剂。\n"
                        + "2. 考虑血嗜酸粒细胞计数\n"
                        + "若血嗜酸粒细胞 ≥ 300个/μL：强烈考虑起始LABA+LAMA+ICS三联疗法。\n"
                        + "若血嗜酸粒细胞 < 300个/μL：起始LABA+LAMA双支扩。");
                feedback.put("clinicalNotes", "1.基石治疗：双支气管扩张剂。\n"
                        + "理由：LABA+LAMA本身就是强效的支气管扩张剂，能稳定气道、减轻动态过度充气，其本身就有明确的减少急性加重的作用。这是所有E组患者的治疗基础。\n"
                        + "若血嗜酸粒细胞 ≥ 300个/μL：强烈考虑起始LABA+LAMA+ICS三联疗法。\n"
                        + "理由：高EOS是“2型炎症”的生物标志物，这类患者的气道炎症对ICS反应好。大型研究（如IMPACT,ETHOS）证实，三联疗法在降低中重度急性加重风险方面优于双支扩，且能降低全因死亡率。\n"
                        + "若血嗜酸粒细胞 < 300个/μL：起始LABA+LAMA双支扩。\n"
                        + "理由：此时加入ICS的额外获益有限，但副作用风险（肺炎、真菌感染等）依然存在。应先使用双支扩，若后续仍有加重，再根据新的血EOS水平考虑升级。\n"
                        + "特别提醒：对于E组患者，不推荐起始LABA+ICS（不含LAMA）的双联疗法，因为LABA+LAMA在预防加重方面优于LABA+ICS，且避免了不必要的ICS暴露。");
                feedback.put("visitFrequency", "强化随访（3-6个月/次）");
                feedback.put("selfMonitorFrequency", "每日症状监测");
                feedback.put("redAlerts", new String[]{
                    "呼吸困难：静息状态下也感到气短，无法平卧",
                    "痰液变化：痰量明显增多，颜色变黄/绿，或出现血丝",
                    "发热：体温超过38℃，尤其是伴有寒战",
                    "精神状态改变：嗜睡、意识模糊、烦躁不安",
                    "药物治疗无效：规律使用急救药物（如沙丁胺醇）后症状无改善",
                    "下肢水肿：新出现的踝部或小腿水肿",
                    "口唇发绀：嘴唇或指甲床变紫"
                });
                break;

            default:
                feedback.put("error", "未知的GOLD分组");
        }

        feedback.put("currentCatScore", catScore);
        feedback.put("currentMmrcGrade", mmrcGrade);

        // ABE 分组变化建议与注意（需要有上一轮分组信息）
        if (goldGroup != null && previousGoldGroup != null) {
            feedback.put("abeGroupChangeFeedback", getAbeGroupChangeAdvice(previousGoldGroup, goldGroup));
        }

        return feedback;
    }

    /**
     * 获取 ABE 分组从上一轮到本轮的变化建议与注意
     */
    public Map<String, Object> getAbeGroupChangeAdvice(String previousGroup, String currentGroup) {
        Map<String, Object> result = new HashMap<>();
        // 首次记录时，previousGroup 可能为空；此处按“未变化”兜底，避免前端显示未知
        if (previousGroup == null && currentGroup != null) {
            previousGroup = currentGroup;
        }

        result.put("previousGroup", previousGroup);
        result.put("currentGroup", currentGroup);

        // 基于“严重/减缓/不变”三类策略段落
        String strategyTitle = "ABE分组转换的治疗调整策略";
        result.put("strategyTitle", strategyTitle);

        String strategyParagraph;
        String changeCategory;
        String changeCategoryText;

        boolean severe =
            ("A".equals(previousGroup) && ("B".equals(currentGroup) || "E".equals(currentGroup)))
                || ("B".equals(previousGroup) && "E".equals(currentGroup));

        boolean improved =
            ("B".equals(previousGroup) && "A".equals(currentGroup))
                || ("E".equals(previousGroup) && ("A".equals(currentGroup) || "B".equals(currentGroup)));

        boolean unchanged = previousGroup != null && previousGroup.equals(currentGroup);

        if (severe) {
            changeCategory = "severe";
            changeCategoryText = "病情恶化";
            strategyParagraph = "当病情严重时（a变b，e。b变e）先提醒您的分组变化显示您的病情恶化，需立马就医密切随访，谨慎改变用药。提示：改变药物治疗需经过医生诊断后进行。";
        } else if (improved) {
            changeCategory = "improved";
            changeCategoryText = "病情好转";
            strategyParagraph = "当病情减缓时（b变a，e变a，b）先提醒您的分组变化显示您的病情好转，需密切随访，谨慎调整降级治疗。若降级后出现症状加重或急性加重，需立即恢复原治疗方案。提示：改变药物治疗需经过医生诊断后进行。";
        } else if (unchanged) {
            changeCategory = "unchanged";
            changeCategoryText = "分组未变化";
            strategyParagraph = "当病情不变时，提醒您的分组并未变化，请谨遵医嘱进行防范治疗。";
        } else {
            // 理论上 A/B/E 组合不会落到这里；兜底避免前端报错
            changeCategory = "unknown";
            changeCategoryText = "分组变化未知";
            strategyParagraph = "分组变化信息不完整，请谨遵医嘱随访。";
        }

        result.put("changeCategory", changeCategory);
        result.put("changeCategoryText", changeCategoryText);
        result.put("strategyParagraph", strategyParagraph);

        // 变化建议与注意（按你提供的“以前分组 -> 现在分组”表映射）
        String coreMedicationAdvice = null;
        String keyConsiderations = null;

        if ("A".equals(previousGroup) && "B".equals(currentGroup)) {
            coreMedicationAdvice = "从单支气管扩张剂升级为双支气管扩张剂。";
            keyConsiderations = "主要原因是症状控制不佳（如呼吸困难加重、CAT评分升高）。在升级药物前，务必检查患者的吸入器技术是否正确，并考虑是否存在其他导致呼吸困难的原因（如心血管疾病）。";
        } else if ("A".equals(previousGroup) && "E".equals(currentGroup)) {
            coreMedicationAdvice = "从单支气管扩张剂升级为双支气管扩张剂。";
            keyConsiderations = "主要原因是发生了急性加重。升级到双支扩是所有E组患者的治疗基础。此时需要仔细评估导致首次加重的诱因（如感染），并加强患者教育。";
        } else if ("B".equals(previousGroup) && "A".equals(currentGroup)) {
            coreMedicationAdvice = "可考虑从双支气管扩张剂降级为单支气管扩张剂。";
            keyConsiderations = "这种情况较为少见，通常发生在症状显著改善且长期稳定后。降级决策需非常谨慎，并在密切随访下进行。";
        } else if ("B".equals(previousGroup) && "E".equals(currentGroup)) {
            coreMedicationAdvice = "在双支气管扩张剂基础上，根据血嗜酸粒细胞计数决定是否升级。";
            keyConsiderations =
                "主要原因是发生了急性加重。如果患者正在使用双支扩但仍发生加重，下一步取决于血EOS水平：\n"
                    + "• 血EOS ≥ 100/μL：建议升级为三联疗法（吸入激素+双支扩）。\n"
                    + "• 血EOS < 100/μL：可考虑添加其他非激素类药物，如罗氟司特（适用于FEV1<50%且有慢性支气管炎的患者）或阿奇霉素。";
        } else if ("E".equals(previousGroup) && "A".equals(currentGroup)) {
            coreMedicationAdvice = "可考虑降级治疗。";
            keyConsiderations =
                "这种情况理论上可能发生在经过干预后，患者长时间（一年以上）未再出现急性加重且症状极轻时。但E组是高风险组，降级需极为审慎，通常只考虑将吸入激素降级为双支扩，且需在血EOS水平较低（<300/μL）时进行，因为高EOS患者撤除激素后加重风险很高。";
        } else if ("E".equals(previousGroup) && "B".equals(currentGroup)) {
            coreMedicationAdvice = "在双支气管扩张剂基础上，根据血嗜酸粒细胞计数决定是否降级治疗。";
            keyConsiderations =
                "这种情况理论上可能发生在经过干预后，患者长时间（一年以上）未再出现急性加重且症状极轻时。但E组是高风险组，降级需极为审慎，下一步取决于血EOS水平：\n"
                    + "• 血EOS < 300/μL：可以考虑将三联疗法降级为双支扩。\n"
                    + "• 血EOS ≥ 300/μL：不建议撤除吸入激素，应维持三联疗法。";
        }

        result.put("coreMedicationAdvice", coreMedicationAdvice);
        result.put("keyConsiderations", keyConsiderations);
        return result;
    }

    /**
     * 获取Dyspnoea-12量表的结果反馈
     */
    public Map<String, Object> getDyspnoea12Feedback(Dyspnoea12Assessment current, Dyspnoea12Assessment previous) {
        Map<String, Object> feedback = new HashMap<>();

        int totalScore = current.getTotalScore();
        int physicalScore = current.getPhysicalScore();
        int emotionalScore = current.getEmotionalScore();

        // 严重程度分级（5级）
        String severityLevel;
        String clinicalFeatures;
        if (totalScore <= 7) {
            severityLevel = "轻度";
            clinicalFeatures = "日常活动中无明显呼吸困难，生活质量基本不受影响";
        } else if (totalScore <= 14) {
            severityLevel = "轻中度";
            clinicalFeatures = "剧烈活动时出现呼吸困难，日常活动轻微受限";
        } else if (totalScore <= 21) {
            severityLevel = "中等";
            clinicalFeatures = "中等强度活动即感气短，日常活动明显受限";
        } else if (totalScore <= 28) {
            severityLevel = "中重度";
            clinicalFeatures = "轻度活动即感呼吸困难，需经常休息";
        } else {
            severityLevel = "重度";
            clinicalFeatures = "静息状态下也存在呼吸困难，生活严重受限，需持续吸氧";
        }

        // Dyspnoea-12三分级（用于与GOLD结合）
        String threeLevelSeverity;
        if (totalScore <= 12) {
            threeLevelSeverity = "轻度";
        } else if (totalScore <= 24) {
            threeLevelSeverity = "中度";
        } else {
            threeLevelSeverity = "重度";
        }

        feedback.put("totalScore", totalScore);
        feedback.put("severityLevel", severityLevel);
        feedback.put("clinicalFeatures", clinicalFeatures);
        feedback.put("threeLevelSeverity", threeLevelSeverity);

        // 身体维度
        String physicalDescription;
        if (physicalScore <= 4) {
            physicalDescription = "基本无呼吸不适，仅在极度劳累时出现轻微气短";
        } else if (physicalScore <= 9) {
            physicalDescription = "活动时感觉呼吸费力，但可自行缓解";
        } else if (physicalScore <= 14) {
            physicalDescription = "明显气短感，需放慢活动节奏";
        } else {
            physicalDescription = "严重窒息感，即使轻微活动也难以完成";
        }
        feedback.put("physicalScore", physicalScore);
        feedback.put("physicalDescription", physicalDescription);

        // 情感维度
        String emotionalDescription;
        String psychologicalIssues;
        if (emotionalScore <= 3) {
            emotionalDescription = "情绪平稳，能接受疾病状态";
            psychologicalIssues = "无明显心理问题";
        } else if (emotionalScore <= 7) {
            emotionalDescription = "轻度烦躁，偶尔因呼吸问题感到沮丧";
            psychologicalIssues = "轻微焦虑倾向";
        } else if (emotionalScore <= 11) {
            emotionalDescription = "中度痛苦，常因呼吸困难感到无助";
            psychologicalIssues = "中度焦虑/抑郁，需心理支持";
        } else {
            emotionalDescription = "严重心理困扰，感到窒息般的恐惧";
            psychologicalIssues = "重度焦虑/抑郁，需专科干预";
        }
        feedback.put("emotionalScore", emotionalScore);
        feedback.put("emotionalDescription", emotionalDescription);
        feedback.put("psychologicalIssues", psychologicalIssues);

        // 与历史对比
        if (previous != null) {
            int scoreChange = totalScore - previous.getTotalScore();
            String changeMeaning;
            String intervention;

            if (scoreChange <= -6) {
                changeMeaning = "显著改善";
                intervention = "维持当前治疗方案，鼓励继续康复训练";
            } else if (scoreChange <= -3) {
                changeMeaning = "中度改善";
                intervention = "治疗有效，可考虑强化康复计划";
            } else if (scoreChange <= 2) {
                changeMeaning = "稳定";
                intervention = "无明显变化，维持现有管理";
            } else if (scoreChange <= 5) {
                changeMeaning = "轻度恶化";
                intervention = "需评估原因（感染、用药依从性等），及时干预";
            } else {
                changeMeaning = "显著恶化";
                intervention = "警惕急性加重风险，建议尽快就医";
            }

            feedback.put("scoreChange", scoreChange);
            feedback.put("changeMeaning", changeMeaning);
            feedback.put("intervention", intervention);
            feedback.put("previousScore", previous.getTotalScore());
            feedback.put("previousDate", previous.getAssessmentDate());
        }

        feedback.put("assessmentDate", current.getAssessmentDate());

        return feedback;
    }

    /**
     * 获取综合结果反馈（GOLD + Dyspnoea-12）
     */
    public Map<String, Object> getComprehensiveFeedback(String goldGroup, String dyspnoea12Severity) {
        Map<String, Object> feedback = new HashMap<>();

        String managementAdvice;

        if ("A".equals(goldGroup)) {
            if ("轻度".equals(dyspnoea12Severity)) {
                managementAdvice = "常规管理，预后良好";
            } else if ("中度".equals(dyspnoea12Severity)) {
                managementAdvice = "关注心理因素，排查其他原因";
            } else {
                managementAdvice = "需详细评估，可能误分组";
            }
        } else if ("B".equals(goldGroup)) {
            if ("轻度".equals(dyspnoea12Severity)) {
                managementAdvice = "症状感知有限，关注隐匿进展";
            } else if ("中度".equals(dyspnoea12Severity)) {
                managementAdvice = "典型B组患者，综合干预";
            } else {
                managementAdvice = "强调心理支持和康复";
            }
        } else if ("E".equals(goldGroup)) {
            if ("轻度".equals(dyspnoea12Severity)) {
                managementAdvice = "虽分级高但感知有限，需加强监测";
            } else if ("中度".equals(dyspnoea12Severity)) {
                managementAdvice = "典型E组患者，强化预防";
            } else {
                managementAdvice = "极高风险，多学科管理";
            }
        } else {
            managementAdvice = "分组信息不完整";
        }

        feedback.put("goldGroup", goldGroup);
        feedback.put("dyspnoea12Severity", dyspnoea12Severity);
        feedback.put("managementAdvice", managementAdvice);
        feedback.put("description", "您GOLD分组为" + goldGroup + "组，Dyspnoea-12分级为" + dyspnoea12Severity + "，建议：" + managementAdvice + "。");

        return feedback;
    }
}
