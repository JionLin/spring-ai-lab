package com.springailab.lab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Spring AI + DeepSeek（OpenAI 兼容）最小示例入口。
 *
 * @author jiaolin
 */
@SpringBootApplication
@MapperScan("com.springailab.lab.domain.user.mapper")
public class SpringAiLabApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // ── API Key 冲突修复 ──────────────────────────────────────────
        // Spring Boot 属性优先级：System.setProperty > OS 环境变量 > YAML
        // 当 qwen profile 激活时，SPRING_AI_OPENAI_API_KEY 环境变量会覆盖
        // application-qwen.yml 中通过 ${DASHSCOPE_API_KEY:} 设置的值，
        // 导致 DashScope 端点收到错误的 key（401）。
        // 修复：通过 System.setProperty 显式设置正确的 key（优先级最高）。
        resolveApiKeyConflict();

        SpringApplication app = new SpringApplication(SpringAiLabApplication.class);
        app.addListeners(event -> {
            if (event instanceof org.springframework.boot.context.event.ApplicationReadyEvent) {
                org.springframework.boot.context.event.ApplicationReadyEvent readyEvent =
                        (org.springframework.boot.context.event.ApplicationReadyEvent) event;
                Environment env = readyEvent.getApplicationContext().getEnvironment();
                String[] activeProfiles = env.getActiveProfiles();
                boolean usesQwen = Arrays.asList(activeProfiles).contains("qwen");
                boolean usesDeepSeek = Arrays.asList(activeProfiles).contains("deepseek");

                String dashscopeKey = env.getProperty("spring.ai.openai.api-key", "");
                boolean keyMissing = dashscopeKey == null || dashscopeKey.isBlank();

                if (usesQwen && keyMissing) {
                    System.err.println("\n❌ [配置错误] 当前激活了 qwen profile，但 DASHSCOPE_API_KEY 未设置或为空！");
                    System.err.println("   请设置环境变量：set DASHSCOPE_API_KEY=你的通义千问API密钥");
                    System.err.println("   或通过启动脚本：scripts/local-run-jdk17.ps1 -QwenKey <你的Key>\n");
                } else if (usesDeepSeek && keyMissing) {
                    System.err.println("\n❌ [配置错误] 当前激活了 deepseek profile，但 SPRING_AI_OPENAI_API_KEY 未设置或为空！");
                    System.err.println("   请设置环境变量：set SPRING_AI_OPENAI_API_KEY=你的DeepSeek API密钥\n");
                } else if (!keyMissing) {
                    System.out.println("✅ API Key 已配置，profile: " + String.join(",", activeProfiles));
                }
            }
        });

        System.out.println("当前SPRING_AI_OPENAI_API_KEY KEY: " + maskKey(System.getenv("SPRING_AI_OPENAI_API_KEY")));
        System.out.println("当前 DASHSCOPE_API_KEY KEY: " + maskKey(System.getenv("DASHSCOPE_API_KEY")));

        app.run(args);
    }

    private static String maskKey(String key) {
        if (key == null || key.isBlank()) {
            return "<未设置>";
        }
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    /**
     * 解决 API Key 环境变量冲突。
     *
     * <p>问题：当用户同时设置了 {@code SPRING_AI_OPENAI_API_KEY}（DeepSeek 用）
     * 和 {@code DASHSCOPE_API_KEY}（通义千问用）两个环境变量时，
     * Spring Boot 的属性解析优先级为：
     * <pre>
     *   OS 环境变量 (SPRING_AI_OPENAI_API_KEY → spring.ai.openai.api-key)
     *   > profile YAML (application-qwen.yml 中 ${DASHSCOPE_API_KEY:})
     * </pre>
     * 导致 qwen profile 下实际发送的是 DeepSeek 的 key，DashScope 返回 401。
     *
     * <p>修复：通过 {@code System.setProperty} 设置正确的 key，
     * 因为 Java System properties 优先级高于 OS 环境变量。
     */
    private static void resolveApiKeyConflict() {
        String profiles = System.getProperty("spring.profiles.active",
                System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "qwen"));
        boolean usesQwen = Arrays.stream(profiles.split(","))
                .map(String::trim)
                .anyMatch(p -> p.equals("qwen"));
        boolean usesDeepSeek = Arrays.stream(profiles.split(","))
                .map(String::trim)
                .anyMatch(p -> p.equals("deepseek"));

        String dashscopeKey = System.getenv("DASHSCOPE_API_KEY");
        String openaiEnvKey = System.getenv("SPRING_AI_OPENAI_API_KEY");

        if (usesQwen && openaiEnvKey != null && !openaiEnvKey.isBlank()
                && dashscopeKey != null && !dashscopeKey.isBlank()) {
            // SPRING_AI_OPENAI_API_KEY 环境变量会覆盖 YAML 中的 ${DASHSCOPE_API_KEY:}
            // 用 System.setProperty 强制使用 DashScope key（优先级更高）
            System.setProperty("spring.ai.openai.api-key", dashscopeKey);
            System.out.println("⚠️  检测到 SPRING_AI_OPENAI_API_KEY 与 DASHSCOPE_API_KEY 同时存在，"
                    + "已强制使用 DASHSCOPE_API_KEY（qwen profile）");
        }

        if (usesDeepSeek && dashscopeKey != null && !dashscopeKey.isBlank()
                && openaiEnvKey != null && !openaiEnvKey.isBlank()) {
            // 反向场景：deepseek profile 下确保使用 SPRING_AI_OPENAI_API_KEY
            System.setProperty("spring.ai.openai.api-key", openaiEnvKey);
            System.out.println("⚠️  检测到 DASHSCOPE_API_KEY 与 SPRING_AI_OPENAI_API_KEY 同时存在，"
                    + "已强制使用 SPRING_AI_OPENAI_API_KEY（deepseek profile）");
        }
    }
}
