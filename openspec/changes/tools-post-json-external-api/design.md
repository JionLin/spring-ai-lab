## Context

- 入口仍为 `POST /chat` + `ChatClient` + `@Tool`（方案 A）。
- 现状：`new WeatherTools()`、演示返回、无 HTTP、无注入。

## Goals / Non-Goals

**Goals:**

- Tool 为 Spring Bean，可注入统一出站客户端与配置。
- 所有外部调用 **HTTP POST**，`Content-Type: application/json`，请求体为 JSON 序列化结果。
- 统一客户端接口 + RestClient 实现、超时、失败时短错误文案。
- 至少两个 Tool 入口，体现「一外部契约一 Tool（或一方法）」。
- 测试不依赖公网（MockWebServer 或 MockRestServiceServer）。

**Non-Goals:**

- 不在本变更引入 MySQL、规则路由（B）、或新的对外 REST 业务 Controller（仅 Tool 路径）。
- 不实现完整 resilience4j 熔断（可在 `Open Questions` 后续做）。
- 不替用户对接某一固定商业 API（仅通用客户端 + 可配置 URL + 演示契约）。

## Decisions

| 决策 | 选择 | 备选 | 理由 |
|------|------|------|------|
| HTTP API | **RestClient**（Spring 6 / Boot 3） | WebClient、Feign | 与 `starter-web` 一致、阻塞式简单、易测 |
| 请求抽象 | 接口如 `ExternalJsonPostClient`：`post(String requestType, Object body)` 或 `(String profileKey, Map<String,Object> body)`，由实现根据 `requestType` 解析 **配置** 得到完整 URL 与默认头 | 每 Tool 内各写 RestClient | 统一超时、日志脱敏、错误包装 |
| 配置模型 | `@ConfigurationProperties` 前缀如 `lab.external.post`：多 **命名 profile**（如 `weather`、`demoEcho`），每项含 `base-url`、`path`、可选 `api-key-header-name` + `api-key`（仅绑定配置，不写死在代码） | 全硬编码 path | 多第三方扩展时加配置项即可 |
| Tool 形态 | 两个 `@Component` 类或一个类两个 `@Tool` 方法；本设计倾向 **一个类两个 `@Tool` 方法** 共享同一 Client Bean，减少 Spring 扫描面 | 多类 | 满足「多契约」且改动集中 |
| 响应摘要 | 各 Tool 内私有方法：从 `JsonNode` 或 Map 取 1～3 个关键字段拼模板；若响应非 JSON 则截断前 N 字符 | 全文返回 | 控制 token、避免泄露大 body |
| 错误文案 | `catch` RestClient 异常 / 超时 → 返回固定前缀 + 简短原因（如 `外部服务暂时不可用`），**禁止** `e.toString()` 进模型 | 透传堆栈 | 安全与体验 |
| 测试 | **MockWebServer**（需 `okhttp` test 依赖）或 Spring `MockRestServiceServer` | httpbin.org | CI 稳定 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| `requestType` 字符串拼错 | 使用枚举或常量类集中管理 profile 名 |
| 密钥进日志 | Client 内 debug 仅打 URL 与 status，不打 Authorization 与 body |
| 模型误召 Tool | 靠 `@Tool`/`@ToolParam` 描述与后续 prompt 调优（本变更不展开） |

## Migration Plan

- 本地：在 `application.yml` 或 env 中增加 `lab.external.post.*`；删除或迁移仓库中已提交的明文 AI Key（另任务，可与本变更同 PR 或后续安全变更）。

## Open Questions

- 是否引入 **Resilience4j**（重试/熔断）作为后续 change。
- 第二个演示 Tool 的具体业务语义（可与第一个共用 Mock 不同 path）。
