package com.zhida.aiagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.model.dto.EvalRunResult;
import com.zhida.aiagent.model.dto.SourceReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RagEvaluationServiceTest {

    @Test
    void shouldRunGoldenSetWithKeywordAndSourceChecks() {
        ChatService chatService = mock(ChatService.class);
        when(chatService.chat("什么是 Agentic RAG？", "eval-agentic-rag-basic", "agent"))
                .thenReturn(new ChatService.ChatServiceResponse(
                        "Agentic RAG 会先检索知识库，再基于证据生成答案。",
                        List.of(new SourceReference(1L, "Agent 文档", "agent.md", "证据片段", 0.9))
                ));
        RagEvaluationService service = new RagEvaluationService(chatService, new ObjectMapper());

        List<EvalRunResult> results = service.run(List.of("agentic-rag-basic"));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().caseId()).isEqualTo("agentic-rag-basic");
        assertThat(results.getFirst().passed()).isTrue();
        assertThat(results.getFirst().matchedKeywords()).contains("Agentic RAG", "知识库");
        assertThat(results.getFirst().sourceCount()).isEqualTo(1);
    }
}
