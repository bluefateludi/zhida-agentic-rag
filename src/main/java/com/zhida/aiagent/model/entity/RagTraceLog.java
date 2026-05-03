package com.zhida.aiagent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Persisted RAG execution trace for observability dashboards.
 */
@Data
@Entity
@Table(name = "rag_trace_log")
public class RagTraceLog {

    @Id
    @Column(length = 64)
    private String traceId;

    @Column(length = 32)
    private String mode;

    @Column(length = 64)
    private String sessionId;

    @Column(columnDefinition = "TEXT")
    private String originalQuery;

    @Column(columnDefinition = "TEXT")
    private String rewrittenQuery;

    @Column(length = 80)
    private String category;

    private Integer retrievalCount;

    private Long latencyMs;

    private Boolean success;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String sourcesJson;

    @Column(columnDefinition = "TEXT")
    private String traceJson;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
