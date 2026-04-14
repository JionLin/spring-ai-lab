# AI调用链入门指南

你已经熟悉 Spring Boot，这份文档只讲你最需要补齐的 AI 部分：**模型怎么接入、Tool 怎么被模型调用、外部 API 怎么进链路、如何兼容多模型**。

## 1. 先建立一个“AI 调用链”心智模型

在这个项目里，完整链路是：

1. 前端/调用方请求 `POST /chat`
2. `ChatController` 把用户输入交给 `ChatClient`
3. `ChatClient` 把 `WeatherTools`（`@Tool` 方法）注册给模型
4. 模型决定是否调用某个 Tool
5. Tool 内部通过统一客户端 `ExternalJsonPostClient` 调外部 POST JSON API
6. Tool 把外部响应“摘要化”后回传给模型
7. 模型输出最终 `reply`

本质上你可以把它理解为：  
**Spring MVC Controller +（LLM 作为调度器）+ 普通 Java Service/Client 调外部接口**。

---

## 2. 对应你项目里的关键代码位置

## 2.1 对话入口（和普通 Controller 很像）

文件：`src/main/java/com/springailab/lab/web/ChatController.java`

- `ChatModel` 由 Spring AI 自动装配
- `ChatClient.builder(chatModel).build()` 组装会话客户端
- `.tools(this.weatherTools)` 把 Tool Bean 暴露给模型

你只要记住：  
**Controller 不负责 AI 推理细节，只负责收请求、调 ChatClient、回响应**。

## 2.2 Tool 层（模型可调用的方法）

文件：`src/main/java/com/springailab/lab/tools/WeatherTools.java`

- `@Component`：Tool 现在是 Spring Bean，可注入依赖
- `@Tool`：声明模型可调用的方法
- `getWeather(...)`、`lookupDemoLine(...)`：两个不同契约入口
- 失败统一返回短文案（不把堆栈回给模型）
- 成功后做“关键字段摘要”，避免把大 JSON 全量塞回模型

你可以把 `@Tool` 当成“模型可调用的 Service API”。

## 2.3 外部 API 统一出口（你最熟悉的客户端模式）

文件：

- `src/main/java/com/springailab/lab/external/ExternalJsonPostClient.java`
- `src/main/java/com/springailab/lab/external/RestClientExternalJsonPostClient.java`
- `src/main/java/com/springailab/lab/external/ExternalJsonPostConfiguration.java`

设计点：

- 统一方法：`postJson(profileKey, body)`
- 固定协议：POST + `application/json`
- 配置驱动 endpoint（`profileKey -> baseUrl/path`）
- 超时、HTTP 错误、网络异常统一映射到 `ExternalPostException`

这和你平时做“第三方网关 SDK 封装层”是同一套路。

---

## 3. 配置层：多模型兼容（当前项目已支持 profile 切换）

### 3.1 共享配置

文件：`src/main/resources/application.yml`

- 只保留共享项（`spring.application.*`、`lab.external.post.*`）
- 当前默认：`spring.profiles.active: qwen`

### 3.2 DeepSeek 配置

文件：`src/main/resources/application-deepseek.yml`

- `spring.ai.openai.base-url: https://api.deepseek.com`
- `model: deepseek-chat`
- `api-key: ${SPRING_AI_OPENAI_API_KEY:}`

### 3.3 Qwen（DashScope OpenAI 兼容）配置

文件：`src/main/resources/application-qwen.yml`

- `base-url: https://dashscope.aliyuncs.com/compatible-mode/v1`
- `model: qwen3.5-plus`（以控制台实际模型名为准）
- `api-key: ${DASHSCOPE_API_KEY:}`

### 3.4 如何切换模型

- 环境变量：`SPRING_PROFILES_ACTIVE=deepseek` 或 `qwen`
- 或 JVM 参数：`--spring.profiles.active=deepseek`

注意：你的代码层（Controller/Tool）不需要随切换改动，核心改动在配置层。

---

## 4. 你作为 Java 工程师最该关注的 5 个工程点

1. **密钥管理**：必须走环境变量，禁止明文入库
2. **异常与降级**：Tool 对外返回短错误，别把堆栈暴露给模型
3. **摘要策略**：对外部响应只取关键字段，控制 token 成本
4. **可测试性**：用 MockWebServer 测出站调用，不依赖公网
5. **职责边界**：Controller 薄、Tool 编排、External 层负责 HTTP 细节

---

## 5. 常见误区（AI 初学者高频）

1. 误区：模型会“自动知道怎么调我的 API”  
   现实：必须把能力显式暴露成 `@Tool`，并描述参数语义

2. 误区：把外部接口原始 JSON 全部返回给模型  
   现实：应做摘要/截断，不然 token 和延迟都会爆

3. 误区：把业务流程写死在 Prompt  
   现实：Prompt 管行为倾向，稳定流程仍靠 Java 代码和配置

4. 误区：切模型要大改代码  
   现实：OpenAI 兼容生态里，通常先改 `base-url/api-key/model` 即可

---

## 6. 下一步学习建议（按难度递进）

1. 先新增第 3 个 `@Tool`（例如查询订单摘要），沿用 `ExternalJsonPostClient`
2. 给 Tool 增加参数校验与输入长度限制
3. 增加统一 traceId 日志，串联 `Controller -> Tool -> 外部 API`
4. 再考虑引入重试/熔断（如 Resilience4j）

这样你会从“会调用模型”升级到“会做可上线的 AI 服务”。

---

## 7. 本地联调清单（含 Postman 样例）

下面按“最短路径可跑通”给你一个实操步骤。

### 7.1 启动前检查

1. JDK 版本确认是 17（本项目 Spring Boot 3.4 需要）
2. 选模型 profile：
   - DeepSeek：`SPRING_PROFILES_ACTIVE=deepseek`
   - Qwen：`SPRING_PROFILES_ACTIVE=qwen`
3. 设置对应密钥（Windows PowerShell 示例）：

```powershell
$env:SPRING_PROFILES_ACTIVE="qwen"
$env:DASHSCOPE_API_KEY="你的key"
# 如果切 deepseek，则改为
# $env:SPRING_PROFILES_ACTIVE="deepseek"
# $env:SPRING_AI_OPENAI_API_KEY="你的key"
```

> 注意：你项目里 `application.yml` 目前默认是 `qwen`，如果你不设 profile，启动会按 qwen 走。

### 7.2 启动应用

```powershell
mvn spring-boot:run
```

看到 `Tomcat started on port(s): 8080` 或类似日志即表示启动成功。

### 7.3 用 Postman 调 `/chat`

- Method: `POST`
- URL: `http://localhost:8080/chat`
- Header: `Content-Type: application/json`
- Body（raw / JSON）：

```json
{
  "message": "帮我查一下杭州天气"
}
```

如果模型判断需要调用 Tool，会走 `WeatherTools.getWeather(...)`，然后由 `ExternalJsonPostClient` 发起外部 POST。

### 7.4 第二个 Tool 的触发样例

```json
{
  "message": "请帮我查询 order-123 的演示信息"
}
```

模型更可能触发 `lookupDemoLine(...)`，用于验证“多 Tool / 多契约”是否工作。

### 7.5 你当前代码下的预期现象（重要）

`lab.external.post.endpoints` 在 `application.yml` 配的是 `127.0.0.1:1`（占位地址），所以默认会走到：

- `外部服务连接超时或不可用`
- 或 `外部服务暂时不可用`

这是**符合预期**的。要联调真实结果，你需要把：

- `lab.external.post.endpoints.weather.base-url/path`
- `lab.external.post.endpoints.demo-echo.base-url/path`

改成真实可访问的测试服务地址（或本地 mock 服务地址）。

### 7.6 快速排障清单

1. 返回 401/403：先查 key 是否匹配当前 profile 对应厂商
2. 返回调用失败：检查 `base-url`、`path`、网络可达性
3. 模型不调用 Tool：看你的 `message` 是否明确表达了工具语义；必要时加强 `@Tool(description=...)`
4. 启动失败：优先检查 JDK 版本与环境变量是否生效

### 7.7 最小验收标准（你可以当 checklist）

- [ ] `POST /chat` 能返回 `reply`
- [ ] 能触发至少一个 `@Tool`（看日志 `Tool getWeather invoked...`）
- [ ] 错误时返回短文案，不暴露堆栈
- [ ] 切换 `deepseek/qwen` 只改 profile 与 key，不改 Java 代码
