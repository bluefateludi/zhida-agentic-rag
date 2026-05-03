package com.zhida.aiagent.model.dto;

/**
 * One observable stage in a persisted RAG trace.
 */
public record TraceTimelineStep(
        String name,
        String status,
        String summary,
        Integer evidenceCount,
        Long durationMs
) {
}
