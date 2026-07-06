-- =====================================================================
-- AIDSP Platform - MySQL 8.0 全量建表脚本
-- 依据: docs/database.md §2 (11 张表) + §3 (索引)
-- 字符集: utf8mb4 / 排序规则: utf8mb4_unicode_ci
-- 引擎:   InnoDB
-- 说明:   所有 IF NOT EXISTS 包裹, 容忍重复执行
-- =====================================================================

CREATE DATABASE IF NOT EXISTS aidsp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aidsp;
SET NAMES utf8mb4;

-- =====================================================================
-- §2.1 user 用户表
-- =====================================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(64)  NOT NULL                COMMENT '用户名',
  `email`         VARCHAR(128) DEFAULT NULL            COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL                COMMENT '密码哈希 (BCrypt)',
  `display_name`  VARCHAR(64)  DEFAULT NULL            COMMENT '显示名',
  `role`          TINYINT      NOT NULL                COMMENT '角色: 0-普通 1-研究员 2-管理员',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '0-禁用 1-启用',
  `created_at`    DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email`    (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================================
-- §2.2 industry 行业表 (含 parent_id 自引用)
-- =====================================================================
CREATE TABLE IF NOT EXISTS `industry` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code`        VARCHAR(32)  NOT NULL                COMMENT '行业编码 (GB/T 4754)',
  `name`        VARCHAR(128) NOT NULL                COMMENT '行业名称',
  `level`       TINYINT      NOT NULL                COMMENT '1-门类 2-大类 3-中类 4-小类',
  `parent_id`   BIGINT       DEFAULT NULL            COMMENT '父行业, 自引用 industry.id',
  `description` TEXT         DEFAULT NULL            COMMENT '描述',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '0-禁用 1-启用',
  `created_at`  DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行业表';

-- =====================================================================
-- §2.3 company 公司表
-- =====================================================================
CREATE TABLE IF NOT EXISTS `company` (
  `id`                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`                VARCHAR(128)   NOT NULL                COMMENT '公司名称',
  `uscc`                VARCHAR(18)    DEFAULT NULL            COMMENT '统一社会信用代码',
  `stock_code`          VARCHAR(16)    DEFAULT NULL            COMMENT '股票代码 (仅作标识)',
  `industry_id`         BIGINT         NOT NULL                COMMENT '所属行业 industry.id',
  `main_business`       TEXT           DEFAULT NULL            COMMENT '主营业务',
  `founded_at`          DATE           DEFAULT NULL            COMMENT '成立日期',
  `registered_capital` DECIMAL(20,2)   DEFAULT NULL            COMMENT '注册资本',
  `province`            VARCHAR(32)    DEFAULT NULL            COMMENT '省份',
  `city`                VARCHAR(32)    DEFAULT NULL            COMMENT '城市',
  `summary`             TEXT           DEFAULT NULL            COMMENT 'AI 生成的简介',
  `logo_url`            VARCHAR(255)   DEFAULT NULL            COMMENT 'Logo URL',
  `status`              TINYINT        NOT NULL DEFAULT 1      COMMENT '0-禁用 1-启用',
  `created_at`          DATETIME       NOT NULL                COMMENT '创建时间',
  `updated_at`          DATETIME       NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uscc`       (`uscc`),
  UNIQUE KEY `uk_stock_code` (`stock_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司表';

-- =====================================================================
-- §2.4 company_financial 公司财报
-- 唯一索引 (company_id, period) 在 §3.2 集中创建
-- =====================================================================
CREATE TABLE IF NOT EXISTS `company_financial` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `company_id`   BIGINT       NOT NULL                COMMENT '公司 ID',
  `period`       VARCHAR(16)  NOT NULL                COMMENT '期间, 如 2025Q1 / 2024',
  `revenue`      DECIMAL(20,2) DEFAULT NULL           COMMENT '营收',
  `net_profit`   DECIMAL(20,2) DEFAULT NULL           COMMENT '净利润',
  `total_assets` DECIMAL(20,2) DEFAULT NULL           COMMENT '总资产',
  `gross_margin` DECIMAL(8,4)  DEFAULT NULL           COMMENT '毛利率',
  `source`       VARCHAR(255) DEFAULT NULL            COMMENT '数据来源',
  `created_at`   DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司财报';

-- =====================================================================
-- §2.5 report 报告表
-- =====================================================================
CREATE TABLE IF NOT EXISTS `report` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(255) NOT NULL                COMMENT '标题',
  `type`         VARCHAR(32)  NOT NULL                COMMENT '报告类型: industry/company/comparative',
  `target_type`  VARCHAR(16)  NOT NULL                COMMENT '关联对象类型: INDUSTRY/COMPANY',
  `target_id`    BIGINT       NOT NULL                COMMENT '关联对象 ID',
  `status`       VARCHAR(16)  NOT NULL                COMMENT 'DRAFT/GENERATING/SUCCESS/FAILED',
  `version`      INT          NOT NULL DEFAULT 1      COMMENT '版本号',
  `agent_run_id` BIGINT       DEFAULT NULL            COMMENT '生成此报告的 Agent 执行 ID',
  `created_by`   BIGINT       DEFAULT NULL            COMMENT '创建人 user.id',
  `created_at`   DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告表';

-- =====================================================================
-- §2.6 report_section 报告章节
-- 唯一索引 (report_id, seq) 在 §3.2 集中创建
-- =====================================================================
CREATE TABLE IF NOT EXISTS `report_section` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `report_id`   BIGINT       NOT NULL                COMMENT '报告 ID',
  `seq`         INT          NOT NULL                COMMENT '章节顺序',
  `heading`     VARCHAR(255) NOT NULL                COMMENT '章节标题',
  `content_md`  LONGTEXT     DEFAULT NULL            COMMENT 'Markdown 内容',
  `word_count`  INT          DEFAULT NULL            COMMENT '字数',
  `confidence`  DECIMAL(4,3) DEFAULT NULL            COMMENT '置信度 0~1',
  `created_at`  DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告章节';

-- =====================================================================
-- §2.7 citation 引用表
-- =====================================================================
CREATE TABLE IF NOT EXISTS `citation` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `section_id`    BIGINT       DEFAULT NULL            COMMENT '章节 ID',
  `knowledge_id`  BIGINT       DEFAULT NULL            COMMENT '知识库引用 ID',
  `external_url`  VARCHAR(512) DEFAULT NULL            COMMENT '外部链接',
  `snippet`       TEXT         DEFAULT NULL            COMMENT '引用片段',
  `created_at`    DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_section`   (`section_id`),
  KEY `idx_knowledge` (`knowledge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='引用表';

-- =====================================================================
-- §2.8 knowledge 知识库条目
-- 唯一索引 content_hash 在 §3.2 集中创建
-- =====================================================================
CREATE TABLE IF NOT EXISTS `knowledge` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(255) NOT NULL                COMMENT '标题',
  `source_type`  VARCHAR(32)  NOT NULL                COMMENT 'WEB/FILE/MANUAL',
  `source_url`   VARCHAR(512) DEFAULT NULL            COMMENT '来源 URL',
  `content_hash` VARCHAR(64)  NOT NULL                COMMENT '内容 SHA-256',
  `raw_content`  LONGTEXT     DEFAULT NULL            COMMENT '原文',
  `language`     VARCHAR(8)   DEFAULT 'zh'            COMMENT '语言',
  `published_at` DATETIME     DEFAULT NULL            COMMENT '发布时间',
  `status`       TINYINT      NOT NULL DEFAULT 1      COMMENT '0-禁用 1-启用',
  `created_at`   DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`   DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库条目';

-- =====================================================================
-- §2.9 knowledge_chunk 知识切片
-- 唯一索引 (knowledge_id, seq) 在 §3.2 集中创建
-- =====================================================================
CREATE TABLE IF NOT EXISTS `knowledge_chunk` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `knowledge_id` BIGINT      NOT NULL                COMMENT '知识 ID',
  `seq`          INT         NOT NULL                COMMENT '切片顺序',
  `content`      TEXT        NOT NULL                COMMENT '切片内容',
  `token_count`  INT         DEFAULT NULL            COMMENT 'Token 数',
  `vector_id`    VARCHAR(64) DEFAULT NULL            COMMENT 'Milvus 中的向量 ID',
  `created_at`   DATETIME    NOT NULL                COMMENT '创建时间',
  `updated_at`   DATETIME    NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识切片';

-- =====================================================================
-- §2.10 agent_run Agent 执行记录
-- 唯一索引 task_id 在 §3.2 集中创建
-- =====================================================================
CREATE TABLE IF NOT EXISTS `agent_run` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id`          VARCHAR(64)  NOT NULL                COMMENT '外部任务 ID',
  `agent_name`       VARCHAR(64)  NOT NULL                COMMENT 'Agent 名称',
  `role`             VARCHAR(16)  NOT NULL                COMMENT 'PLANNER/WORKER/VALIDATOR',
  `input_json`       LONGTEXT     DEFAULT NULL            COMMENT '输入 JSON',
  `output_json`      LONGTEXT     DEFAULT NULL            COMMENT '输出 JSON',
  `status`           VARCHAR(16)  NOT NULL                COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
  `error_message`    TEXT         DEFAULT NULL            COMMENT '错误信息',
  `prompt_tokens`    INT          DEFAULT NULL            COMMENT 'Prompt Token',
  `completion_tokens` INT         DEFAULT NULL            COMMENT 'Completion Token',
  `total_tokens`     INT          DEFAULT NULL            COMMENT '总 Token',
  `latency_ms`       BIGINT       DEFAULT NULL            COMMENT '延迟毫秒',
  `started_at`       DATETIME     NOT NULL                COMMENT '开始时间',
  `finished_at`      DATETIME     DEFAULT NULL            COMMENT '结束时间',
  `created_at`       DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`       DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 执行记录';

-- =====================================================================
-- §2.11 source 数据来源
-- =====================================================================
CREATE TABLE IF NOT EXISTS `source` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(128) NOT NULL                COMMENT '名称',
  `type`        VARCHAR(32)  NOT NULL                COMMENT 'OFFICIAL/NEWS/RESEARCH',
  `url`         VARCHAR(512) DEFAULT NULL            COMMENT 'URL',
  `credibility` TINYINT      DEFAULT 3               COMMENT '可信度 1~5',
  `created_at`  DATETIME     NOT NULL                COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL                COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据来源';

-- =====================================================================
-- §3.2 重点索引 (NORMAL / UNIQUE)
-- =====================================================================

-- company
ALTER TABLE `company`           ADD INDEX `idx_industry`        (`industry_id`);
ALTER TABLE `company`           ADD INDEX `idx_name`            (`name`);
ALTER TABLE `company`           ADD INDEX `idx_updated`         (`updated_at` DESC);

-- company_financial
ALTER TABLE `company_financial` ADD UNIQUE INDEX `uk_company_period` (`company_id`, `period`);

-- report
ALTER TABLE `report`            ADD INDEX `idx_target`          (`target_type`, `target_id`);
ALTER TABLE `report`            ADD INDEX `idx_status_created`  (`status`, `created_at` DESC);

-- report_section
ALTER TABLE `report_section`    ADD UNIQUE INDEX `uk_report_seq`     (`report_id`, `seq`);

-- knowledge
ALTER TABLE `knowledge`         ADD UNIQUE INDEX `uk_content_hash`   (`content_hash`);
ALTER TABLE `knowledge`         ADD INDEX `idx_published`        (`published_at` DESC);

-- knowledge_chunk
ALTER TABLE `knowledge_chunk`   ADD UNIQUE INDEX `uk_knowledge_seq`  (`knowledge_id`, `seq`);
ALTER TABLE `knowledge_chunk`   ADD INDEX `idx_knowledge`        (`knowledge_id`);

-- agent_run
ALTER TABLE `agent_run`         ADD UNIQUE INDEX `idx_task`           (`task_id`);
ALTER TABLE `agent_run`         ADD INDEX `idx_agent_started`     (`agent_name`, `started_at` DESC);

-- industry
ALTER TABLE `industry`          ADD UNIQUE INDEX `uk_code`           (`code`);

-- =====================================================================
-- §3.3 全文索引 (中文 ngram 解析器)
-- =====================================================================
ALTER TABLE `report_section`    ADD FULLTEXT INDEX `ft_content`       (`content_md`) WITH PARSER ngram;
ALTER TABLE `knowledge_chunk`   ADD FULLTEXT INDEX `ft_content`       (`content`)    WITH PARSER ngram;

-- =====================================================================
-- 初始数据: 测试 admin 用户
-- password_hash 为 BCrypt 占位, 启动后请通过应用层改写
-- =====================================================================
INSERT INTO `user` (`username`, `email`, `password_hash`, `display_name`, `role`, `status`, `created_at`, `updated_at`)
VALUES (
  'admin',
  'admin@aidsp.local',
  'BCrypt:$2a$10$placeholderplaceholderplaceholderplaceholderplaceholderpla',
  '系统管理员',
  2,
  1,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE `updated_at` = NOW();
