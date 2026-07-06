# 数据库设计 (Database Design)

> 本文件定义系统的数据库结构、ER 关系与索引策略。
> 数据库：MySQL 8.0，字符集：`utf8mb4`，排序规则：`utf8mb4_unicode_ci`。

---

## 1. ER 图

```
┌────────────┐         ┌────────────┐
│  industry  │ 1     N │  company   │
│            │─────────│            │
└────────────┘         └─────┬──────┘
                             │ N
                             │
                             │ 1
                       ┌─────▼──────┐         ┌────────────┐
                       │  report    │ 1     N │ report_sec │
                       │            │─────────│            │
                       └─────┬──────┘         └────────────┘
                             │ N
                             │
                       ┌─────▼──────┐
                       │  citation  │
                       └────────────┘

┌────────────┐         ┌────────────┐
│ knowledge  │ 1     N │  chunk     │
│            │─────────│            │
└────────────┘         └─────┬──────┘
                             │ N
                             │
                       ┌─────▼──────┐
                       │ embedding  │ (Milvus 单独存储)
                       └────────────┘

┌────────────┐
│   user     │
└────────────┘

┌────────────┐         ┌────────────┐
│ agent_run  │ N     1 │  report    │
│            │─────────│            │
└────────────┘         └────────────┘
```

---

## 2. 表结构

### 2.1 `user` 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(64) | UNIQUE, NOT NULL | 用户名 |
| email | VARCHAR(128) | UNIQUE | 邮箱 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希（BCrypt） |
| display_name | VARCHAR(64) | | 显示名 |
| role | TINYINT | NOT NULL | 角色：0-普通 1-研究员 2-管理员 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 0-禁用 1-启用 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

### 2.2 `industry` 行业表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| code | VARCHAR(32) | UNIQUE, NOT NULL | 行业编码（GB/T 4754） |
| name | VARCHAR(128) | NOT NULL | 行业名称 |
| level | TINYINT | NOT NULL | 1-门类 2-大类 3-中类 4-小类 |
| parent_id | BIGINT | FK→industry.id, NULL | 父行业 |
| description | TEXT | | 描述 |
| status | TINYINT | NOT NULL, DEFAULT 1 | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

### 2.3 `company` 公司表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(128) | NOT NULL | 公司名称 |
| uscc | VARCHAR(18) | UNIQUE | 统一社会信用代码 |
| stock_code | VARCHAR(16) | UNIQUE, NULL | 股票代码（仅作标识） |
| industry_id | BIGINT | FK→industry.id, NOT NULL | 所属行业 |
| main_business | TEXT | | 主营业务 |
| founded_at | DATE | | 成立日期 |
| registered_capital | DECIMAL(20,2) | | 注册资本 |
| province | VARCHAR(32) | | 省份 |
| city | VARCHAR(32) | | 城市 |
| summary | TEXT | | AI 生成的简介 |
| logo_url | VARCHAR(255) | | |
| status | TINYINT | NOT NULL, DEFAULT 1 | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

### 2.4 `company_financial` 公司财报

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| company_id | BIGINT | FK, NOT NULL | |
| period | VARCHAR(16) | NOT NULL | 如 `2025Q1` / `2024` |
| revenue | DECIMAL(20,2) | | 营收 |
| net_profit | DECIMAL(20,2) | | 净利润 |
| total_assets | DECIMAL(20,2) | | 总资产 |
| gross_margin | DECIMAL(8,4) | | 毛利率 |
| source | VARCHAR(255) | | 数据来源 |
| created_at | DATETIME | NOT NULL | |

> 唯一索引：`(company_id, period)`

### 2.5 `report` 报告表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| title | VARCHAR(255) | NOT NULL | 标题 |
| type | VARCHAR(32) | NOT NULL | 报告类型：industry/company/comparative |
| target_type | VARCHAR(16) | NOT NULL | 关联对象类型：INDUSTRY/COMPANY |
| target_id | BIGINT | NOT NULL | 关联对象 ID |
| status | VARCHAR(16) | NOT NULL | DRAFT/GENERATING/SUCCESS/FAILED |
| version | INT | NOT NULL, DEFAULT 1 | 版本号 |
| agent_run_id | BIGINT | FK→agent_run.id | 生成此报告的 Agent 执行 |
| created_by | BIGINT | FK→user.id | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

### 2.6 `report_section` 报告章节

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| report_id | BIGINT | FK, NOT NULL | |
| seq | INT | NOT NULL | 章节顺序 |
| heading | VARCHAR(255) | NOT NULL | 章节标题 |
| content_md | LONGTEXT | | Markdown 内容 |
| word_count | INT | | 字数 |
| confidence | DECIMAL(4,3) | | 置信度 0~1 |
| created_at | DATETIME | NOT NULL | |

> 唯一索引：`(report_id, seq)`

### 2.7 `citation` 引用表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| section_id | BIGINT | FK→report_section.id | |
| knowledge_id | BIGINT | FK→knowledge.id, NULL | 知识库引用 |
| external_url | VARCHAR(512) | | 外部链接 |
| snippet | TEXT | | 引用片段 |
| created_at | DATETIME | NOT NULL | |

### 2.8 `knowledge` 知识库条目

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| title | VARCHAR(255) | NOT NULL | |
| source_type | VARCHAR(32) | NOT NULL | WEB/FILE/MANUAL |
| source_url | VARCHAR(512) | | |
| content_hash | VARCHAR(64) | UNIQUE, NOT NULL | 内容 SHA-256 |
| raw_content | LONGTEXT | | 原文 |
| language | VARCHAR(8) | DEFAULT 'zh' | |
| published_at | DATETIME | | |
| status | TINYINT | NOT NULL, DEFAULT 1 | |
| created_at | DATETIME | NOT NULL | |

### 2.9 `knowledge_chunk` 知识切片

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| knowledge_id | BIGINT | FK, NOT NULL | |
| seq | INT | NOT NULL | 切片顺序 |
| content | TEXT | NOT NULL | 切片内容 |
| token_count | INT | | |
| vector_id | VARCHAR(64) | | Milvus 中的向量 ID |

> 唯一索引：`(knowledge_id, seq)`

### 2.10 `agent_run` Agent 执行记录

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| task_id | VARCHAR(64) | UNIQUE, NOT NULL | 外部任务 ID |
| agent_name | VARCHAR(64) | NOT NULL | |
| role | VARCHAR(16) | NOT NULL | PLANNER/WORKER/VALIDATOR |
| input_json | LONGTEXT | | |
| output_json | LONGTEXT | | |
| status | VARCHAR(16) | NOT NULL | |
| error_message | TEXT | | |
| prompt_tokens | INT | | |
| completion_tokens | INT | | |
| total_tokens | INT | | |
| latency_ms | BIGINT | | |
| started_at | DATETIME | NOT NULL | |
| finished_at | DATETIME | | |

### 2.11 `source` 数据来源

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| name | VARCHAR(128) | NOT NULL | |
| type | VARCHAR(32) | NOT NULL | OFFICIAL/NEWS/RESEARCH |
| url | VARCHAR(512) | | |
| credibility | TINYINT | DEFAULT 3 | 可信度 1~5 |
| created_at | DATETIME | NOT NULL | |

---

## 3. 索引设计

### 3.1 通用原则

- 主键索引：自增 BIGINT
- 外键：必须建索引
- 唯一约束：自动建唯一索引
- 复合索引：遵循**最左前缀**原则
- 索引列**NOT NULL** 优先
- 单表索引数 ≤ 5（不含主键）

### 3.2 重点索引

| 表 | 索引 | 类型 | 字段 |
|----|------|------|------|
| company | idx_industry | NORMAL | (industry_id) |
| company | idx_name | NORMAL | (name) |
| company | idx_updated | NORMAL | (updated_at DESC) |
| company_financial | uk_company_period | UNIQUE | (company_id, period) |
| report | idx_target | NORMAL | (target_type, target_id) |
| report | idx_status_created | NORMAL | (status, created_at DESC) |
| report_section | uk_report_seq | UNIQUE | (report_id, seq) |
| knowledge | uk_content_hash | UNIQUE | (content_hash) |
| knowledge | idx_published | NORMAL | (published_at DESC) |
| knowledge_chunk | idx_knowledge | NORMAL | (knowledge_id) |
| agent_run | idx_task | UNIQUE | (task_id) |
| agent_run | idx_agent_started | NORMAL | (agent_name, started_at DESC) |
| industry | uk_code | UNIQUE | (code) |
| industry | idx_parent | NORMAL | (parent_id) |

### 3.3 全文索引

- `report_section.content_md`：FULLTEXT（中文分词用 ngram 解析器）
- `knowledge_chunk.content`：FULLTEXT

```sql
ALTER TABLE report_section
  ADD FULLTEXT INDEX ft_content (content_md) WITH PARSER ngram;
```

### 3.4 复合索引示例

```sql
-- 报告列表：按目标 + 状态 + 时间倒序
CREATE INDEX idx_target_status_created
  ON report (target_type, target_id, status, created_at DESC);
```

---

## 4. 数据完整性

### 4.1 外键策略

- 业务核心表（company / industry / report / knowledge）使用外键
- 日志/统计表（agent_run）不强制外键，应用层保证

### 4.2 软删除

- 业务表使用 `status` 字段表示禁用/删除
- 真正删除仅管理员手工 SQL 操作

### 4.3 审计字段

- 所有业务表包含 `created_at`, `updated_at`
- 关键表加 `created_by`, `updated_by`

---

## 4. 持久层扩展（沿用 MBSE）

> 位于 `aidsp-core.mybatisplus` 包，所有业务模块的 Repository 复用以下扩展。

### 4.1 `MyBaseMapper<T>`

- 继承 `BaseMapper<T>`，扩展 `selectCacheByIds` / `batchUpdateOnVersion` 等
- 业务模块自定义 Mapper 必须继承 `MyBaseMapper<T>`

### 4.2 通用字段自动填充

- `MetaObjectHandler` 自动写入：
  - `createTime` / `updateTime`
  - `createBy` / `updateBy`（从 `UserContext.get()` 取）
- 字段上加 `@TableField(fill = FieldFill.INSERT)` / `INSERT_UPDATE`

### 4.3 `@VersionControl` 版本感知写操作

- 注解在 Entity 或 Mapper 方法上
- 拦截器自动改写：所有 `update` / `delete` 走 `*OnVersion` 形式
- 强制带 `version` 条件，防止并发覆盖

### 4.4 `@DataScope` 数据权限

- 注解在 Mapper 方法参数上（如 `@DataScope(deptAlias = "c")`）
- 拦截器自动拼接 `WHERE dept_id IN (...)`
- 权限规则从 `UserContext` / `DataScopeService` 读取

### 4.5 关键字转义

- MyBatis-Plus 拦截器统一处理 `order`、`key` 等关键字
- 兼容 MySQL / 达梦 / 人大金仓

---

## 5. 分库分表（未来）

- 单机 MySQL 起步
- 预估数据量 > 1000w 行时考虑：
  - 按 `industry_id` 哈希分表 `company`
  - 按 `created_at` 月份分表 `report` / `agent_run`

---

## 6. 修订记录

| 版本 | 日期 | 修订内容 |
|------|------|----------|
| 1.0  | 2026-07-06 | 初版数据库设计 |
