package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.dto.ResearchBrief;
import com.zhida.aiagent.model.dto.RetrievalTraceItem;
import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.service.RagTraceService;
import com.zhida.aiagent.service.ResearchAgentService;
import com.zhida.aiagent.service.WriterAgentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResearchAgentService researchAgentService;

    @MockBean
    private WriterAgentService writerAgentService;

    @MockBean
    private RagTraceService ragTraceService;

    @Test
    void shouldGenerateMarkdownReportFromResearchBrief() throws Exception {
        SourceReference source = new SourceReference(
                7L,
                "Agentic RAG 设计文档",
                "agentic-rag.md",
                "研究 Agent 先整理证据，写作 Agent 再输出报告。",
                0.86
        );
        source.setCategory("agent");
        source.setChunkIndex(3);
        source.setRank(1);
        ResearchBrief brief = new ResearchBrief(
                "这个方向值得做吗",
                "评估这个 Agentic RAG 方向是否值得投入",
                List.of("核心结论是什么？"),
                List.of(source),
                List.of(),
                List.of("缺少用户访谈"),
                Instant.parse("2026-05-02T10:15:30Z")
        );
        when(researchAgentService.buildBrief("这个方向值得做吗", "agent")).thenReturn(brief);
        when(writerAgentService.writeReport(brief)).thenReturn("# 研究报告\n\n信息不足");

        mockMvc.perform(post("/report/generate")
                        .contentType("application/json")
                        .content("""
                                {
                                  "question": "这个方向值得做吗",
                                  "category": "agent"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reportMarkdown\":\"# 研究报告\\n\\n信息不足\"")))
                .andExpect(content().string(containsString("\"rewrittenQuestion\":\"评估这个 Agentic RAG 方向是否值得投入\"")));

        ArgumentCaptor<List<RetrievalTraceItem>> retrievalsCaptor = ArgumentCaptor.captor();
        verify(ragTraceService).recordSuccess(
                eq("REPORT"),
                isNull(),
                eq("这个方向值得做吗"),
                eq("评估这个 Agentic RAG 方向是否值得投入"),
                eq("agent"),
                anyString(),
                anyLong(),
                retrievalsCaptor.capture()
        );
        assertThat(retrievalsCaptor.getValue())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.rank()).isEqualTo(1);
                    assertThat(item.documentId()).isEqualTo(7L);
                    assertThat(item.documentTitle()).isEqualTo("Agentic RAG 设计文档");
                    assertThat(item.fileName()).isEqualTo("agentic-rag.md");
                    assertThat(item.category()).isEqualTo("agent");
                    assertThat(item.chunkIndex()).isEqualTo(3);
                    assertThat(item.score()).isEqualTo(0.86);
                    assertThat(item.contentPreview()).contains("研究 Agent");
                });
    }
}
