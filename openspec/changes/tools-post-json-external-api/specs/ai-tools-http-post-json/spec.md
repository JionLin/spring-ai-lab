## ADDED Requirements

### Requirement: Tool 类由 Spring 容器管理

所有对外部系统发起 HTTP 调用的 Spring AI `@Tool` 所在类 SHALL 注册为 Spring Bean（例如 `@Component` 或在 `@Configuration` 中通过 `@Bean` 声明），SHALL 通过构造器注入或字段注入获取其依赖（例如统一 JSON POST 客户端、配置属性对象）。

#### Scenario: 无 new Tool 反模式

- **WHEN** 审查者检查 `ChatController`（或唯一对话入口类）中对 `ChatClient.tools(...)` 的调用
- **THEN** 其 SHALL NOT 发现对 Tool 实现类使用 `new` 进行实例化以参与对话链路

### Requirement: 对话入口使用注入的 Tool Bean

`ChatController`（或项目中对 `ChatClient` 绑定 Tool 的唯一生产入口）SHALL 通过 Spring 注入获取 Tool Bean，并将该 Bean 实例传递给 `ChatClient` 的 tools API，以使 Tool 方法执行时具备完整依赖（含 HTTP 客户端）。

#### Scenario: 构造器可注入 Tool

- **WHEN** 应用上下文启动且该 Controller Bean 被创建
- **THEN** 其 SHALL 成功解析对 Tool Bean 的依赖且应用 SHALL 无因缺失 Tool Bean 导致的启动失败

### Requirement: 统一 POST application/json 出站客户端

系统 SHALL 提供可注入的出站组件，用于向外部 HTTP 资源发送 **POST** 请求且 **Content-Type 为 application/json**，请求体 SHALL 由 Java 对象或 `Map` 经 JSON 序列化得到。该组件 SHALL 支持为不同逻辑调用方区分 **请求类型或配置 profile**（例如枚举、常量键或字符串键），并根据该键从配置中解析目标 **base-url** 与 **path**（或等价的完整 URL 拼装规则）。

该组件 SHALL 为每次调用配置 **连接超时** 与 **读超时**（具体数值由实现决定但必须在 `design.md` 或配置中可查且为有限值）。

#### Scenario: POST JSON 成功

- **WHEN** 出站组件被调用且远端返回 2xx 且 body 为合法 JSON
- **THEN** 调用方 SHALL 能获得用于后续摘要的解析结果（例如字符串 body 或树模型），且请求发送过程中 SHALL 使用 POST 方法与 JSON 请求体

#### Scenario: 超时或网络失败

- **WHEN** 远端在配置的超时时间内无响应或连接失败
- **THEN** 出站组件 SHALL 以受检异常或统一结果类型向调用方表达失败，且 SHALL NOT 向最终用户模型回复中泄露内部堆栈 trace

### Requirement: URL 与密钥仅来自配置

外部服务的 **scheme/host/port/path** 以及可选的 **API Key 或等价凭据** SHALL 仅通过 Spring Boot 配置属性或环境变量绑定注入；Java 源码中 SHALL NOT 包含硬编码的生产 URL 或硬编码密钥常量。

#### Scenario: 密钥不进源码检索

- **WHEN** 审查者对 `src/main/java` 执行敏感字符串检索（例如与真实密钥同形态的占位除外）
- **THEN** 其 SHALL NOT 发现与具体第三方生产凭据等价的硬编码值

### Requirement: 多个 Tool 入口映射多个 JSON 契约

系统 SHALL 至少暴露 **两个** 带 `@Tool` 注解的方法（可位于同一 Bean 或不同 Bean），且二者对外部 POST JSON 的 **请求体字段集合或 path/profile** 所代表的契约 SHALL 彼此可区分，以体现「不同第三方字段差异 → 不同 Tool 映射」的模式。

#### Scenario: 两个 Tool 均可被模型调度

- **WHEN** Spring AI 在运行时注册上述 Tool 定义
- **THEN** 模型 SHALL 至少具备两个可区分的 tool 名称或描述入口（两个 `@Tool` 方法）

### Requirement: Tool 返回内容须为摘要或关键字段

`@Tool` 方法的返回值（即将回灌对话的字符串）SHALL 为基于响应 JSON 的 **关键字段提取** 或 **固定模板摘要**；当响应体超过合理长度时，SHALL 截断或省略细节，SHALL NOT 无约束地将完整原始 JSON 作为默认返回策略。

#### Scenario: 大响应被裁剪

- **WHEN** 远端返回的 JSON 字符数明显超过单条工具回复的合理展示长度（由实现选定阈值并在代码注释或配置中说明）
- **THEN** 返回给模型的字符串 SHALL 仍为有限长度且语义上为摘要或截断提示

### Requirement: 失败时短错误描述

当出站 HTTP 返回非 2xx、超时、或响应无法按预期解析时，`@Tool` 方法返回给模型的文本 SHALL 为简短错误描述（建议不超过若干十字/词），SHALL NOT 包含异常类全名与堆栈；SHALL NOT 包含 Authorization 头或完整请求体。

#### Scenario: 4xx 不泄露敏感细节

- **WHEN** 远端返回 401 或 403
- **THEN** 工具返回字符串 SHALL 为泛化错误提示且不包含所配置的密钥原文
