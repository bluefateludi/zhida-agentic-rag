package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.entity.RagTraceLog;
import com.zhida.aiagent.service.RagTraceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraceController.class)
class TraceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RagTraceService ragTraceService;

    @Test
    void shouldListRecentTraceLogs() throws Exception {
        RagTraceLog log = new RagTraceLog();
        log.setTraceId("trace-1");
        log.setOriginalQuery("什么是 Agentic RAG？");
        log.setRewrittenQuery("Agentic RAG 定义");
        log.setLatencyMs(128L);
        log.setRetrievalCount(2);
        log.setSourcesJson("[{\"fileName\":\"agent.md\"}]");
        when(ragTraceService.listRecent(20)).thenReturn(List.of(log));

        mockMvc.perform(get("/traces/recent"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("trace-1")))
                .andExpect(content().string(containsString("Agentic RAG 定义")))
                .andExpect(content().string(containsString("agent.md")));
    }

    @Test
    void shouldGetTraceDetailByTraceId() throws Exception {
        RagTraceLog log = new RagTraceLog();
        log.setTraceId("trace-1");
        log.setTraceJson("{\"traceId\":\"trace-1\",\"retrievals\":[{\"rank\":1,\"fileName\":\"agent.md\"}]}");
        when(ragTraceService.getTrace("trace-1")).thenReturn(Optional.of(log));

        mockMvc.perform(get("/traces/trace-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("trace-1")))
                .andExpect(content().string(containsString("agent.md")));
    }

    @Test
    void shouldReturnNotFoundWhenTraceDoesNotExist() throws Exception {
        when(ragTraceService.getTrace("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/traces/missing"))
                .andExpect(status().isNotFound());
    }
}
