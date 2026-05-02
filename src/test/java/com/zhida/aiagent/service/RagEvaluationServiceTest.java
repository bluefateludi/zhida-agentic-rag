package com.zhida.aiagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.model.dto.EvalRunResult;
import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.model.entity.ChatSession;
import com.zhida.aiagent.model.enums.ChatMode;
import com.zhida.aiagent.repository.ChatSessionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagEvaluationServiceTest {

    @Test
    void shouldRunGoldenSetWithKeywordAndSourceChecks() {
        ChatService chatService = mock(ChatService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        when(chatSessionRepository.existsById("eval-agentic-rag-basic")).thenReturn(false);
        when(chatService.chat("什么是 Agentic RAG？", "eval-agentic-rag-basic", "agent"))
                .thenReturn(new ChatService.ChatServiceResponse(
                        "Agentic RAG 会先检索知识库，再基于证据生成答案。",
                        List.of(new SourceReference(1L, "Agent 文档", "agent.md", "证据片段", 0.9))
                ));
        RagEvaluationService service = new RagEvaluationService(chatService, new ObjectMapper(), chatSessionRepository);

        List<EvalRunResult> results = service.run(List.of("agentic-rag-basic"));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().caseId()).isEqualTo("agentic-rag-basic");
        assertThat(results.getFirst().passed()).isTrue();
        assertThat(results.getFirst().matchedKeywords()).contains("Agentic RAG", "知识库");
        assertThat(results.getFirst().sourceCount()).isEqualTo(1);

        ArgumentCaptor<ChatSession> sessionCaptor = ArgumentCaptor.forClass(ChatSession.class);
        verify(chatSessionRepository).save(sessionCaptor.capture());
        ChatSession session = sessionCaptor.getValue();
        assertThat(session.getId()).isEqualTo("eval-agentic-rag-basic");
        assertThat(session.getTitle()).isEqualTo("RAG 评测 - agentic-rag-basic");
        assertThat(session.getMode()).isEqualTo(ChatMode.CHAT);
        verify(chatService).chat("什么是 Agentic RAG？", "eval-agentic-rag-basic", "agent");
    }

    @Test
    void shouldReuseExistingEvaluationSessionBeforeChatting() {
        ChatService chatService = mock(ChatService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        when(chatSessionRepository.existsById("eval-agentic-rag-basic")).thenReturn(true);
        when(chatService.chat(any(), any(), any()))
                .thenReturn(new ChatService.ChatServiceResponse("Agentic RAG 知识库", List.of()));
        RagEvaluationService service = new RagEvaluationService(chatService, new ObjectMapper(), chatSessionRepository);

        service.run(List.of("agentic-rag-basic"));

        verify(chatSessionRepository).existsById("eval-agentic-rag-basic");
        verify(chatSessionRepository, org.mockito.Mockito.never()).save(any(ChatSession.class));
    }
}
