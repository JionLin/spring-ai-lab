## Why

当前 `WeatherTools` 在 `ChatController` 中以 `new WeatherTools()` 创建，无法注入 HTTP 客户端与配置；工具方法也未调用真实外部服务。需要按 **方案 A（仅对话内 `@Tool` 触发）** 落地可扩展形态：**多个外部 POST JSON 契约**、**统一出站客户端**、**配置化 URL 与密钥**、**超时与短错误回传**、**响应摘要**，便于后续逐个对接第三方字段差异。

## What Changes

- 将工具类改为 **Spring 管理的 Bean**（`@Component` 或 `@Configuration`/`@Bean`），由构造器或字段注入依赖。
- `ChatController`（或等价入口）**不得**再对 Tool 使用 `new`；改为注入 Bean 并交给 `ChatClient.tools(...)`。
- 引入 **统一 POST `application/json` 出站抽象**（小接口：区分 `requestType` 或等价枚举/常量 + 请求体 DTO/`Map`）及基于 **RestClient**（或同等级别官方 API）的实现，含 **连接/读超时**。
- **配置化**：每个外部契约的根 URL、路径片段、可选请求头（如 API Key）使用 `application.yml` / 环境变量绑定；**禁止**在 Java 源码中硬编码密钥与完整 URL。
- **多契约**：领域上允许多个 `@Tool` 方法或多个 Tool Bean；本变更至少提供 **两个** 不同请求体形态的演示 Tool（可指向同一可配置基座的不同 path，或两个配置前缀），以证明「一契约一 Tool」模式。
- **响应处理**：Tool 返回给模型的字符串 **SHALL** 由「关键字段提取」或「固定模板摘要」生成，**不得**将完整大 JSON 无裁剪回灌（除非长度已证明很小且规格允许）。
- **失败路径**：HTTP 失败、超时、序列化异常时返回 **短中文或英文错误描述**（无堆栈、无敏感头进模型回复）。

## Capabilities

### New Capabilities

- `ai-tools-http-post-json`：定义 Spring AI Tool 与统一 POST JSON 出站客户端、配置、超时、错误与响应摘要的行为要求。

### Modified Capabilities

- （无：不要求在本变更中修改 `tech-onboarding-docs` 的既有 Requirement 文本；文档同步可作为实现期任务在 `tasks.md` 中跟踪。）

## Impact

- **源码**：`ChatController`、`WeatherTools`（或拆分/新增 Tool 类）、新增 `client`/`config` 包或类；可能新增 `@ConfigurationProperties`。
- **依赖**：通常无需新 starter（`spring-boot-starter-web` 已带 **RestClient** 所需栈）；若选用 WebClient 则需额外依赖（本设计默认 **RestClient** 以避免扩大 BOM）。
- **配置**：新增自定义 `spring.*` 或 `lab.*` 配置前缀；示例使用占位符，本地通过环境变量注入密钥。
- **测试**：建议 **MockWebServer**（OkHttp test）或 Spring `MockRestServiceServer` 对出站调用做确定性测试，避免依赖公网。
