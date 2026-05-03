package com.zhida.aiagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.model.dto.RagTrace;
import com.zhida.aiagent.model.dto.RetrievalTraceItem;
import com.zhida.aiagent.model.entity.RagTraceLog;
import com.zhida.aiagent.repository.RagTraceLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Persists and exposes read-only RAG trace logs.
 */
@Slf4j
@Service
public class RagTraceService {

    private final RagTraceLogRepository ragTraceLogRepository;
    private final ObjectMapper objectMapper;

    public RagTraceService(RagTraceLogRepository ragTraceLogRepository, ObjectMapper objectMapper) {
        this.ragTraceLogRepository = ragTraceLogRepository;
        this.objectMapper = objectMapper;
    }

    public void recordSuccess(String mode,
                              String sessionId,
                              String originalQuery,
                              String rewrittenQuery,
                              String category,
                              String traceId,
                              Long latencyMs,
                              List<RetrievalTraceItem> retrievals) {
        List<RetrievalTraceItem> safeRetrievals = retrievals == null ? List.of() : retrievals;
        RagTrace trace = new RagTrace(traceId, originalQuery, rewrittenQuery, category, latencyMs, safeRetrievals);
        RagTraceLog log = baseLog(mode, sessionId, originalQuery, rewrittenQuery, category, traceId, latencyMs);
        log.setRetrievalCount(safeRetrievals.size());
        log.setSuccess(true);
        log.setSourcesJson(toJson(safeRetrievals));
        log.setTraceJson(toJson(trace));
        ragTraceLogRepository.save(log);
    }

    public void recordFailure(String mode,
                              String sessionId,
                              String originalQuery,
                              String rewrittenQuery,
                              String category,
                              String traceId,
                              Long latencyMs,
                              String errorMessage) {
        RagTrace trace = new RagTrace(traceId, originalQuery, rewrittenQuery, category, latencyMs, List.of());
        RagTraceLog log = baseLog(mode, sessionId, originalQuery, rewrittenQuery, category, traceId, latencyMs);
        log.setRetrievalCount(0);
        log.setSuccess(false);
        log.setErrorMessage(errorMessage);
        log.setSourcesJson("[]");
        log.setTraceJson(toJson(trace));
        ragTraceLogRepository.save(log);
    }

    public List<RagTraceLog> listRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return ragTraceLogRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .limit(safeLimit)
                .toList();
    }

    public Optional<RagTraceLog> getTrace(String traceId) {
        return ragTraceLogRepository.findById(traceId);
    }

    private RagTraceLog baseLog(String mode,
                                String sessionId,
                                String originalQuery,
                                String rewrittenQuery,
                                String category,
                                String traceId,
                                Long latencyMs) {
        RagTraceLog log = new RagTraceLog();
        log.setMode(mode);
        log.setSessionId(sessionId);
        log.setOriginalQuery(originalQuery);
        log.setRewrittenQuery(rewrittenQuery);
        log.setCategory(category);
        log.setTraceId(traceId);
        log.setLatencyMs(latencyMs);
        return log;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("RAG trace JSON serialization failed", e);
            return "[]";
        }
    }
}
