package com.zhida.aiagent.model.dto;

import java.time.Instant;
import java.util.List;

/**
 * 研究阶段的结构化简报，供写作 Agent 消费。
 */
public record ResearchBrief(
        String originalQuestion,
        String rewrittenQuestion,
        List<String> subQuestions,
        List<SourceReference> kbEvidence,
        List<SourceReference> webEvidence,
        List<String> informationGaps,
        Instant generatedAt
) {
    public ResearchBrief {
        subQuestions = subQuestions == null ? List.of() : List.copyOf(subQuestions);
        kbEvidence = kbEvidence == null ? List.of() : List.copyOf(kbEvidence);
        webEvidence = webEvidence == null ? List.of() : List.copyOf(webEvidence);
        informationGaps = informationGaps == null ? List.of() : List.copyOf(informationGaps);
    }
}
