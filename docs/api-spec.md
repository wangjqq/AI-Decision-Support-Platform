# API 规范 (API Specification)

> 本文件定义前后端 API 契约。
> 沿用 **MBSE Platform** 模式：Controller 直返业务对象，由 `GlobalRestResponseAdvice` 自动包装为 `RestResponse<T>`。
> 所有 Controller 必须遵循本规范。

---

## 1. REST 规范

### 1.1 基本约定

| 项 | 规范 |
|----|------|
| Base Path | `/api/v1` |
| Content-Type | `application/json; charset=utf-8` |
| 字符编码 | UTF-8 |
| 时间格式 | ISO-8601：`2026-07-06T14:30:00+08:00` |
| ID 类型 | Long（雪花算法） |
| 鉴权 | `Authorization: Bearer <jwt>` |
| 分页 | 见 §1.4 |

### 1.2 HTTP 方法与路径

| 方法 | 用途 | 示例 |
|------|------|------|
| GET | 查询 | `GET /api/v1/companies/{id}` |
| POST | 创建 | `POST /api/v1/companies` |
| PUT | 全量更新 | `PUT /api/v1/companies/{id}` |
| PATCH | 局部更新 | `PATCH /api/v1/companies/{id}` |
| DELETE | 删除 | `DELETE /api/v1/companies/{id}` |

- 资源名**复数**
- 路径**全小写**，多个单词用 `-` 分隔：`/api/v1/industry-reports`
- 禁止动词出现在路径中（用 HTTP 方法表达）

### 1.3 资源命名

| 模块 | 路径前缀 |
|------|----------|
| 公司 | `/api/v1/companies` |
| 行业 | `/api/v1/industries` |
| 报告 | `/api/v1/reports` |
| Agent | `/api/v1/agents` |
| 知识库 | `/api/v1/knowledge` |
| 系统 | `/api/v1/sys/users`、`/api/v1/sys/roles` |

### 1.4 分页参数

```
GET /api/v1/companies?page=1&size=20&sort=createdAt,desc
```

- `page`：从 1 开始
- `size`：默认 20，最大 100
- `sort`：字段 + 方向，可多个

### 1.5 过滤与搜索

- 使用 query string：`?industry=tech&keyword=AI`
- 复杂查询使用 `POST /search` 端点 + 请求体

---

## 2. 统一返回结构 `RestResponse<T>`

### 2.1 定义（位于 `aidsp-core.dto.RestResponse`）

```java
@Data
public class RestResponse<T> {
    private int     code;       // 业务状态码
    private String  msg;        // 提示信息
    private T       data;       // 业务数据
    private String  traceId;    // 链路追踪 ID
    private long    timestamp;  // 服务器时间戳
}
```

### 2.2 Controller 不手动包装

```java
@GetMapping("/company/{id}")
public CompanyVO getCompany(@PathVariable Long id) {
    return companyService.getById(id);   // 由 GlobalRestResponseAdvice 自动包装
}
```

### 2.3 成功响应

```json
{
  "code": 0,
  "msg": "success",
  "data": { ... },
  "traceId": "abc123",
  "timestamp": 1751798400000
}
```

### 2.4 失败响应

```json
{
  "code": 10004,
  "msg": "公司不存在",
  "data": null,
  "traceId": "abc123",
  "timestamp": 1751798400000
}
```

### 2.5 业务状态码

| 范围 | 含义 |
|------|------|
| 0    | 成功 |
| 1xxx | 通用错误（参数、权限、系统） |
| 2xxx | 公司模块 |
| 3xxx | 行业模块 |
| 4xxx | 报告模块 |
| 5xxx | Agent / AI 错误 |
| 6xxx | 知识库 / RAG 错误 |
| 9xxx | 第三方服务错误 |

#### 通用错误码

| Code | Message |
|------|---------|
| 0    | success |
| 1000 | 系统繁忙 |
| 1001 | 参数校验失败 |
| 1002 | 未授权 |
| 1003 | 无权限 |
| 1004 | 资源不存在 |
| 1005 | 资源冲突 |

### 2.6 分页结果

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [ ... ],
    "total": 132,
    "page": 1,
    "size": 20,
    "pages": 7
  },
  "traceId": "...",
  "timestamp": ...
}
```

---

## 3. 全局包装与异常处理（沿用 MBSE）

### 3.1 `GlobalRestResponseAdvice`

`@RestControllerAdvice` 自动将 Controller 返回值包装为 `RestResponse<T>`：

- 自动排除：
  - `ResponseEntity<T>`（文件下载）
  - Knife4j / Swagger 端点
  - OAuth 回调端点
  - `RestResponse<?>` 已是包装结果
  - `String`（避免误转）

```java
@RestControllerAdvice(basePackages = "com.aidsp.platform")
public class GlobalRestResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 排除特殊端点
    }
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof RestResponse) return body;
        return RestResponse.success(body);
    }
}
```

### 3.2 `GlobalExceptionAdvice`

统一处理异常 → 业务码：

| 异常 | 业务码 |
|------|--------|
| `BusinessException` | 自定义（构造时传入） |
| `MethodArgumentNotValidException` / `ConstraintViolationException` | 1001 |
| `UnauthorizedException` | 1002 |
| `AccessDeniedException` | 1003 |
| `ResourceNotFoundException` | 1004 |
| `DuplicateResourceException` | 1005 |
| `Throwable`（兜底） | 1000 |

```java
@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler(BusinessException.class)
    public RestResponse<Void> handleBiz(BusinessException e) {
        return RestResponse.fail(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        return RestResponse.fail(1001, e.getBindingResult().getFieldError().getDefaultMessage());
    }
    // ...
}
```

### 3.3 业务异常

```java
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(ErrorCode ec)           { super(ec.getMsg()); this.code = ec.getCode(); }
    public BusinessException(ErrorCode ec, String m) { super(m);           this.code = ec.getCode(); }
}

// 使用
throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND, "公司不存在: " + id);
```

---

## 4. DTO / VO 规则

### 4.1 命名

| 类型 | 用途 | 后缀 |
|------|------|------|
| Request Body | 接收前端入参 | `XxxRequest` |
| Response Body | 返回给前端的展示数据 | `XxxVO` |
| 内部传输对象 | 模块间（Dubbo）传递 | `XxxDTO` |
| 数据库对象 | MyBatis-Plus Entity | `XxxPO` / `XxxDO` |
| 查询条件 | Query 封装 | `XxxQuery` |

### 4.2 转换原则

- Controller 接收 `Request` → 转为 `DTO` → 调 Service（Dubbo）
- Service 返回 `DTO` → Controller 转为 `VO` → 包装（由 Advice 完成）
- **禁止 Entity 直接出 Controller**
- **禁止 Entity 直接接收前端参数**
- 跨模块数据传输**只用 DTO**，不传 Entity

### 4.3 字段规范

- 使用 `@JsonProperty` 处理字段名映射
- 时间字段统一 `Long` 时间戳 或 `String` ISO-8601
- 枚举字段用 `Integer` 编码
- ID 字段：`Long id`

### 4.4 校验

- 使用 `jakarta.validation` 注解
- Controller 方法参数加 `@Valid`
- 嵌套对象用 `@Valid` 级联校验

```java
public class CompanyCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Pattern(regexp = "^\\d{18}$")
    private String uscc;

    @NotNull
    @Min(1)
    private Long industryId;
}
```

---

## 5. 接口示例

### 5.1 创建公司

**Request**
```
POST /api/v1/companies
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "name": "示例科技有限公司",
  "uscc": "91110000123456789X",
  "industryId": 12,
  "mainBusiness": "AI 芯片研发"
}
```

**Response**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 100001,
    "name": "示例科技有限公司",
    "uscc": "91110000123456789X",
    "industryId": 12,
    "createdAt": "2026-07-06T14:30:00+08:00"
  },
  "traceId": "...",
  "timestamp": 1751798400000
}
```

### 5.2 异步报告生成

**Request**
```
POST /api/v1/reports/generate
```

**Response（立即返回 taskId）**
```json
{
  "code": 0,
  "msg": "success",
  "data": { "taskId": "T202607061430001" },
  "traceId": "...",
  "timestamp": ...
}
```

**查询**
```
GET /api/v1/reports/tasks/T202607061430001
```

返回任务状态：`PENDING / RUNNING / SUCCESS / FAILED` 及进度。

---

## 6. 版本管理

- URI 版本：`/api/v1`
- 破坏性变更必须升级主版本
- 老版本至少保留 3 个月

---

## 7. 修订记录

| 版本 | 日期 | 修订内容 |
|------|------|----------|
| 1.0  | 2026-07-06 | 初版 API 规范 |
| 1.1  | 2026-07-06 | 沿用 MBSE 模式：`Result<T>` → `RestResponse<T>`，新增 `GlobalRestResponseAdvice` / `GlobalExceptionAdvice` |
