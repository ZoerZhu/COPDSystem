-- ============================================
-- 慢性阻塞性肺疾病分级与评估系统
-- MySQL 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS copd_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE copd_system;

-- ============================================
-- 1. 用户表 (sys_user)
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'PATIENT' COMMENT '角色：PATIENT-患者，ADMIN-管理员',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 患者信息表 (patient_info)
-- ============================================
DROP TABLE IF EXISTS patient_info;
CREATE TABLE patient_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别：男/女',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '联系方式',
    province VARCHAR(50) COMMENT '居住地（省/直辖市）',
    city VARCHAR(50) COMMENT '居住地（市）',
    district VARCHAR(50) COMMENT '居住地（区/县）',
    smoking_status VARCHAR(20) COMMENT '吸烟状态：NEVER-不吸烟，FORMER-已戒烟，CURRENT-正在吸烟',
    cigarettes_per_month INT COMMENT '每月吸烟包数',
    total_acute_exacerbations INT DEFAULT 0 COMMENT '有史以来急性加重次数',
    total_hospitalizations INT DEFAULT 0 COMMENT '有史以来住院次数',
    yearly_acute_exacerbations INT DEFAULT 0 COMMENT '一年内急性加重次数',
    yearly_hospitalizations INT DEFAULT 0 COMMENT '一年内住院次数',
    fev1_pred DECIMAL(5,2) COMMENT 'FEV1%pred（第一秒用力呼气容积占预计值百分比）',
    fev1_fvc DECIMAL(5,2) COMMENT 'FEV1/FVC（第一秒用力呼气容积/用力肺活量）',
    gold_group VARCHAR(5) COMMENT 'GOLD分组：A/B/E',
    last_assessment_date DATE COMMENT '最后评估日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_gold_group (gold_group),
    INDEX idx_last_assessment (last_assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者信息表';

-- ============================================
-- 3. CAT量表评估记录表 (cat_assessment)
-- ============================================
DROP TABLE IF EXISTS cat_assessment;
CREATE TABLE cat_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    question1_cough INT NOT NULL COMMENT '问题1：咳嗽（0-5分）',
    question2_sputum INT NOT NULL COMMENT '问题2：咳痰（0-5分）',
    question3_chest INT NOT NULL COMMENT '问题3：胸闷（0-5分）',
    question4_breath INT NOT NULL COMMENT '问题4：喘气（0-5分）',
    question5_activity INT NOT NULL COMMENT '问题5：日常活动受限（0-5分）',
    question6_confidence INT NOT NULL COMMENT '问题6：外出信心（0-5分）',
    question7_sleep INT NOT NULL COMMENT '问题7：睡眠（0-5分）',
    question8_energy INT NOT NULL COMMENT '问题8：精力（0-5分）',
    total_score INT NOT NULL COMMENT '总分（0-40分）',
    assessment_date DATE NOT NULL COMMENT '评估日期',
    remarks TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_patient_id (patient_id),
    INDEX idx_assessment_date (assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CAT量表评估记录表';

-- ============================================
-- 4. mMRC量表评估记录表 (mmrc_assessment)
-- ============================================
DROP TABLE IF EXISTS mmrc_assessment;
CREATE TABLE mmrc_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    grade INT NOT NULL COMMENT '呼吸困难分级（0-4级）',
    description VARCHAR(500) COMMENT '分级描述',
    assessment_date DATE NOT NULL COMMENT '评估日期',
    remarks TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_patient_id (patient_id),
    INDEX idx_assessment_date (assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='mMRC量表评估记录表';

-- ============================================
-- 5. Dyspnoea-12量表评估记录表 (dyspnoea12_assessment)
-- ============================================
DROP TABLE IF EXISTS dyspnoea12_assessment;
CREATE TABLE dyspnoea12_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    -- 身体维度（7题，每题0-3分）
    q1_breathless_sudden INT NOT NULL COMMENT '问题1：突然呼吸困难',
    q2_breathless_heavy INT NOT NULL COMMENT '问题2：呼吸困难但尚可忍受',
    q3_breathless_exhausted INT NOT NULL COMMENT '问题3：呼吸困难到精疲力竭',
    q4_breathless_starve INT NOT NULL COMMENT '问题4：呼吸困难到无法呼吸',
    q5_chest_tightness INT NOT NULL COMMENT '问题5：胸闷',
    q6_breath_heavy INT NOT NULL COMMENT '6：呼吸费力',
    q7_breath_rapid INT NOT NULL COMMENT '7：呼吸急促',
    -- 情感维度（5题，每题0-3分）
    q8_frustrated INT NOT NULL COMMENT '问题8：感到沮丧',
    q9_fear INT NOT NULL COMMENT '问题9：感到害怕',
    q10_distress INT NOT NULL COMMENT '10：感到痛苦',
    q11_worry INT NOT NULL COMMENT '11：感到担心',
    q12_depressed INT NOT NULL COMMENT '12：感到抑郁',
    -- 计算得分
    physical_score INT NOT NULL COMMENT '身体维度总分（0-21分）',
    emotional_score INT NOT NULL COMMENT '情感维度总分（0-15分）',
    total_score INT NOT NULL COMMENT '总分（0-36分）',
    severity_level VARCHAR(20) COMMENT '严重程度：轻度/轻中度/中度/中重度/重度',
    assessment_date DATE NOT NULL COMMENT '评估日期',
    remarks TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_patient_id (patient_id),
    INDEX idx_assessment_date (assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dyspnoea-12量表评估记录表';

-- ============================================
-- 6. 综合评估结果表 (comprehensive_assessment)
-- ============================================
DROP TABLE IF EXISTS comprehensive_assessment;
CREATE TABLE comprehensive_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    assessment_date DATE NOT NULL COMMENT '评估日期',
    -- 各量表得分
    cat_score INT COMMENT 'CAT总分',
    mmrc_grade INT COMMENT 'mMRC分级',
    dyspnoea12_total INT COMMENT 'Dyspnoea-12总分',
    dyspnoea12_severity VARCHAR(20) COMMENT 'Dyspnoea-12严重程度',
    -- GOLD分组
    gold_group VARCHAR(5) NOT NULL COMMENT 'GOLD分组：A/B/E',
    risk_level VARCHAR(20) NOT NULL COMMENT '风险等级：低风险/高风险',
    symptom_level VARCHAR(20) NOT NULL COMMENT '症状水平：低症状/高症状',
    -- 治疗建议
    treatment_recommendation TEXT COMMENT '治疗建议',
    -- 交叉反馈
    cross_feedback TEXT COMMENT '矩阵交叉反馈',
    -- 与历史对比
    score_change INT COMMENT '与上次评估总分变化',
    clinical_interpretation VARCHAR(500) COMMENT '临床解读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_patient_id (patient_id),
    INDEX idx_assessment_date (assessment_date),
    INDEX idx_gold_group (gold_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综合评估结果表';

-- ============================================
-- 6. ABE分组变化历史表 (abe_group_change_history)
-- ============================================
DROP TABLE IF EXISTS abe_group_change_history;
CREATE TABLE abe_group_change_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    cat_assessment_id BIGINT NOT NULL COMMENT 'CAT评估记录ID',
    mmrc_assessment_id BIGINT NOT NULL COMMENT 'mMRC评估记录ID',
    evaluation_time DATETIME NOT NULL COMMENT 'ABE分组计算时间',
    previous_group VARCHAR(5) COMMENT '上一轮分组：A/B/E',
    current_group VARCHAR(5) NOT NULL COMMENT '当前分组：A/B/E',
    change_category VARCHAR(20) COMMENT '变化类别：severe/improved/unchanged/unknown',
    change_category_text VARCHAR(500) COMMENT '变化类别说明',
    core_medication_advice TEXT COMMENT '核心用药变化建议',
    key_considerations TEXT COMMENT '关键考量与注意事项',
    strategy_title VARCHAR(200) COMMENT '策略标题',
    strategy_paragraph TEXT COMMENT '策略说明正文',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_patient_id (patient_id),
    INDEX idx_evaluation_time (evaluation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ABE分组变化历史表';

-- ============================================
-- 7. 系统配置表 (system_config)
-- ============================================
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_desc VARCHAR(255) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================
-- 8. 登录日志表 (login_log)
-- ============================================
DROP TABLE IF EXISTS login_log;
CREATE TABLE login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    logout_time DATETIME COMMENT '登出时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    login_status VARCHAR(20) COMMENT '登录状态：SUCCESS/FAIL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ============================================
-- 9. 操作日志表 (operation_log)
-- ============================================
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation_module VARCHAR(50) COMMENT '操作模块',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 10. 数据统计缓存表 (statistics_cache)
-- ============================================
DROP TABLE IF EXISTS statistics_cache;
CREATE TABLE statistics_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    stat_type VARCHAR(50) NOT NULL COMMENT '统计类型',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_data JSON COMMENT '统计数据JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stat_type (stat_type),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据统计缓存表';
