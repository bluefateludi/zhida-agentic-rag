package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.entity.ChatMessage;
import com.zhida.aiagent.model.entity.ChatSession;
import com.zhida.aiagent.service.ChatService;
import com.zhida.aiagent.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 聊天接口（含会话管理）
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;

    public ChatController(ChatService chatService, ChatSessionService chatSessionService) {
        this.chatService = chatService;
        this.chatSessionService = chatSessionService;
    }

    // ==================== 会话管理 ====================

    /**
     * 创建新会话
     */
    @PostMapping("/session")
    public ResponseEntity<ChatSession> createSession(
            @RequestBody(required = false) Map<String, String> body) {
        String title = body != null ? body.get("title") : null;
        ChatSession session = chatSessionService.createSession(title);
        return ResponseEntity.ok(session);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> listSessions() {
        List<ChatSession> sessions = chatSessionService.listSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * 重命名会话
     */
    @PutMapping("/session/{sessionId}")
    public ResponseEntity<ChatSession> renameSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        ChatSession session = chatSessionService.renameSession(sessionId, body.get("title"));
        return ResponseEntity.ok(session);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> deleteSession(@PathVariable String sessionId) {
        chatSessionService.deleteSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "会话已删除"));
    }

    // ==================== 聊天 ====================

    /**
     * 同步聊天
     */
    @PostMapping("/send")
    public ResponseEntity<ChatService.ChatServiceResponse> chatSend(
            @RequestBody Map<String, String> body) {
        String message = body.get("message");
        String sessionId = body.get("sessionId");
        String category = body.get("category");
        ChatService.ChatServiceResponse response = chatService.chat(message, sessionId, category);
        return ResponseEntity.ok(response);
    }

    /**
     * SSE 流式聊天
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam String sessionId,
            @RequestParam(required = false) String category) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        chatService.chatStream(message, sessionId, category)
                .subscribe(
                        event -> {
                            try {
                                if ("complete".equals(event.event())) {
                                    sseEmitter.send(SseEmitter.event()
                                            .name("complete")
                                            .data(new StreamCompletePayload(event.content(), event.sources())));
                                    return;
                                }
                                sseEmitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(event.content()));
                            } catch (Exception e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        sseEmitter::completeWithError,
                        sseEmitter::complete
                );
        return sseEmitter;
    }

    /**
     * 获取会话历史消息
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        List<ChatMessage> messages = chatService.getHistory(sessionId);
        return ResponseEntity.ok(messages);
    }

    private record StreamCompletePayload(String content, List<?> sources) {}
}
