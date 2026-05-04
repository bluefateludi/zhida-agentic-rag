package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.dto.ResearchBrief;
import com.zhida.aiagent.model.dto.RetrievalTraceItem;
import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.model.dto.TraceTimelineStep;
import com.zhida.aiagent.service.RagTraceService;
import com.zhida.aiagent.service.ResearchAgentService;
import com.zhida.aiagent.service.WriterAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 研究报告模式接口。
 */
@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {

    private final ResearchAgentService researchAgentService;
    private final WriterAgentService writerAgentService;
    private final RagTraceService ragTraceService;

    public ReportController(ResearchAgentService researchAgentService,
                            WriterAgentService writerAgentService,
                            RagTraceService ragTraceService) {
        this.researchAgentService = researchAgentService;
        this.writerAgentService = writerAgentService;
        this.ragTraceService = ragTraceService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ReportGenerateResponse> generate(@RequestBody ReportGenerateRequest request) {
        long startedAt = System.nanoTime();
        String traceId = UUID.randomUUID().toString();
        ResearchBrief brief = null;
        String originalQuestion = request.question();
        String category = request.category();
        long writerDurationMs = 0L;
        long outputDurationMs = 0L;
        List<TraceTimelineStep> researchTimeline = List.of();

        try {
            ResearchAgentService.TimedResearchBrief timedBrief = researchAgentService.buildTimedBrief(originalQuestion, category);
            brief = timedBrief.brief();
            researchTimeline = timedBrief.timeline();

            long phaseStartedAt = System.nanoTime();
            String reportMarkdown = writerAgentService.writeReport(brief);
            writerDurationMs = elapsedMs(phaseStartedAt);

            phaseStartedAt = System.nanoTime();
            ReportGenerateResponse response = new ReportGenerateResponse(brief, reportMarkdown, traceId);
            outputDurationMs = elapsedMs(phaseStartedAt);

            recordTraceSuccess(originalQuestion, category, traceId, startedAt, brief, researchTimeline, writerDurationMs, outputDurationMs);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            recordTraceFailure(originalQuestion, category, traceId, startedAt, brief, e);
            throw e;
        }
    }

    private void recordTraceSuccess(String originalQuestion,
                                    String category,
                                    String traceId,
                                    long startedAt,
                                    ResearchBrief brief,
                                    List<TraceTimelineStep> researchTimeline,
                                    long writerDurationMs,
                                    long outputDurationMs) {
        try {
            ragTraceService.recordSuccessWithTimeline(
                    "REPORT",
                    null,
                    originalQuestion,
                    brief.rewrittenQuestion(),
                    category,
                    traceId,
                    elapsedMs(startedAt),
                    toRetrievalTraceItems(brief),
                    buildReportTimeline(brief, researchTimeline, writerDurationMs, outputDurationMs)
            );
        } catch (RuntimeException e) {
            log.warn("Report trace persistence failed - traceId={}", traceId, e);
        }
    }

    private void recordTraceFailure(String originalQuestion,
                                    String category,
                                    String traceId,
                                    long startedAt,
                                    ResearchBrief brief,
                                    Throwable error) {
        try {
            ragTraceService.recordFailure(
                    "REPORT",
                    null,
                    originalQuestion,
                    brief != null ? brief.rewrittenQuestion() : originalQuestion,
                    category,
                    traceId,
                    elapsedMs(startedAt),
                    error.getMessage()
            );
        } catch (RuntimeException e) {
            log.warn("Report trace failure persistence failed - traceId={}", traceId, e);
        }
    }

    private List<RetrievalTraceItem> toRetrievalTraceItems(ResearchBrief brief) {
        List<SourceReference> sources = new ArrayList<>();
        sources.addAll(brief.kbEvidence());
        sources.addAll(brief.webEvidence());

        List<RetrievalTraceItem> traceItems = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            SourceReference source = sources.get(i);
            traceItems.add(new RetrievalTraceItem(
                    source.getRank() != null ? source.getRank() : i + 1,
                    source.getDocumentId(),
                    source.getDocumentTitle(),
                    source.getFileName(),
                    source.getCategory(),
                    source.getChunkIndex(),
                    source.getScore(),
                    truncate(source.getRelevantContent(), 240)
            ));
        }
        return traceItems;
    }

    private List<TraceTimelineStep> buildReportTimeline(ResearchBrief brief,
                                                        List<TraceTimelineStep> researchTimeline,
                                                        long writerDurationMs,
                                                        long outputDurationMs) {
        int kbEvidenceCount = brief.kbEvidence().size();
        int webEvidenceCount = brief.webEvidence().size();
        int totalEvidenceCount = kbEvidenceCount + webEvidenceCount;
        List<TraceTimelineStep> timeline = new ArrayList<>(researchTimeline == null ? List.of() : researchTimeline);
        timeline.add(new TraceTimelineStep("Writer Agent", "DONE", "写作 Agent 已组织报告结构与结论。", totalEvidenceCount, Math.max(0, writerDurationMs)));
        timeline.add(new TraceTimelineStep("Report Output", "DONE", "报告响应已生成。", totalEvidenceCount, Math.max(0, outputDurationMs)));
        return timeline;
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private long elapsedMs(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }

    private record ReportGenerateRequest(String question, String category) {
    }

    private record ReportGenerateResponse(ResearchBrief brief, String reportMarkdown, String traceId) {
    }
}
