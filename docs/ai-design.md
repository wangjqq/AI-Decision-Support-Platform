# AI 设计规范 (AI Design Specification)

> 本文件定义系统内 AI Agent、Prompt、Tool Calling 与 JSON 输出的设计规范。
> 优先级：项目宪法 > 本文件 > 代码实现。

---

## 1. Agent 拆分规则

### 1.1 角色模型（三层强制）

| 角色 | 数量 | 职责 | 工具权限 |
|------|------|------|----------|
| **Planner** | 1 | 任务理解、计划拆解、子任务编排 | 只读工具 |
| **Worker** | N | 执行具体子任务（检索、生成） | 读写工具 |
| **Validator** | 1 | 校验输出（事实、引用、格式、置信度） | 只读工具 |

> 任何核心分析任务必须包含 **至少 1 Planner + ≥1 Worker + 1 Validator**。
> 简单任务（如分类、抽取）允许 Planner 兼任 Validator，但 **Worker 不可兼任**。

### 1.2 Agent 命名

- 命名空间：`aidsp.{module}.{role}.{purpose}`
- 示例：
  - `aidsp.report.planner.industry`
  - `aidsp.report.worker.research`
  - `aidsp.report.validator.facts`

### 1.4 Agent 注册中心

所有 Agent 在 `aidsp-agent` 模块的 `AgentRegistry` / `AgentStrategyFactory` 中注册：

```java
public interface Agent {
    String name();
    String role();          // PLANNER / WORKER / VALIDATOR
    String description();
    AgentResult run(AgentContext ctx);
}
```

### 1.4 Agent 通信

- Agent 间通过 **结构化 JSON** 通信
- 中间产物写入 `agent_run.input_json` / `output_json`
- 禁止 Agent 之间传递非序列化对象

### 1.5 失败处理

| 失败环节 | 处理 |
|----------|------|
| Planner 失败 | 整体失败，返回 5000 |
| Worker 失败 | 重试 ≤ 3 次，最终失败上报 Planner 重新拆解 |
| Validator 失败 | 反馈 Planner 修订，最多 2 轮 |
| 超过最大轮次 | 整体失败，写入失败原因 |

---

## 2. Prompt 规范

### 2.1 Prompt 文件组织

```
prompts/
├── system/                    # 系统级 Prompt
│   ├── planner.system.md
│   ├── worker.research.system.md
│   ├── worker.analysis.system.md
│   └── validator.system.md
├── templates/                 # 任务模板
│   ├── report.industry.template.md
│   ├── report.company.template.md
│   └── report.comparative.template.md
└── examples/                  # Few-shot 示例
    ├── plan.example.json
    ├── section.example.json
    └── validation.example.json
```

### 2.2 Prompt 结构模板

每个 Prompt 必须包含以下结构：

```markdown
# Role
（角色定义：身份、目标、边界）

# Context
（背景信息：用户输入、历史、知识）

# Task
（明确任务：动词 + 输出物 + 约束）

# Constraints
（硬约束：禁止事项、长度、风格）

# Output Format
（严格 JSON Schema）

# Examples
（Few-shot 示例，可选）

# Self-Check
（自检清单：让模型在输出前自检）
```

### 2.3 Prompt 编写原则

1. **明确角色**：让模型知道它是谁
2. **明确目标**：动词开头
3. **明确边界**：禁止事项必须显式列出
4. **明确格式**：JSON Schema 写清楚
5. **可验证**：包含 Self-Check 清单
6. **少废话**：禁止礼貌用语、客套、感叹
7. **中文优先**：本系统以中文输出
8. **可追溯**：要求模型标注引用来源

### 2.4 禁止 Prompt

- ❌ "请你像一个专家一样…"（无具体约束）
- ❌ "写一份报告"（无格式）
- ❌ "分析一下"（无输出要求）
- ❌ Prompt 超过 3000 tokens（拆分到 Context）

### 2.5 版本管理

- Prompt 文件纳入 Git
- 每次修改记录版本号与变更说明
- 线上 Prompt 通过 `prompt_version` 字段管理

---

## 3. JSON 输出规范

### 3.1 通用 Schema

所有 Agent 输出必须遵循：

```json
{
  "status": "SUCCESS | PARTIAL | FAILED",
  "data": { ... },                     // 业务数据
  "evidence": [                        // 引用证据
    {
      "type": "KNOWLEDGE | URL | INTERNAL",
      "ref_id": "knowledge:123",
      "snippet": "原文片段",
      "score": 0.92
    }
  ],
  "confidence": 0.85,                  // 0~1
  "reasoning": "推理过程摘要（≤200字）",
  "warnings": ["潜在问题1", ...]
}
```

### 3.2 字段规则

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | enum | ✅ | 三态 |
| data | object | ✅ | 业务数据，结构由任务决定 |
| evidence | array | ✅ | 至少 1 项，Validator 强校验 |
| confidence | number | ✅ | 0~1，< 0.6 触发人工审核 |
| reasoning | string | ✅ | 中文 |
| warnings | array | ❌ | 列出不确定性 |

### 3.3 子 Schema 示例

#### 3.3.1 Planner 输出

```json
{
  "status": "SUCCESS",
  "data": {
    "plan_id": "P20260706143001",
    "goal": "生成 AI 芯片行业研究报告",
    "subtasks": [
      {
        "id": "S1",
        "agent": "aidsp.report.worker.research",
        "task": "检索 AI 芯片行业 2025 趋势",
        "depends_on": []
      },
      {
        "id": "S2",
        "agent": "aidsp.report.worker.analysis",
        "task": "分析头部公司业务格局",
        "depends_on": ["S1"]
      }
    ]
  },
  "evidence": [...],
  "confidence": 0.9,
  "reasoning": "..."
}
```

#### 3.3.2 Worker 章节输出

```json
{
  "status": "SUCCESS",
  "data": {
    "heading": "行业概览",
    "content_md": "## 行业概览\n...",
    "word_count": 850
  },
  "evidence": [
    { "type": "KNOWLEDGE", "ref_id": "knowledge:45", "snippet": "...", "score": 0.88 }
  ],
  "confidence": 0.82,
  "reasoning": "基于知识库 45/46/47 条目综合..."
}
```

#### 3.3.3 Validator 输出

```json
{
  "status": "SUCCESS",
  "data": {
    "passed": true,
    "checks": {
      "format": { "ok": true },
      "facts": { "ok": true, "issues": [] },
      "citations": { "ok": true, "count": 5 },
      "hallucination": { "ok": true, "score": 0.05 }
    }
  },
  "evidence": [],
  "confidence": 0.95,
  "reasoning": "所有检查通过"
}
```

### 3.4 JSON 校验

- 使用 `jackson` + Schema 校验
- LLM 输出必须经过 `JsonExtractor` 提取 JSON
- 解析失败 → 重试 1 次 → 失败则上报

---

## 4. Tool Calling 规则

### 4.1 工具定义

使用 Spring AI 的 `@Tool` 注解：

```java
@Tool(name = "rag_search", description = "在知识库中语义检索")
public List<KnowledgeChunk> search(
    @ToolParam(description = "查询文本") String query,
    @ToolParam(description = "返回条数") int topK
) { ... }
```

### 4.2 工具清单

| 工具名 | 用途 | 权限 |
|--------|------|------|
| `search_company` | 查询公司 | READ |
| `search_industry` | 查询行业 | READ |
| `fetch_financials` | 拉取财报 | READ |
| `rag_search` | 知识库检索 | READ |
| `list_reports` | 列出报告 | READ |
| `save_draft_section` | 保存草稿 | WRITE |
| `publish_report` | 发布报告 | WRITE |

### 4.3 工具调用约束

1. **可观测**：所有 Tool 调用记录入 `agent_run`
2. **幂等**：GET 类工具必须幂等
3. **超时**：单 Tool 调用 ≤ 30s
4. **限流**：单 AgentRun 累计 Token ≤ 100k
5. **校验**：写入类工具（save_draft_section / publish_report）必须先经 Validator

### 4.4 工具安全

- Tool 入参由 LLM 生成，必须做：
  - 长度限制
  - 类型校验
  - 注入检查（关键词黑名单）
  - 越权检查（用户上下文）

### 4.5 Tool 与 Agent 权限矩阵

| Agent \ Tool | rag_search | fetch_financials | save_draft | publish |
|--------------|:----------:|:----------------:|:----------:|:-------:|
| Planner      | ✅          | ❌                | ❌          | ❌       |
| Worker       | ✅          | ✅                | ✅          | ❌       |
| Validator    | ✅          | ✅                | ❌          | ❌       |

---

## 5. RAG 规范

### 5.1 切片策略

- 默认按段落 + 标点切片
- 单片 300~800 tokens
- 重叠 50~100 tokens
- 保留章节标题作为切片前缀

### 5.2 检索流程

```
Query
  → Embedding (bge-large-zh)
  → Vector Search (Milvus topK=20)
  → Rerank (bge-reranker-large → topK=5)
  → Context Assembly
  → LLM
```

### 5.3 引用规范

- LLM 输出必须包含 `[ref:knowledge:45]` 标记
- 后处理将其解析为 `citation` 表记录
- 引用片段必须 ≥ 10 字符

---

## 6. 模型与成本

### 6.1 模型分级

| 级别 | 用途 | 模型 |
|------|------|------|
| L0 抽取 | 实体识别、分类 | 小模型（Qwen2.5-7B） |
| L1 推理 | 章节生成 | 中模型（DeepSeek-V3） |
| L2 规划 | Planner | 强模型（GPT-4 级） |

### 6.2 成本控制

- 单报告 Token 上限：100k
- 失败重试 Token 计入单报告
- 超出上限 → 强制结束 + 警告

### 6.3 缓存

- 相同 query + 知识库版本命中缓存 → 直接返回
- 缓存 Key：`sha256(query + knowledge_version)`

---

## 7. 安全与对齐

### 7.1 Prompt 注入防御

- 用户输入与系统 Prompt 物理隔离
- 使用 `<user_query>` 标签包裹
- 高危指令黑名单（"忽略以上指令"等）

### 7.2 内容过滤

- 输出前经过敏感词过滤
- 涉及医疗/法律/投资建议 → 风险提示
- **绝对禁止**输出"买入/卖出"等交易指令

### 7.3 审计

- 完整记录：Prompt + Response + Tool Calls + Citations
- 保留期限：≥ 180 天
- 不可篡改：使用 append-only 存储

---

## 8. 修订记录

| 版本 | 日期 | 修订内容 |
|------|------|----------|
| 1.0  | 2026-07-06 | 初版 AI 设计规范 |
