package com.zhida.aiagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.model.dto.RagTrace;
import com.zhida.aiagent.model.dto.RetrievalTraceItem;
import com.zhida.aiagent.model.entity.RagTraceLog;
import com.zhida.aiagent.repository.RagTraceLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagTraceServiceTest {

    @Test
    void shouldPersistSuccessfulRagTraceWithRetrievalMetadata() throws Exception {
        RagTraceLogRepository repository = mock(RagTraceLogRepository.class);
        RagTraceService service = new RagTraceService(repository, new ObjectMapper());
        RetrievalTraceItem retrieval = new RetrievalTraceItem(
                1,
                7L,
                "Agent 知识库",
                "agent.md",
                "agent",
                3,
                0.86,
                "RAG 召回内容"
        );

        service.recordSuccess(
                "CHAT",
                "session-1",
                "什么是 Agentic RAG？",
                "Agentic RAG 定义",
                "agent",
                "trace-1",
                128L,
                List.of(retrieval)
        );

        ArgumentCaptor<RagTraceLog> captor = ArgumentCaptor.forClass(RagTraceLog.class);
        verify(repository).save(captor.capture());
        RagTraceLog saved = captor.getValue();

        assertThat(saved.getTraceId()).isEqualTo("trace-1");
        assertThat(saved.getMode()).isEqualTo("CHAT");
        assertThat(saved.getSessionId()).isEqualTo("session-1");
        assertThat(saved.getOriginalQuery()).isEqualTo("什么是 Agentic RAG？");
        assertThat(saved.getRewrittenQuery()).isEqualTo("Agentic RAG 定义");
        assertThat(saved.getCategory()).isEqualTo("agent");
        assertThat(saved.getLatencyMs()).isEqualTo(128L);
        assertThat(saved.getRetrievalCount()).isEqualTo(1);
        assertThat(saved.getSuccess()).isTrue();
        assertThat(saved.getErrorMessage()).isNull();
        assertThat(saved.getSourcesJson()).contains("Agent 知识库");

        RagTrace trace = new ObjectMapper().readValue(saved.getTraceJson(), RagTrace.class);
        assertThat(trace.traceId()).isEqualTo("trace-1");
        assertThat(trace.retrievals()).hasSize(1);
        assertThat(trace.retrievals().getFirst().fileName()).isEqualTo("agent.md");
    }

    @Test
    void shouldReturnRecentTracesAndTraceDetails() {
        RagTraceLogRepository repository = mock(RagTraceLogRepository.class);
        RagTraceService service = new RagTraceService(repository, new ObjectMapper());
        RagTraceLog log = new RagTraceLog();
        log.setTraceId("trace-1");
        when(repository.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of(log));
        when(repository.findById("trace-1")).thenReturn(Optional.of(log));

        assertThat(service.listRecent(20)).containsExactly(log);
        assertThat(service.getTrace("trace-1")).containsSame(log);
    }
}
