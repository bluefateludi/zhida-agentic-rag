package com.zhida.aiagent.service;

import com.zhida.aiagent.model.dto.ResearchBrief;
import com.zhida.aiagent.model.dto.SourceReference;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import org.springframework.ai.chat.model.ChatModel;

class WriterAgentServiceTest {

    @Test
    void shouldBuildPromptThatIncludesEvidenceAndInformationGaps() {
        WriterAgentService service = new WriterAgentService(mock(ChatModel.class));
        ResearchBrief brief = new ResearchBrief(
                "这个方向值得做吗",
                "评估这个 Agentic RAG 方向是否值得投入",
                List.of("核心结论是什么？", "有哪些证据支持？"),
                List.of(new SourceReference(1L, "产品评审纪要", "review.md", "已有 3 个试点团队明确提出知识库问答需求。", 0.95)),
                List.of(new SourceReference("行业报道", "https://example.com", "2026 年行业开始从单点问答转向工作流编排。", null)),
                List.of("缺少真实付费意愿数据"),
                Instant.parse("2026-05-02T10:15:30Z")
        );

        String prompt = service.buildReportPrompt(brief);

        assertThat(prompt).contains("原始问题：这个方向值得做吗");
        assertThat(prompt).contains("改写后的研究问题：评估这个 Agentic RAG 方向是否值得投入");
        assertThat(prompt).contains("知识库证据");
        assertThat(prompt).contains("产品评审纪要");
        assertThat(prompt).contains("公开网络证据");
        assertThat(prompt).contains("缺少真实付费意愿数据");
        assertThat(prompt).contains("如果证据不足，必须明确标记“信息不足”");
    }
}
