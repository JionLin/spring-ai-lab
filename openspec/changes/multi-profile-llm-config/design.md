## Context

- 项目使用 Spring Boot 3.4 + Spring AI `spring-ai-starter-model-openai`，通过 `spring.ai.openai.base-url`、`api-key`、`chat.options.model` 对接 **OpenAI 兼容** HTTP API。
- 已存在 `lab.external.post` 等与 LLM 无关的配置，应保留在根 `application.yml` 或共享文件中。

## Goals / Non-Goals

**Goals:**

- 使用 **`application-deepseek.yml`** 与 **`application-qwen.yml`**（命名可微调，但须两个厂商维度）分别承载 DeepSeek 与阿里云 DashScope **兼容模式** 的 `spring.ai.openai` 配置块。
- 密钥 **仅** 来自环境变量占位（如 `${SPRING_AI_OPENAI_API_KEY}` / `${DASHSCOPE_API_KEY}`），仓库内不出现真实密钥。
- `qwen` profile 中 `base-url` 与官方 **OpenAI 兼容** 地域端点一致（例如中国北京：`https://dashscope.aliyuncs.com/compatible-mode/v1`）；`model` 使用占位或文档推荐值并注释「以控制台为准」。
- 文档说明：`SPRING_PROFILES_ACTIVE=deepseek` 或 `qwen,test` 等组合方式。

**Non-Goals:**

- 不在本变更引入第二套 `ChatModel` Bean 或运行时动态切换 UI（仅配置拆分）。
- 不在本变更引入 DashScope **原生** Java SDK（仍走 OpenAI 兼容 HTTP）。

## Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| 文件命名 | `application-deepseek.yml`、`application-qwen.yml` | 与对话中「多 profile」表述一致、见名知义 |
| 默认 profile | 推荐默认 `spring.profiles.active: deepseek` 写在根 yml，降低 **BREAKING**；若保持无默认则必须在 README/TECH_STACK 强调必设 | 平衡本地「克隆即跑」与安全 |
| test 与厂商 profile | `application-test.yml` 使用 `spring.profiles.include: deepseek`（或等价）保证 `mvn test` 只拉取一套 LLM 占位配置 | 避免 test 与 qwen 重复维护两套 key |
| 模型名 | `qwen` profile 使用 `qwen-plus` 或 `qwen-plus-latest` 作示例，并注释「以 Model Studio 为准」 | 用户口述的 `qwen3.5-plus` 可能与控制台 ID 不一致 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| profile 拼写错误导致启动缺配置 | `TECH_STACK` 列出合法 profile 名与示例命令 |
| 地域与密钥不匹配 | 在 `application-qwen.yml` 顶部注释链到阿里云官方兼容说明链接 |

## Migration Plan

- 已有本地 `application.yml` 自定义：合并到对应 profile 文件；根文件只保留共享项。
- 启动命令增加环境变量：`SPRING_PROFILES_ACTIVE=qwen`（示例）。

## Open Questions

- 是否再增加 `application-openai.yml`（真 OpenAI）作为第三 profile（可后续 change）。
