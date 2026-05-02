package com.zhida.aiagent.service;

import com.zhida.aiagent.model.dto.ResearchBrief;
import com.zhida.aiagent.rag.QueryRewriter;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResearchAgentServiceTest {

    @Test
    void shouldBuildBriefFromKnowledgeBaseWithoutWebFallbackWhenEvidenceIsEnough() {
        VectorStore vectorStore = mock(VectorStore.class);
        QueryRewriter queryRewriter = mock(QueryRewriter.class);
        WebResearchService webResearchService = mock(WebResearchService.class);
        when(queryRewriter.doQueryRewrite("怎么设计企业知识库问答首页")).thenReturn("企业知识库问答首页设计要点");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(
                new Document("证据一", Map.of(
                        "documentId", 11L,
                        "documentTitle", "首页设计规范",
                        "fileName", "design.md",
                        "category", "product",
                        "chunkIndex", 2
                )),
                new Document("证据二", Map.of(
                        "documentId", 12L,
                        "documentTitle", "Agent 工作台说明",
                        "fileName", "agent.md",
                        "category", "agent",
                        "chunkIndex", 4
                ))
        ));

        ResearchAgentService service = new ResearchAgentService(vectorStore, queryRewriter, webResearchService);
        ResearchBrief brief = service.buildBrief("怎么设计企业知识库问答首页", "product");

        assertThat(brief.originalQuestion()).isEqualTo("怎么设计企业知识库问答首页");
        assertThat(brief.rewrittenQuestion()).isEqualTo("企业知识库问答首页设计要点");
        assertThat(brief.kbEvidence()).hasSize(2);
        assertThat(brief.kbEvidence().getFirst().getRank()).isEqualTo(1);
        assertThat(brief.kbEvidence().getFirst().getChunkIndex()).isEqualTo(2);
        assertThat(brief.webEvidence()).isEmpty();
        assertThat(brief.informationGaps()).isEmpty();
        assertThat(brief.subQuestions()).contains("这个问题的核心判断是什么？");
        verify(webResearchService, never()).research(any());
    }

    @Test
    void shouldUseWebResearchAndMarkInformationGapsWhenKnowledgeBaseEvidenceIsInsufficient() {
        VectorStore vectorStore = mock(VectorStore.class);
        QueryRewriter queryRewriter = mock(QueryRewriter.class);
        WebResearchService webResearchService = mock(WebResearchService.class);
        when(queryRewriter.doQueryRewrite("最新的 AI Agent 行业趋势是什么")).thenReturn("AI Agent 行业最新趋势");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        when(webResearchService.research("AI Agent 行业最新趋势")).thenReturn(
                new WebResearchService.WebResearchResult(
                        "- 行业正在走向工作流编排",
                        List.of(new com.zhida.aiagent.model.dto.SourceReference(
                                "行业报告",
                                "https://example.com/report",
                                "行业正在走向工作流编排",
                                null
                        ))
                )
        );

        ResearchAgentService service = new ResearchAgentService(vectorStore, queryRewriter, webResearchService);
        ResearchBrief brief = service.buildBrief("最新的 AI Agent 行业趋势是什么", null);

        assertThat(brief.kbEvidence()).isEmpty();
        assertThat(brief.webEvidence()).hasSize(1);
        assertThat(brief.informationGaps()).contains("知识库中缺少足够的直接证据");
        assertThat(brief.informationGaps()).contains("结论依赖公开网络资料，仍需结合内部知识库进一步核验");
        verify(webResearchService).research("AI Agent 行业最新趋势");
    }
}
