package com.zhida.aiagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.model.entity.ChatMessage;
import com.zhida.aiagent.model.enums.ChatMode;
import com.zhida.aiagent.model.enums.MessageRole;
import com.zhida.aiagent.rag.KnowledgeBaseRagConfig;
import com.zhida.aiagent.rag.QueryRewriter;
import com.zhida.aiagent.rag.RagTraceCollector;
import com.zhida.aiagent.rag.SourceContextHolder;
import com.zhida.aiagent.repository.ChatMessageRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 产品分析模式聊天服务
 */
@Slf4j
@Service
public class PmChatService {

    private static final String PM_SYSTEM_PROMPT = """
            你是一个产品分析助手，负责帮助用户讨论产品、增长、用户价值和组织执行问题。

            回答要求：
            1. 表达直接、清晰、克制，不说空话
            2. 优先抓本质问题，先讲判断，再讲原因
            3. 同时参考知识库内容和联网研究结果，不能只依赖单一路径
            4. 如果事实不足或信息冲突，要明确说明不确定性
            5. 重点关注用户价值、产品效率、增长、组织执行和长期竞争力
            6. 使用 Markdown 输出，必要时使用短标题和列表
            7. 用户询问产品原则、判断标准、方法论或“什么是一个好产品”这类问题时，属于正常产品分析范围
            8. 如果知识库没有命中，但问题属于产品分析范围，不能拒答；应基于通用产品分析框架回答，并说明本次没有明确知识库依据
            """;

    private static final String STREAM_FALLBACK_MESSAGE = """
            PM 模式暂时无法完成完整分析，可能是联网研究、知识库检索或模型响应超时。

            我先给出一个可继续推进的判断框架：先看用户价值是否真实且高频，再看解决方案是否足够简单有效，最后看增长、留存和执行成本是否支撑长期成立。你可以稍后重试，我会继续结合知识库和联网研究补充依据。
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebResearchService webResearchService;
    private final ChatSessionService chatSessionService;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    private RagTraceService ragTraceService;

    public PmChatService(ChatModel dashscopeChatModel,
                         WebResearchService webResearchService,
                         ChatSessionService chatSessionService) {
        this.webResearchService = webResearchService;
        this.chatSessionService = chatSessionService;
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(PM_SYSTEM_PROMPT)
                .build();
    }

    public ChatService.ChatServiceResponse chat(String message, String sessionId, String category) {
        long startedAt = System.nanoTime();
        chatSessionService.getSession(sessionId, ChatMode.PM);
        saveMessage(sessionId, MessageRole.USER, message, null);

        String traceId = UUID.randomUUID().toString();
        String rewrittenQuery = message;
        RagTraceCollector traceCollector = new RagTraceCollector();

        try {
            rewrittenQuery = queryRewriter.doQueryRewrite(message);
            WebResearchService.WebResearchResult webResearchResult = webResearchService.research(rewrittenQuery);

            var ragAdvisor = KnowledgeBaseRagConfig.createPmRagAdvisor(pgVectorVectorStore, category, traceCollector::capture);
            var aiResponse = chatClient.prompt()
                    .user(buildPmPrompt(message, rewrittenQuery, webResearchResult.summary()))
                    .advisors(ragAdvisor)
                    .call()
                    .chatResponse();

            String content = aiResponse.getResult().getOutput().getText();
            List<SourceReference> sources = mergeSources(webResearchResult.sources(), traceId, traceCollector.getDocuments());
            saveMessage(sessionId, MessageRole.ASSISTANT, content, sources);
            recordTraceSuccess("PM", sessionId, message, rewrittenQuery, category, traceId, startedAt, traceCollector);
            return new ChatService.ChatServiceResponse(content, sources);
        } catch (RuntimeException e) {
            recordTraceFailure("PM", sessionId, message, rewrittenQuery, category, traceId, startedAt, e);
            throw e;
        } finally {
            SourceContextHolder.clear();
        }
    }

    public Flux<ChatStreamEvent> chatStream(String message, String sessionId, String category) {
        chatSessionService.getSession(sessionId, ChatMode.PM);
        saveMessage(sessionId, MessageRole.USER, message, null);

        return Flux.defer(() -> {
                    long startedAt = System.nanoTime();
                    String rewrittenQuery = queryRewriter.doQueryRewrite(message);
                    String traceId = UUID.randomUUID().toString();
                    RagTraceCollector traceCollector = new RagTraceCollector();
                    WebResearchService.WebResearchResult webResearchResult = webResearchService.research(rewrittenQuery);
                    SourceContextHolder.clear();

                    var ragAdvisor = KnowledgeBaseRagConfig.createPmRagAdvisor(pgVectorVectorStore, category, traceCollector::capture);
                    ChatStreamAccumulator accumulator = new ChatStreamAccumulator(
                            () -> mergeSources(webResearchResult.sources(), traceId, traceCollector.getDocuments()),
                            (content, sources) -> {
                                saveMessage(sessionId, MessageRole.ASSISTANT, content, sources);
                                recordTraceSuccess("PM_STREAM", sessionId, message, rewrittenQuery, category, traceId, startedAt, traceCollector);
                            }
                    );

                    return chatClient.prompt()
                            .user(buildPmPrompt(message, rewrittenQuery, webResearchResult.summary()))
                            .advisors(ragAdvisor)
                            .stream()
                            .content()
                            .map(accumulator::append)
                            .concatWith(Flux.defer(() -> Flux.just(accumulator.complete())))
                            .doOnError(error -> recordTraceFailure("PM_STREAM", sessionId, message, rewrittenQuery, category, traceId, startedAt, error));
                })
                .onErrorResume(error -> {
                    log.warn("PM 流式回复失败，返回兜底回复 - session: {}, error={}", sessionId, error.toString());
                    saveMessage(sessionId, MessageRole.ASSISTANT, STREAM_FALLBACK_MESSAGE, List.of());
                    return Flux.just(ChatStreamEvent.complete(STREAM_FALLBACK_MESSAGE, List.of()));
                })
                .doFinally(signalType -> SourceContextHolder.clear());
    }

    public List<ChatMessage> getHistory(String sessionId) {
        chatSessionService.getSession(sessionId, ChatMode.PM);
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private String buildPmPrompt(String originalMessage, String rewrittenQuery, String webSummary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户原始问题：").append(originalMessage).append("\n\n");
        prompt.append("改写后的核心问题：").append(rewrittenQuery).append("\n\n");
        if (webSummary != null && !webSummary.isBlank()) {
            prompt.append("联网研究结果：\n").append(webSummary).append("\n\n");
        } else {
            prompt.append("联网研究结果：暂无足够公开信息，请主要依赖知识库并明确不确定性。\n\n");
        }
        prompt.append("请整合知识库和联网研究，给出直接、清晰、可执行的产品判断。");
        return prompt.toString();
    }

    private List<SourceReference> mergeSources(List<SourceReference> webSources, String traceId, List<Document> kbDocs) {
        List<SourceReference> safeWebSources = webSources == null ? List.of() : webSources;
        List<SourceReference> merged = new ArrayList<>(safeWebSources);
        if (kbDocs != null && !kbDocs.isEmpty()) {
            merged.addAll(java.util.stream.IntStream.range(0, kbDocs.size())
                    .mapToObj(index -> {
                        Document doc = kbDocs.get(index);
                        String text = doc.getText() != null ? doc.getText() : "";
                        SourceReference source = new SourceReference(
                                (String) doc.getMetadata().get("documentTitle"),
                                (String) doc.getMetadata().get("fileName"),
                                text.length() > 200 ? text.substring(0, 200) + "..." : text,
                                null
                        );
                        source.setDocumentId(parseDocumentId(doc.getMetadata().get("documentId")));
                        source.setCategory((String) doc.getMetadata().get("category"));
                        source.setChunkIndex(parseInteger(doc.getMetadata().get("chunkIndex")));
                        source.setRank(safeWebSources.size() + index + 1);
                        source.setTraceId(traceId);
                        return source;
                    })
                    .collect(Collectors.toList()));
        }
        return merged;
    }

    private Long parseDocumentId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void saveMessage(String sessionId, MessageRole role, String content, List<SourceReference> sources) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        if (sources != null && !sources.isEmpty()) {
            try {
                msg.setSources(objectMapper.writeValueAsString(sources));
            } catch (JsonProcessingException e) {
                msg.setSources(sources.toString());
            }
        }
        chatMessageRepository.save(msg);
    }

    private void recordTraceSuccess(String mode,
                                    String sessionId,
                                    String originalQuery,
                                    String rewrittenQuery,
                                    String category,
                                    String traceId,
                                    long startedAt,
                                    RagTraceCollector traceCollector) {
        try {
            ragTraceService.recordSuccess(
                    mode,
                    sessionId,
                    originalQuery,
                    rewrittenQuery,
                    category,
                    traceId,
                    elapsedMs(startedAt),
                    traceCollector.toTraceItems()
            );
        } catch (RuntimeException e) {
            log.warn("PM RAG trace persistence failed - traceId={}", traceId, e);
        }
    }

    private void recordTraceFailure(String mode,
                                    String sessionId,
                                    String originalQuery,
                                    String rewrittenQuery,
                                    String category,
                                    String traceId,
                                    long startedAt,
                                    Throwable error) {
        try {
            ragTraceService.recordFailure(
                    mode,
                    sessionId,
                    originalQuery,
                    rewrittenQuery,
                    category,
                    traceId,
                    elapsedMs(startedAt),
                    error.getMessage()
            );
        } catch (RuntimeException e) {
            log.warn("PM RAG trace failure persistence failed - traceId={}", traceId, e);
        }
    }

    private long elapsedMs(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }
}
