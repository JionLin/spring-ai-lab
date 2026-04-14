package com.springailab.lab.domain.chat.service;

import com.springailab.lab.domain.chat.config.ChatCostProperties;
import com.springailab.lab.tools.WeatherTools;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 对话编排服务（同步 + SSE + 会话记忆 + 指标）。
 *
 * @author jiaolin
 */
@Service
public class ChatOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrator.class);

    private static final Duration STREAM_TIMEOUT = Duration.ofMinutes(5);

    private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000L);

    private final ChatClient chatClient;

    private final WeatherTools weatherTools;

    private final ConversationMessageStore conversationMessageStore;

    private final MeterRegistry meterRegistry;

    private final ChatCostProperties chatCostProperties;

    public ChatOrchestrator(ChatModel chatModel,
            WeatherTools weatherTools,
            ConversationMessageStore conversationMessageStore,
            MeterRegistry meterRegistry,
            ChatCostProperties chatCostProperties) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.weatherTools = weatherTools;
        this.conversationMessageStore = conversationMessageStore;
        this.meterRegistry = meterRegistry;
        this.chatCostProperties = chatCostProperties;
    }

    /**
     * 同步对话。
     *
     * @param message 用户消息
     * @param conversationId 会话ID
     * @return HTTP 响应
     */
    public ResponseEntity<Map<String, String>> chat(String message, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        String normalizedMessage = normalizeMessage(message);
        try {
            ChatResponse response = this.chatClient.prompt()
                    .messages(buildPromptMessages(normalizedConversationId, normalizedMessage))
                    .tools(this.weatherTools)
                    .call()
                    .chatResponse();
            String content = extractContent(response);
            appendConversation(normalizedConversationId, normalizedMessage, content);
            collectEstimatedCostFromUsage(response, "chat", "false");
            return ResponseEntity.ok(Map.of("reply", content));
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
     * SSE 流式对话。
     *
     * @param message 用户消息
     * @param conversationId 会话ID
     * @return SSE 发射器
     */
    public SseEmitter streamChat(String message, String conversationId) {
        String normalizedConversationId = normalizeConversationId(conversationId);
        String normalizedMessage = normalizeMessage(message);
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT.toMillis());
        Timer.Sample sample = Timer.start(this.meterRegistry);
        Counter.builder("chat_stream_requests_total")
                .register(this.meterRegistry)
                .increment();

        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicReference<Usage> latestUsage = new AtomicReference<>();
        StringBuilder streamedContent = new StringBuilder();
        Flux<ChatResponse> responseFlux = this.chatClient.prompt()
                .messages(buildPromptMessages(normalizedConversationId, normalizedMessage))
                .tools(this.weatherTools)
                .stream()
                .chatResponse();
        Disposable disposable = responseFlux.subscribe(chatResponse -> {
            Usage usage = extractUsage(chatResponse);
            if (usage != null) {
                latestUsage.set(usage);
            }
            String token = extractContent(chatResponse);
            if (!StringUtils.hasText(token)) {
                return;
            }
            streamedContent.append(token);
            sendEvent(emitter, "token", new ChatEventPayload(normalizedConversationId, token));
        }, throwable -> {
            Counter.builder("chat_stream_errors_total")
                    .register(this.meterRegistry)
                    .increment();
            sendEvent(emitter, "error", new ChatEventPayload(normalizedConversationId, "AI 服务调用失败"));
            finalizeStream(finished, sample, latestUsage.get(), "true");
            emitter.completeWithError(throwable);
        }, () -> {
            sendEvent(emitter, "done", new ChatEventPayload(normalizedConversationId, "completed"));
            appendConversation(normalizedConversationId, normalizedMessage, streamedContent.toString());
            finalizeStream(finished, sample, latestUsage.get(), "true");
            emitter.complete();
        });

        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(() -> {
            disposable.dispose();
            if (finished.compareAndSet(false, true)) {
                Counter.builder("chat_stream_errors_total")
                        .register(this.meterRegistry)
                        .increment();
                sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
            }
            emitter.complete();
        });
        return emitter;
    }

    private void finalizeStream(AtomicBoolean finished, Timer.Sample sample, Usage usage, String toolInvoked) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        sample.stop(Timer.builder("chat_stream_latency").register(this.meterRegistry));
        collectEstimatedCost(usage, "chat", toolInvoked);
    }

    private void collectEstimatedCostFromUsage(ChatResponse response, String callType, String toolInvoked) {
        collectEstimatedCost(extractUsage(response), callType, toolInvoked);
    }

    private void collectEstimatedCost(Usage usage, String callType, String toolInvoked) {
        if (usage == null || usage.getPromptTokens() == null || usage.getCompletionTokens() == null) {
            return;
        }
        BigDecimal tokens = BigDecimal.valueOf(usage.getPromptTokens() + usage.getCompletionTokens());
        BigDecimal unitPrice = resolveUnitPrice(callType);
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal estimatedCost = tokens
                .divide(ONE_THOUSAND, 6, RoundingMode.HALF_UP)
                .multiply(unitPrice);
        Counter.builder("ai_estimated_cost_total")
                .tag("call_type", callType)
                .tag("tool_invoked", toolInvoked)
                .register(this.meterRegistry)
                .increment(estimatedCost.doubleValue());
    }

    private BigDecimal resolveUnitPrice(String callType) {
        if ("embedding".equals(callType)) {
            return this.chatCostProperties.getEmbedding();
        }
        return this.chatCostProperties.getChat();
    }

    private List<Message> buildPromptMessages(String conversationId, String message) {
        List<String> history = this.conversationMessageStore.loadMessages(conversationId);
        List<Message> messages = new ArrayList<>();
        for (String item : history) {
            if (item.startsWith("U:")) {
                messages.add(new UserMessage(item.substring(2)));
                continue;
            }
            if (item.startsWith("A:")) {
                messages.add(new AssistantMessage(item.substring(2)));
            }
        }
        messages.add(new UserMessage(message));
        return messages;
    }

    private void appendConversation(String conversationId, String userMessage, String assistantReply) {
        this.conversationMessageStore.appendMessage(conversationId, "U:" + userMessage);
        this.conversationMessageStore.appendMessage(conversationId, "A:" + assistantReply);
    }

    private static String normalizeConversationId(String conversationId) {
        if (StringUtils.hasText(conversationId)) {
            return conversationId.trim();
        }
        return "conversation-" + UUID.randomUUID();
    }

    private static String normalizeMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "你好";
        }
        return message.trim();
    }

    private static String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        if (text == null) {
            return "";
        }
        return text;
    }

    private static Usage extractUsage(ChatResponse response) {
        if (response == null || response.getMetadata() == null) {
            return null;
        }
        return response.getMetadata().getUsage();
    }

    private static void sendEvent(SseEmitter emitter, String eventName, ChatEventPayload payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payload));
        } catch (IOException ex) {
            throw new IllegalStateException("SSE send failed", ex);
        }
    }
}
