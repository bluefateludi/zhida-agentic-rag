package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.entity.ChatMessage;
import com.zhida.aiagent.model.entity.ChatSession;
import com.zhida.aiagent.model.enums.ChatMode;
import com.zhida.aiagent.service.ChatService;
import com.zhida.aiagent.service.ChatSessionService;
import com.zhida.aiagent.service.PmChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 产品分析模式接口
 */
@Slf4j
@RestController
@RequestMapping("/pm")
public class PmController {

    private final PmChatService pmChatService;
    private final ChatSessionService chatSessionService;

    public PmController(PmChatService pmChatService, ChatSessionService chatSessionService) {
        this.pmChatService = pmChatService;
        this.chatSessionService = chatSessionService;
    }

    @PostMapping("/session")
    public ResponseEntity<ChatSession> createSession(@RequestBody(required = false) Map<String, String> body) {
        String title = body != null ? body.get("title") : null;
        return ResponseEntity.ok(chatSessionService.createSession(title, ChatMode.PM));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> listSessions() {
        return ResponseEntity.ok(chatSessionService.listSessions(ChatMode.PM));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> deleteSession(@PathVariable String sessionId) {
        chatSessionService.getSession(sessionId, ChatMode.PM);
        chatSessionService.deleteSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "会话已删除"));
    }

    @PostMapping("/send")
    public ResponseEntity<ChatService.ChatServiceResponse> chatSend(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(pmChatService.chat(body.get("message"), body.get("sessionId"), body.get("category")));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam String message,
                                 @RequestParam String sessionId,
                                 @RequestParam(required = false) String category) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        pmChatService.chatStream(message, sessionId, category)
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
                        error -> sendFallbackAndComplete(sseEmitter),
                        sseEmitter::complete
                );
        return sseEmitter;
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(pmChatService.getHistory(sessionId));
    }

    private record StreamCompletePayload(String content, List<?> sources) {}

    private void sendFallbackAndComplete(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("complete")
                    .data(new StreamCompletePayload(
                            "PM 模式暂时无法完成完整分析，请稍后重试。",
                            List.of()
                    )));
            sseEmitter.complete();
        } catch (Exception e) {
            sseEmitter.completeWithError(e);
        }
    }
}
