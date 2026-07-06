# 项目宪法 (Constitution)

> 本文件为项目最高优先级文档，所有架构决策、代码实现、AI 设计均须遵循本宪法。
> 任何与之冲突的设计须先修改本宪法，再行实施。

---

## 1. 项目定位

**项目名称**：AI Decision Support Platform（AI 决策支持平台）

**核心定位**：
- 为企业/投资者提供 **结构化、可解释、可追溯** 的行业与公司决策支持信息
- 聚焦于 **行业研究、公司画像、报告生成** 三大场景
- 以 AI Agent 协作 + 知识库（RAG）为核心能力
- 输出形式以**研究报告、对比分析、趋势解读**为主，非实时交易指令

**目标用户**：
- 行业研究员
- 投资分析师（辅助决策）
- 企业战略部门
- 学术研究者

---

## 2. 不做什么（明确边界）

> 以下为**绝对禁止**事项，违反者视为破坏项目宪法。

1. **禁止任何形式的"炒股系统化表达"**
   - ❌ 不提供买入/卖出/加仓/减仓信号
   - ❌ 不输出实时股价、涨跌幅、买卖点
   - ❌ 不做"明日涨跌预测"、短线择时建议
   - ❌ 不展示收益曲线、胜率、回测等交易化指标
   - ✅ 仅做长期、行业级、公司基本面的研究型输出

2. **禁止单 Prompt 完成核心业务**
   - 任何"一气呵成"的 Prompt 调用禁止作为正式功能上线
   - 核心分析必须经多 Agent 协作 + 结构化校验

3. **禁止 AI 幻觉数据进入数据库**
   - 所有 AI 生成的实体（Company / Industry / Report）必须可追溯到来源
   - 不允许将无来源信息直接落库

4. **禁止跨模块直接访问数据库**
   - 模块间通信走 Service / API，不允许绕过

5. **禁止在前端硬编码业务逻辑**
   - 前端仅做展示与交互，决策逻辑在后端

---

## 3. 架构原则

### 3.1 模块化单体（Modular Monolith）

- **首选**：模块化单体架构
  - 一个部署包，清晰的模块边界
  - 业务模块内部统一拆为 `xxx-api`（接口/DTO）和 `xxx-service`（实现）
  - 跨模块调用走 **Dubbo in-JVM**
- **不优先微服务**：避免过早分布式化
- **演进路径**：模块化单体（Dubbo in-JVM）→ （必要时）Dubbo 跨进程 / 微服务

### 3.2 分层架构

```
┌────────────────────────────┐
│  Frontend (React + Vite)   │
└──────────────┬─────────────┘
               │ REST / JSON
┌──────────────▼─────────────┐
│  aidsp-web (Controller)    │  ← GlobalRestResponseAdvice 自动包装
├────────────────────────────┤
│  Service Layer (业务)       │  ← 跨模块走 Dubbo in-JVM
├────────────────────────────┤
│  Domain / Agent Layer      │
├────────────────────────────┤
│  Repository (MyBaseMapper) │  ← 含 @VersionControl / @DataScope
├────────────────────────────┤
│  Database (MySQL 8)        │
└────────────────────────────┘
         │ ▲
   Artemis│ │Spring Event
         ▼ │
  ┌────────────────┐
  │ Cache / MQ     │
  └────────────────┘
```

### 3.3 命名与目录约定

- 后端包名：`com.aidsp.platform.{module}`
- Maven 模块：
  - `aidsp-core`（基础设施，0 业务依赖）
  - `aidsp-sys` / `aidsp-{biz}-api` / `aidsp-{biz}-service` / `aidsp-adapter` / `aidsp-web`
- 前端目录：`src/{module}/`
- 公共组件：`frontend/src/components/`
- 状态管理：**Redux Toolkit + RTK Query**（明确不使用 Zustand）
- Agent 实现统一在 `aidsp-agent-service` 模块下

### 3.4 依赖原则

- 上层可依赖下层，下层不可依赖上层
- `aidsp-agent` 可调用 `aidsp-knowledge` / `aidsp-rag`（通过 Dubbo in-JVM）
- `aidsp-report` 可调用 `aidsp-company` / `aidsp-industry` / `aidsp-agent`
- `aidsp-core` 严禁出现业务代码
- 反向依赖禁止

---

## 4. AI 使用原则

### 4.1 多 Agent 协作（强制）

- **核心业务禁止单 Prompt**
- 任何分析任务必须由 ≥ 2 个 Agent 协作完成
- Agent 之间通过结构化 JSON 通信
- 必须有 **规划 Agent（Planner）→ 执行 Agent（Worker）→ 校验 Agent（Validator）** 三个角色

### 4.2 Agent 设计约束

| 角色 | 职责 | 禁止 |
|------|------|------|
| Planner | 任务拆解、计划生成 | 直接执行业务逻辑 |
| Worker | 调用工具、检索知识、生成内容 | 自行决策流程 |
| Validator | 校验输出、检测幻觉、评分 | 篡改输出 |

### 4.3 模型使用规范

- 复杂推理任务使用强模型（DeepSeek / GPT-4 级）
- 简单抽取、分类使用轻量模型
- 所有 LLM 调用记录：Prompt / Response / Token / Latency

### 4.4 可解释性

- 每个 AI 输出必须附带：
  - 推理过程摘要
  - 引用来源（知识库 ID 或外部 URL）
  - 置信度评分

---

## 5. 数据模型边界

> 系统核心实体**严格限定**为以下三类，不允许任意扩展新核心实体。

### 5.1 Company（公司）

- 描述：单一企业主体画像
- 关键字段：名称、统一社会信用代码、所属行业、主营业务、财报数据
- 边界：不做股价、不做实时行情

### 5.2 Industry（行业）

- 描述：行业宏观与中观研究
- 关键字段：行业名称、分类标准、产业链结构、政策、趋势
- 边界：不做行业指数预测

### 5.3 Report（报告）

- 描述：AI 生成或人工编辑的研究报告
- 关键字段：标题、类型、关联 Company/Industry、章节、引用、版本
- 边界：报告不可直接作为投资建议

### 5.4 辅助实体

- `Knowledge`：知识库条目（向量 + 原文）
- `AgentRun`：Agent 执行记录
- `User`：用户
- `Source`：数据来源

> 新增核心实体需经过架构评审 + 修改本宪法。

---

## 6. 修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| 1.0  | 2026-07-06 | 初版宪法 | Architect |
| 1.1  | 2026-07-06 | 沿用 MBSE Platform 架构：api/service 拆分、Dubbo in-JVM、Artemis、Spring Security+JWT、`aidsp-core` 零业务约束 | Architect |

---

**宪法的权威性**：
本宪法 > 架构设计 > API 规范 > 数据库设计 > AI 设计 > 代码实现。
任何冲突以高层级为准。
