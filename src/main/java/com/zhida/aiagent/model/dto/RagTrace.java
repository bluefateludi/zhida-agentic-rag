package com.zhida.aiagent.model.dto;

import java.util.List;

/**
 * Request-level RAG trace for evaluation and observability.
 */
public record RagTrace(
        String traceId,
        String originalQuery,
        String rewrittenQuery,
        String category,
        Long latencyMs,
        List<RetrievalTraceItem> retrievals
) {
    public RagTrace {
        retrievals = retrievals == null ? List.of() : List.copyOf(retrievals);
    }
}
