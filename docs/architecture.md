# 架构设计 (Architecture)

> 本文件描述系统的整体技术架构、模块划分、数据流与 Agent 协作流。
> 沿用 **MBSE Platform** 平台的可复用设计模式，目标是全栈学习一致性。
> 任何架构变更须先更新本文件。

---

## 1. 总体架构

### 1.1 技术栈选型

| 层 | 技术 | 说明 |
|----|------|------|
| 前端 | React 18 + Vite + TypeScript | SPA |
| UI | Ant Design 5 | 中后台风格 |
| 状态 | Redux Toolkit + RTK Query | 全局状态 + 服务端状态缓存 |
| 图表 | ECharts | 数据可视化 |
| 后端 | Spring Boot 3.3.x + Java 21 | 主框架 |
| 服务调用 | Dubbo in-JVM | 跨模块调用 |
| 持久层 | MyBatis-Plus 3.5+ | 含自研扩展（MyBaseMapper / @VersionControl / @DataScope） |
| 数据库 | MySQL 8.0 | 主库 |
| 缓存 | Redis 7 + Guava LoadingCache | 多级缓存 |
| 消息 | Apache Artemis MQ | 异步事件、缓存失效广播 |
| 安全 | Spring Security + JWT | 鉴权 |
| AI 框架 | Spring AI | 统一 LLM 抽象 |
| 构建 | Maven | 多模块 |
| 部署 | Docker + Docker Compose | 单机部署优先 |

### 1.2 启动信息

- 启动类：`com.aidsp.platform.Application`
- 端口：`8013`（与 MBSE Platform 8012 区分）
- API 文档：`http://localhost:8013/doc.html`（Knife4j）
- 端口冲突时通过 `application.yml` 调整

### 1.3 部署拓扑

```
                ┌─────────────────┐
                │   Nginx (前端)  │
                └────────┬────────┘
                         │ HTTP/JSON
                ┌────────▼────────┐
                │  aidsp-web      │
                │  (Spring Boot)  │
                └─┬──────┬──────┬┘
                  │      │      │
            ┌─────▼┐ ┌───▼──┐ ┌─▼──────┐
            │MySQL │ │Redis │ │Artemis │
            └──────┘ └──────┘ └────────┘
                                    │
                              ┌─────▼─────┐
                              │  Milvus   │
                              └───────────┘
```

---

## 2. 后端模块结构（沿用 MBSE）

### 2.1 Maven 多模块

```
aidsp-platform
├── aidsp-core           # 基础设施：配置、安全、缓存、MQ、上下文、工具类（不含业务）
├── aidsp-sys            # 系统管理：用户/角色/菜单/字典/审计
├── aidsp-company        # 公司域（api + service）
├── aidsp-industry       # 行业域（api + service）
├── aidsp-report         # 报告域（api + service）
├── aidsp-agent          # AI Agent 编排（api + service）
├── aidsp-knowledge      # 知识库（api + service）
├── aidsp-rag            # RAG 检索（api + service）
├── aidsp-adapter        # 外部集成：文件 / SSO / 第三方数据源
└── aidsp-web            # Spring Boot 启动入口（含 main）
```

### 2.2 业务模块内部分层

每个业务模块拆为两个子模块：

```
aidsp-company
├── aidsp-company-api     # Dubbo 接口定义、DTO、VO、Enum
└── aidsp-company-service # 业务实现、Controller、Repository、Entity
```

- 跨模块调用走 **Dubbo in-JVM**（`@DubboReference` 注入）
- `api` 模块只暴露接口与数据传输对象，**不含实现**
- `service` 模块**不依赖其他业务的 service**，只依赖其 `api`

### 2.3 依赖规则

```
aidsp-web
  └─> 各 service 模块
        └─> 对应 api 模块 + aidsp-core + aidsp-sys
aidsp-sys → aidsp-core
aidsp-adapter → aidsp-core
aidsp-core   → 0 业务依赖
```

> **强约束**：`aidsp-core` 严禁出现业务代码；违反者 PR 直接拒绝。

---

## 3. 前端目录结构

```
frontend/
├── src/
│   ├── api/            # 接口请求封装（RTK Query）
│   ├── components/     # 公共组件
│   ├── pages/
│   │   ├── dashboard/
│   │   ├── company/
│   │   ├── industry/
│   │   └── report/
│   ├── modules/        # 业务模块
│   ├── stores/         # Redux store + slices
│   ├── hooks/          # useAppDispatch / useAppSelector
│   ├── router/
│   ├── utils/
│   └── main.tsx
├── public/
├── index.html
└── vite.config.ts
```

---

## 4. 核心可复用设计模式（沿用 MBSE）

> 本节列出在本项目中**强制复用**的模式，新代码不得绕过。

### 4.1 统一响应 + 全局异常处理

**位置**：`aidsp-core` 模块
- `core.dto.RestResponse<T>`：统一返回 `{ code, msg, data }`
- `core.interceptor.GlobalRestResponseAdvice`：自动包装 Controller 返回值
- `core.interceptor.GlobalExceptionAdvice`：统一异常 → 业务码
- 自动排除：文件下载、Swagger/Knife4j、OAuth 端点

```java
// Controller 不手动包装
@GetMapping("/company/{id}")
public CompanyVO getCompany(@PathVariable Long id) {
    return companyService.getById(id);
}

// 业务异常直接抛出
throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
```

### 4.2 自动注册策略工厂

`@Component` 命名遵循 `xxxStrategy` → 枚举 `XXX` 的约定，启动时自动注册到 `EnumMap`。新增策略**零工厂改动**。

```java
public interface AgentStrategy {
    AgentResult execute(AgentContext ctx);
}

@Component
public class PlannerAgentStrategy implements AgentStrategy { ... }   // beanName = plannerAgentStrategy
@Component
public class WorkerAgentStrategy  implements AgentStrategy { ... }   // beanName = workerAgentStrategy
```

### 4.3 分层版本化缓存管理器

**位置**：`aidsp-core.cache` + 各业务模块的 `cache.manager`
- 抽象基类 `AbstractVersionedCacheManager<T>` 统一处理：
  - Key 构建：`{prefix}{projectId}:{branchId}:{version}:{dataId}`
  - Key 解析与版本感知
  - Guava `LoadingCache` 容量与过期控制
  - 防御性副本返回
  - `Optional` 防穿透
- 业务子类只关注"如何加载"

**三层结构**：
1. 基础快照缓存（`xxxBaseCache`）
2. 关系缓存（`xxxRelationCache`）
3. 补充加载器（`xxxLoader`）

### 4.4 TTL 上下文传递

**位置**：`aidsp-core.context`
- 使用阿里 `TransmittableThreadLocal` 替代 JDK `ThreadLocal`
- 解决线程池、`@Async`、`Dubbo` 调用链上的上下文丢失

```java
public class UserContext {
    private static final TransmittableThreadLocal<UserInfo> HOLDER = new TransmittableThreadLocal<>();
    public static void set(UserInfo u) { HOLDER.set(u); }
    public static UserInfo get()       { return HOLDER.get(); }
    public static void clear()         { HOLDER.remove(); }   // 必须在 finally
}

public class TenantContext { /* 同上 */ }
public class TraceContext  { /* 链路追踪 */ }
```

### 4.5 类型安全消息发布/订阅（Artemis MQ）

**位置**：`aidsp-core.mq`
- `MessagePublisherService`：发送 Queue / Topic / 延迟 / 共享 / 持久订阅
- `DynamicSubscriberManager` + `TypedMessageHandler<T>`：泛型自动反序列化
- 每个订阅独立单线程，保证顺序消费

```java
// 发送
publisher.sendToQueue("agent.run.queue", agentRunDto);
publisher.sendToQueue("cache.evict.queue", event, 5000);   // 延迟 5s
publisher.sendToTopic("report.generated.topic", reportDto);

// 订阅
subscriberManager.subscribeQueue("agent.run.queue", AgentRunDto.class, this::handleRun);
private void handleRun(AgentRunDto run) { ... }
```

### 4.6 事件驱动缓存失效

**位置**：`aidsp-core.event`
- 写操作发布 Spring `ApplicationEvent`
- 多个 `@EventListener` 各自失效自己负责的缓存
- 业务代码不直接 `cacheManager.invalidate(...)`

```java
// 写操作
eventPublisher.publishEvent(new CompanyChangedEvent(companyId, op));

// 缓存失效监听
@EventListener
public void onCompanyChanged(CompanyChangedEvent e) {
    companyCache.invalidate(e.getCompanyId());
}
```

### 4.7 MyBatis-Plus 扩展

**位置**：`aidsp-core.mybatisplus`

| 扩展点 | 用途 |
|--------|------|
| `MyBaseMapper<T>` | 自定义基类 Mapper，扩展 `selectCacheByIds` 等 |
| `MetaObjectHandler` | `createTime` / `updateTime` / `createBy` 自动填充 |
| `@VersionControl` | 注解 + 拦截器，所有写操作走 `*OnVersion` 方法 |
| `@DataScope` | 数据权限拦截器，自动拼接权限 SQL |
| 关键字转义 | 兼容 MySQL / 达梦 / 人大金仓 |

---

## 5. 数据流

### 5.1 用户请求流（同步）

```
用户 ──HTTP──> Controller
                 │ (GlobalRestResponseAdvice 自动包装)
                 ▼
              Service (本模块)
                 │ (Dubbo in-JVM)
                 ▼
          其他模块 Service.api
                 │
                 ▼
            Repository
                 │
                 ▼
              MySQL
                 │
                 ▼
         RestResponse<T> 响应
```

### 5.2 报告生成数据流（异步）

```
1. Controller 提交任务 → AgentTaskService
2. AgentTaskService 发送 Artemis 消息 → agent.run.queue
3. AgentOrchestrator 消费
   a. PlannerAgent 拆解任务
   b. WorkerAgent × N 并行执行（检索知识、生成章节）
   c. ValidatorAgent 校验
4. 完成后发布 ReportGeneratedEvent（Spring Event + Artemis Topic）
5. 多 Handler 监听：
   - ReportService 持久化
   - 缓存失效
   - 通知前端（WebSocket / SSE）
6. 状态写回 agent_run + report
```

### 5.3 缓存失效流

```
Service 写操作完成
  → publishEvent(DomainChangedEvent)
       ├─> LocalEventListener（同进程内缓存失效）
       └─> Artemis Topic（广播给其他实例）
              └─> RemoteEventListener（其他节点缓存失效）
```

---

## 6. Agent 编排

### 6.1 三层角色（强制）

```
                    ┌──────────────┐
                    │   Planner    │
                    │   Agent      │
                    └──────┬───────┘
                           │ Plan(JSON)
                           ▼
                  ┌────────────────────┐
                  │   Orchestrator     │
                  └──────┬─────────────┘
       ┌─────────────────┼─────────────────┐
       ▼                 ▼                 ▼
  ┌─────────┐       ┌─────────┐       ┌─────────┐
  │ Worker  │       │ Worker  │       │ Worker  │
  │ Research│       │ Company │       │ Industry│
  └────┬────┘       └────┬────┘       └────┬────┘
       └─────────────────┼─────────────────┘
                         ▼
                  ┌──────────────┐
                  │  Validator   │
                  │   Agent      │
                  └──────┬───────┘
                         ▼
                  ┌──────────────┐
                  │   Report     │
                  │   Service    │
                  └──────────────┘
```

### 6.2 Agent 通信

- 全部使用 **结构化 JSON**
- 通信载体：`AgentContext` + `AgentResult` 强类型包装
- 所有 LLM / Tool 调用记录 `agent_run` 表

### 6.3 工具注册

工具统一在 `aidsp-agent` 模块的 `ToolRegistry` 注册（沿用 MBSE 的 `xxxStrategy` 约定 → `ToolRegistry` 自动扫描）。

---

## 7. 性能与可扩展性

### 7.1 性能目标

- 页面首屏 < 2s
- API 平均响应 < 500ms
- 报告生成 < 60s（含 AI 推理）

### 7.2 扩展点

- **水平扩展**：后端无状态，多实例
- **模型切换**：通过 Spring AI 切换 LLM Provider
- **知识库扩展**：Milvus 集群模式
- **模块拆分**：成熟后 Dubbo in-JVM 可平滑改为独立部署

---

## 8. 安全

- API 网关层 JWT 鉴权（Spring Security 过滤器链）
- 内部 Dubbo 调用走服务间 Token
- 敏感字段加密存储（API Key 等）
- 防止 Prompt 注入：输入清洗 + 角色隔离 + 高危指令黑名单
- 数据权限：`@DataScope` 自动注入 SQL

---

## 9. 快速复用指引

| 项目类型 | 优先复用模式 |
|----------|--------------|
| 普通 Web CRUD | 统一响应+异常、策略工厂、模块化结构 |
| 高并发读多写少 | + 分层缓存、事件驱动失效 |
| 多租户 / SaaS | + TTL 上下文传递（TenantContext） |
| 事件驱动 / 异步 | + 类型安全 Artemis MQ |

---

## 10. 修订记录

| 版本 | 日期 | 修订内容 |
|------|------|----------|
| 1.0  | 2026-07-06 | 初版架构 |
| 1.1  | 2026-07-06 | 沿用 MBSE Platform 模式重写：核心设施、模块分层、设计模式 |
