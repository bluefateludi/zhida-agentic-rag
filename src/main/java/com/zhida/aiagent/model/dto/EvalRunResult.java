package com.zhida.aiagent.model.dto;

import java.util.List;

/**
 * Result of one golden-set evaluation case.
 */
public record EvalRunResult(
        String caseId,
        String question,
        String actualAnswer,
        List<String> expectedKeywords,
        List<String> matchedKeywords,
        int sourceCount,
        boolean passed
) {
    public EvalRunResult {
        expectedKeywords = expectedKeywords == null ? List.of() : List.copyOf(expectedKeywords);
        matchedKeywords = matchedKeywords == null ? List.of() : List.copyOf(matchedKeywords);
    }
}
