-- ============================================
-- 测试数据插入脚本
-- ============================================

USE copd_system;

-- ============================================
-- 1. 插入系统配置
-- ============================================
INSERT INTO system_config (config_key, config_value, config_desc) VALUES
('system_name', '慢性阻塞性肺疾病分级与评估系统', '系统名称'),
('font_size_default', '16', '默认字体大小'),
('font_size_min', '12', '最小字体大小'),
('font_size_max', '24', '最大字体大小'),
('assessment_reminder_days', '90', '评估提醒间隔天数（90天）'),
('high_risk_threshold', '2', '高风险阈值（年急性加重次数）');

-- ============================================
-- 2. 插入用户数据
--    密码说明：使用MD5加密
--    admin/123456 -> e10adc3949ba59abbe56e057f20f883e
--    test/123456 -> e10adc3949ba59abbe56e057f20f883e
-- ============================================
INSERT INTO sys_user (id, username, password, real_name, role, phone, email, status) VALUES
(3, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', '13800138000', 'admin@copd.com', 1),
(2, 'test', 'e10adc3949ba59abbe56e057f20f883e', '测试患者', 'PATIENT', '13900139000', 'test@copd.com', 1);

-- ============================================
-- 3. 插入患者信息数据
-- ============================================
INSERT INTO patient_info (id, user_id, name, age, gender, birth_date, phone, province, city, smoking_status, cigarettes_per_month, total_acute_exacerbations, total_hospitalizations, yearly_acute_exacerbations, yearly_hospitalizations, fev1_pred, fev1_fvc, gold_group, last_assessment_date) VALUES
-- 患者1：张三 - A组（低症状、低风险）
(1, 2, '张三', 65, '男', '1961-03-15', '13900139001', '北京市', '北京市', 'FORMER', 30, 1, 0, 0, 0, 78.5, 68.2, 'A', '2026-02-20'),
-- 患者2：李四 - B组（高症状、低风险）
(2, 2, '李四', 72, '男', '1954-08-22', '13900139002', '上海市', '上海市', 'CURRENT', 40, 3, 1, 1, 0, 65.3, 62.1, 'B', '2026-03-01'),
-- 患者3：王五 - E组（高风险）
(3, 2, '王五', 68, '女', '1958-11-10', '13900139003', '广东省', '广州市', 'FORMER', 20, 8, 4, 3, 2, 45.2, 55.8, 'E', '2026-01-15'),
-- 患者4：赵六 - B组（高症状）
(4, 2, '赵六', 55, '女', '1971-05-18', '13900139004', '四川省', '成都市', 'NEVER', 0, 0, 0, 0, 0, 82.1, 75.3, 'B', '2026-02-28'),
-- 患者5：孙七 - A组（低症状）
(5, 2, '孙七', 80, '男', '1946-12-01', '13900139005', '浙江省', '杭州市', 'FORMER', 15, 2, 0, 0, 0, 71.8, 66.5, 'A', '2026-03-10');

-- ============================================
-- 4. 插入CAT量表评估数据
-- ============================================
INSERT INTO cat_assessment (patient_id, question1_cough, question2_sputum, question3_chest, question4_breath, question5_activity, question6_confidence, question7_sleep, question8_energy, total_score, assessment_date) VALUES
-- 患者1张三 - A组
(1, 1, 2, 1, 2, 1, 1, 2, 1, 11, '2026-02-20'),
-- 患者2李四 - B组
(2, 3, 3, 4, 4, 3, 3, 4, 3, 27, '2026-03-01'),
-- 患者3王五 - E组
(3, 5, 5, 5, 5, 5, 5, 4, 5, 39, '2026-01-15'),
-- 患者4赵六 - B组
(4, 2, 2, 3, 4, 3, 2, 3, 3, 22, '2026-02-28'),
-- 患者5孙七 - A组
(5, 1, 1, 2, 1, 2, 1, 1, 2, 11, '2026-03-10');

-- ============================================
-- 5. 插入mMRC量表评估数据
-- ============================================
INSERT INTO mmrc_assessment (patient_id, grade, description, assessment_date) VALUES
-- 患者1张三 - 0级
(1, 0, '仅在剧烈活动时出现呼吸困难', '2026-02-20'),
-- 患者2李四 - 2级
(2, 2, '因呼吸困难而平地行走缓慢，或中途需要停下休息', '2026-03-01'),
-- 患者3王五 - 3级
(3, 3, '平地行走100米或数分钟后需要停下休息', '2026-01-15'),
-- 患者4赵六 - 2级
(4, 2, '因呼吸困难而平地行走缓慢，或中途需要停下休息', '2026-02-28'),
-- 患者5孙七 - 1级
(5, 1, '平地快速行走或上小山坡时出现气短', '2026-03-10');

-- ============================================
-- 6. 插入Dyspnoea-12量表评估数据
-- ============================================
INSERT INTO dyspnoea12_assessment (
    patient_id,
    q1_breathless_sudden, q2_breathless_heavy, q3_breathless_exhausted, q4_breathless_starve, q5_chest_tightness, q6_breath_heavy, q7_breath_rapid,
    q8_frustrated, q9_fear, q10_distress, q11_worry, q12_depressed,
    physical_score, emotional_score, total_score, severity_level, assessment_date
) VALUES
-- 患者1张三 - 轻度
(1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 3, 1, 4, '轻度', '2026-02-20'),
-- 患者2李四 - 中度
(2, 2, 2, 1, 0, 2, 2, 1, 1, 1, 2, 1, 1, 10, 6, 16, '中度', '2026-03-01'),
-- 患者3王五 - 重度
(3, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 2, 2, 20, 12, 32, '重度', '2026-01-15'),
-- 患者4赵六 - 轻中度
(4, 1, 2, 1, 0, 1, 2, 1, 1, 1, 1, 1, 0, 8, 4, 12, '轻中度', '2026-02-28'),
-- 患者5孙七 - 轻度
(5, 0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 3, 0, 3, '轻度', '2026-03-10');

-- ============================================
-- 7. 插入综合评估结果数据
-- ============================================
INSERT INTO comprehensive_assessment (
    patient_id, assessment_date, cat_score, mmrc_grade, dyspnoea12_total, dyspnoea12_severity,
    gold_group, risk_level, symptom_level, treatment_recommendation, score_change, clinical_interpretation
) VALUES
-- 患者1张三 - A组
(1, '2026-02-20', 11, 0, 4, '轻度', 'A', '低风险', '低症状', '按需使用短效支气管扩张剂或一种长效支气管扩张剂，常规随访（一年1-2次）', 0, '基线评估'),
-- 患者2李四 - B组
(2, '2026-03-01', 27, 2, 16, '中度', 'B', '低风险', '高症状', '首选治疗为双支气管扩张剂（LABA + LAMA），密切随访（每半年一次）', 0, '基线评估'),
-- 患者3王五 - E组
(3, '2026-01-15', 39, 3, 32, '重度', 'E', '高风险', '高症状', '基石治疗为双支气管扩张剂，若血嗜酸粒细胞 >= 300个/μL 强烈建议三联疗法。密切关注7大红色警报症状', 0, '基线评估'),
-- 患者4赵六 - B组
(4, '2026-02-28', 22, 2, 12, '轻中度', 'B', '低风险', '高症状', '首选治疗为双支气管扩张剂（LABA + LAMA），密切随访（每半年一次）', 0, '基线评估'),
-- 患者5孙七 - A组
(5, '2026-03-10', 11, 1, 3, '轻度', 'A', '低风险', '低症状', '按需使用短效支气管扩张剂或一种长效支气管扩张剂，常规随访（一年1-2次）', 0, '基线评估');

-- ============================================
-- 8. 插入登录日志数据
-- ============================================
INSERT INTO login_log (user_id, username, login_time, ip_address, login_status) VALUES
(1, 'admin', '2026-03-15 08:30:00', '127.0.0.1', 'SUCCESS'),
(2, 'test', '2026-03-15 09:00:00', '127.0.0.1', 'SUCCESS'),
(2, 'test', '2026-03-10 14:20:00', '127.0.0.1', 'SUCCESS'),
(2, 'test', '2026-03-05 10:15:00', '127.0.0.1', 'SUCCESS');

-- ============================================
-- 9. 验证数据
-- ============================================
SELECT '用户表数据验证:' AS '';
SELECT id, username, real_name, role, status FROM sys_user;

SELECT '患者信息表数据验证:' AS '';
SELECT id, user_id, name, age, gender, province, gold_group FROM patient_info;

SELECT 'CAT量表评估数据验证:' AS '';
SELECT patient_id, total_score, assessment_date FROM cat_assessment;

SELECT 'mMRC量表评估数据验证:' AS '';
SELECT patient_id, grade, assessment_date FROM mmrc_assessment;

SELECT 'Dyspnoea-12量表评估数据验证:' AS '';
SELECT patient_id, total_score, severity_level, assessment_date FROM dyspnoea12_assessment;

SELECT '综合评估结果数据验证:' AS '';
SELECT patient_id, gold_group, risk_level, symptom_level, assessment_date FROM comprehensive_assessment;

SELECT '数据插入完成！' AS '';
