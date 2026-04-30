package com.zhida.aiagent.model.dto;

import java.util.List;

/**
 * One golden-set case for RAG regression evaluation.
 */
public record EvalCase(
        String id,
        String question,
        String category,
        List<String> expectedKeywords,
        boolean requireSources
) {
    public EvalCase {
        expectedKeywords = expectedKeywords == null ? List.of() : List.copyOf(expectedKeywords);
    }
}
