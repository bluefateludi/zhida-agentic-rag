package com.zhida.aiagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.chatmemory.DatabaseChatMemoryRepository;
import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.model.entity.ChatMessage;
import com.zhida.aiagent.model.enums.MessageRole;
import com.zhida.aiagent.rag.KnowledgeBaseRagConfig;
import com.zhida.aiagent.rag.QueryRewriter;
import com.zhida.aiagent.rag.SourceDocumentCollector;
import com.zhida.aiagent.repository.ChatMessageRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 核心知识库聊天服务
 */
@Slf4j
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    private static final String SYSTEM_PROMPT = """
            你是「智答 AI」——一个专业的企业知识库问答助手。

            你的职责：
            1. 基于知识库中的文档内容，准确、专业地回答用户问题
            2. 如果知识库中没有相关信息，请诚实告知用户，不要编造答案
            3. 回答时尽量引用具体文档来源
            4. 使用 Markdown 格式使回答更清晰易读

            回答要求：
            - 准确性优先，基于知识库内容回答
            - 条理清晰，使用编号或分段组织答案
            - 如有不确定之处，明确指出
            """;

    public ChatService(ChatModel dashscopeChatModel,
                       DatabaseChatMemoryRepository databaseChatMemoryRepository) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(databaseChatMemoryRepository)
                .maxMessages(20)
                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * 同步聊天（带 RAG + 来源追溯）
     */
    public ChatServiceResponse chat(String message, String sessionId, String category) {
        log.info("收到聊天请求 - session: {}, category: {}, message: {}", sessionId, category, message);
        saveMessage(sessionId, MessageRole.USER, message, null);

        String rewrittenQuery = queryRewriter.doQueryRewrite(message);
        String traceId = UUID.randomUUID().toString();

        SourceDocumentCollector sourceCollector = new SourceDocumentCollector();

        var ragAdvisor = KnowledgeBaseRagConfig.createRagAdvisor(pgVectorVectorStore, category, sourceCollector::capture);

        var aiResponse = chatClient
                .prompt()
                .user(rewrittenQuery)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, sessionId))
                .advisors(ragAdvisor)
                .call()
                .chatResponse();

        String content = aiResponse.getResult().getOutput().getText();

        List<SourceReference> sources = buildSourceReferences(sourceCollector.getDocuments(), traceId);
        log.info("RAG 检索来源数: {}, AI 回复长度: {}", sources.size(), content != null ? content.length() : 0);

        saveMessage(sessionId, MessageRole.ASSISTANT, content, sources);

        return new ChatServiceResponse(content, sources);
    }

    /**
     * SSE 流式聊天（带 RAG + 来源追溯）
     */
    public Flux<ChatStreamEvent> chatStream(String message, String sessionId, String category) {
        log.info("收到流式聊天请求 - session: {}, category: {}, message: {}", sessionId, category, message);
        saveMessage(sessionId, MessageRole.USER, message, null);

        String rewrittenQuery = queryRewriter.doQueryRewrite(message);
        String traceId = UUID.randomUUID().toString();
        SourceDocumentCollector sourceCollector = new SourceDocumentCollector();

        var ragAdvisor = KnowledgeBaseRagConfig.createRagAdvisor(pgVectorVectorStore, category, sourceCollector::capture);
        ChatStreamAccumulator accumulator = new ChatStreamAccumulator(
                () -> buildSourceReferences(sourceCollector.getDocuments(), traceId),
                (content, sources) -> {
                    log.info("流式 RAG 检索来源数: {}, AI 回复长度: {}", sources.size(), content != null ? content.length() : 0);
                    saveMessage(sessionId, MessageRole.ASSISTANT, content, sources);
                }
        );

        return chatClient
                .prompt()
                .user(rewrittenQuery)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, sessionId))
                .advisors(ragAdvisor)
                .stream()
                .content()
                .map(accumulator::append)
                .concatWith(Flux.defer(() -> Flux.just(accumulator.complete())))
                .doOnError(error -> log.error("流式聊天失败 - session: {}", sessionId, error));
    }

    /**
     * 获取会话历史消息
     */
    public List<ChatMessage> getHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * 从 ThreadLocal 构建来源引用列表
     */
    private List<SourceReference> buildSourceReferences(List<Document> docs, String traceId) {
        if (docs == null || docs.isEmpty()) {
            return List.of();
        }
        return java.util.stream.IntStream.range(0, docs.size())
                .mapToObj(index -> {
                    Document doc = docs.get(index);
                    String text = doc.getText() != null ? doc.getText() : "";
                    SourceReference source = new SourceReference(
                            parseDocumentId(doc.getMetadata().get("documentId")),
                            (String) doc.getMetadata().get("documentTitle"),
                            (String) doc.getMetadata().get("fileName"),
                            text.length() > 200 ? text.substring(0, 200) + "..." : text,
                            null
                    );
                    source.setCategory((String) doc.getMetadata().get("category"));
                    source.setChunkIndex(parseInteger(doc.getMetadata().get("chunkIndex")));
                    source.setRank(index + 1);
                    source.setTraceId(traceId);
                    return source;
                })
                .collect(Collectors.toList());
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

    /**
     * 聊天响应内部类（避免与 Spring AI ChatResponse 冲突）
     */
    public record ChatServiceResponse(String content, List<SourceReference> sources) {}
}
