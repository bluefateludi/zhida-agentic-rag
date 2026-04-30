package com.zhida.aiagent.model.dto;

/**
 * One retrieved chunk in a RAG execution trace.
 */
public record RetrievalTraceItem(
        Integer rank,
        Long documentId,
        String documentTitle,
        String fileName,
        String category,
        Integer chunkIndex,
        Double score,
        String contentPreview
) {
}
