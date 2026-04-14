package com.springailab.lab.web;

import com.springailab.lab.tools.WeatherTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 最小对话 HTTP 入口，触发 DeepSeek + 工具调用链路。
 *
 * @author jiaolin
 */
@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;

    private final WeatherTools weatherTools;

    /**
     * @param chatModel    由 Spring AI 自动配置的 Chat 模型
     * @param weatherTools 由容器注入的 Tool Bean（含多个 {@code @Tool} 方法）
     */
    public ChatController(ChatModel chatModel, WeatherTools weatherTools) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.weatherTools = weatherTools;
    }

    /**
     * 发送用户消息，由模型决定是否调用 {@link WeatherTools} 中的工具。
     *
     * @param request 请求体
     * @return 模型最终文本回复
     */
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        log.info("Chat request received, messageLength={},request={}",
                request.message() == null ? 0 : request.message().length(), request);
        try {
            String reply = this.chatClient.prompt()
                    .user(request.message())
                    .tools(this.weatherTools)
                    .call()
                    .content();
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (NonTransientAiException ex) {
            String msg = ex.getMessage();
            log.error("AI 调用失败（不可重试）: {}", msg, ex);
            if (msg != null && msg.contains("401")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "API Key 无效或未配置，请检查环境变量 DASHSCOPE_API_KEY 是否正确设置。",
                                "detail", "https://help.aliyun.com/zh/model-studio/error-code#apikey-error"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI 服务调用失败", "detail", msg));
        }
    }

    /**
     * 聊天请求。
     *
     * @param message 用户自然语言
     */
    public record ChatRequest(String message) {
    }
}
