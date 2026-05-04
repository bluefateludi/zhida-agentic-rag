package com.zhida.aiagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.model.dto.RagTrace;
import com.zhida.aiagent.model.dto.RetrievalTraceItem;
import com.zhida.aiagent.model.dto.TraceTimelineStep;
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
        recordSuccessWithTimeline(
                mode,
                sessionId,
                originalQuery,
                rewrittenQuery,
                category,
                traceId,
                latencyMs,
                safeRetrievals,
                buildSuccessTimeline(mode, latencyMs, safeRetrievals)
        );
    }

    public void recordSuccessWithTimeline(String mode,
                                          String sessionId,
                                          String originalQuery,
                                          String rewrittenQuery,
                                          String category,
                                          String traceId,
                                          Long latencyMs,
                                          List<RetrievalTraceItem> retrievals,
                                          List<TraceTimelineStep> timeline) {
        List<RetrievalTraceItem> safeRetrievals = retrievals == null ? List.of() : retrievals;
        List<TraceTimelineStep> safeTimeline = timeline == null ? List.of() : timeline;
        RagTrace trace = new RagTrace(
                traceId,
                originalQuery,
                rewrittenQuery,
                category,
                latencyMs,
                safeRetrievals,
                safeTimeline
        );
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
        RagTrace trace = new RagTrace(
                traceId,
                originalQuery,
                rewrittenQuery,
                category,
                latencyMs,
                List.of(),
                buildFailureTimeline(mode, latencyMs, errorMessage)
        );
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

    private List<TraceTimelineStep> buildSuccessTimeline(String mode,
                                                         Long latencyMs,
                                                         List<RetrievalTraceItem> retrievals) {
        if ("REPORT".equals(mode)) {
            int kbEvidenceCount = countBySourceType(retrievals, false);
            int webEvidenceCount = countBySourceType(retrievals, true);
            int totalEvidenceCount = retrievals == null ? 0 : retrievals.size();
            return List.of(
                    new TraceTimelineStep("Query Rewrite", "DONE", "问题已改写为更适合研究报告的检索表达。", null, null),
                    new TraceTimelineStep("KB Retrieval", "DONE", evidenceSummary(kbEvidenceCount, "知识库证据"), kbEvidenceCount, null),
                    new TraceTimelineStep("Web Research", "DONE", evidenceSummary(webEvidenceCount, "公开网络证据"), webEvidenceCount, null),
                    new TraceTimelineStep("Writer Agent", "DONE", "写作 Agent 已基于 ResearchBrief 组织报告结构。", totalEvidenceCount, null),
                    new TraceTimelineStep("Report Output", "DONE", "报告 Markdown 已生成并写入本次 trace。", totalEvidenceCount, latencyMs)
            );
        }

        int evidenceCount = retrievals == null ? 0 : retrievals.size();
        return List.of(
                new TraceTimelineStep("Query Rewrite", "DONE", "问题已改写为更适合知识库检索的表达。", null, null),
                new TraceTimelineStep("Retrieval", "DONE", evidenceSummary(evidenceCount, "召回证据"), evidenceCount, null),
                new TraceTimelineStep("Answer", "DONE", "回答已生成并持久化本次 trace。", evidenceCount, latencyMs)
        );
    }

    private List<TraceTimelineStep> buildFailureTimeline(String mode, Long latencyMs, String errorMessage) {
        String summary = errorMessage == null || errorMessage.isBlank()
                ? "执行失败，未记录具体错误。"
                : errorMessage;
        if ("REPORT".equals(mode)) {
            return List.of(
                    new TraceTimelineStep("Query Rewrite", "BLOCKED", summary, null, null),
                    new TraceTimelineStep("KB Retrieval", "BLOCKED", "阶段未完成。", 0, null),
                    new TraceTimelineStep("Web Research", "BLOCKED", "阶段未完成。", 0, null),
                    new TraceTimelineStep("Writer Agent", "BLOCKED", "阶段未完成。", 0, null),
                    new TraceTimelineStep("Report Output", "BLOCKED", summary, 0, latencyMs)
            );
        }
        return List.of(
                new TraceTimelineStep("Query Rewrite", "BLOCKED", summary, null, null),
                new TraceTimelineStep("Retrieval", "BLOCKED", "阶段未完成。", 0, null),
                new TraceTimelineStep("Answer", "BLOCKED", summary, 0, latencyMs)
        );
    }

    private int countBySourceType(List<RetrievalTraceItem> retrievals, boolean webSource) {
        if (retrievals == null || retrievals.isEmpty()) {
            return 0;
        }
        return (int) retrievals.stream()
                .filter(item -> {
                    boolean currentIsWeb = "web".equalsIgnoreCase(item.category());
                    return webSource == currentIsWeb;
                })
                .count();
    }

    private String evidenceSummary(int evidenceCount, String label) {
        return evidenceCount > 0
                ? "已记录 " + evidenceCount + " 条" + label + "。"
                : "未记录" + label + "。";
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
