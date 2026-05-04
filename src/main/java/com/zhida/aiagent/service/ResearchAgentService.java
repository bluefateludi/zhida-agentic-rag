package com.zhida.aiagent.service;

import com.zhida.aiagent.model.dto.ResearchBrief;
import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.model.dto.TraceTimelineStep;
import com.zhida.aiagent.rag.QueryRewriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 研究 Agent，负责问题改写与证据整理。
 */
@Slf4j
@Service
public class ResearchAgentService {

    private final VectorStore pgVectorVectorStore;
    private final QueryRewriter queryRewriter;
    private final WebResearchService webResearchService;

    public ResearchAgentService(VectorStore pgVectorVectorStore,
                                QueryRewriter queryRewriter,
                                WebResearchService webResearchService) {
        this.pgVectorVectorStore = pgVectorVectorStore;
        this.queryRewriter = queryRewriter;
        this.webResearchService = webResearchService;
    }

    public ResearchBrief buildBrief(String originalQuestion, String category) {
        return buildTimedBrief(originalQuestion, category).brief();
    }

    public TimedResearchBrief buildTimedBrief(String originalQuestion, String category) {
        long phaseStartedAt = System.nanoTime();
        String rewrittenQuestion = queryRewriter.doQueryRewrite(originalQuestion);
        long queryRewriteDurationMs = elapsedMs(phaseStartedAt);

        phaseStartedAt = System.nanoTime();
        List<SourceReference> kbEvidence = retrieveKnowledgeBaseEvidence(rewrittenQuestion, category);
        long kbRetrievalDurationMs = elapsedMs(phaseStartedAt);

        boolean useWebResearch = shouldUseWebResearch(originalQuestion, rewrittenQuestion, kbEvidence);
        phaseStartedAt = System.nanoTime();
        List<SourceReference> webEvidence = useWebResearch
                ? retrieveWebEvidence(rewrittenQuestion, kbEvidence.size())
                : List.of();
        long webResearchDurationMs = useWebResearch ? elapsedMs(phaseStartedAt) : 0L;

        ResearchBrief brief = new ResearchBrief(
                originalQuestion,
                rewrittenQuestion,
                buildSubQuestions(rewrittenQuestion),
                kbEvidence,
                webEvidence,
                buildInformationGaps(kbEvidence, webEvidence, useWebResearch),
                Instant.now()
        );
        return new TimedResearchBrief(
                brief,
                List.of(
                        new TraceTimelineStep("Query Rewrite", "DONE", "问题已改写为研究报告检索表达。", null, queryRewriteDurationMs),
                        new TraceTimelineStep("KB Retrieval", "DONE", evidenceSummary(kbEvidence.size(), "知识库证据"), kbEvidence.size(), kbRetrievalDurationMs),
                        new TraceTimelineStep("Web Research", "DONE", evidenceSummary(webEvidence.size(), "公开网络证据"), webEvidence.size(), webResearchDurationMs)
                )
        );
    }

    private List<SourceReference> retrieveKnowledgeBaseEvidence(String question, String category) {
        try {
            SearchRequest.Builder builder = SearchRequest.builder()
                    .query(question)
                    .topK(6)
                    .similarityThreshold(0.3);
            if (category != null && !category.isBlank()) {
                builder.filterExpression(new FilterExpressionBuilder().eq("category", category).build());
            }

            List<Document> docs = pgVectorVectorStore.similaritySearch(builder.build());
            if (docs == null || docs.isEmpty()) {
                return List.of();
            }

            List<SourceReference> evidence = new ArrayList<>();
            for (int i = 0; i < docs.size(); i++) {
                Document doc = docs.get(i);
                String text = doc.getText() != null ? doc.getText() : "";
                SourceReference source = new SourceReference(
                        parseDocumentId(doc.getMetadata().get("documentId")),
                        (String) doc.getMetadata().get("documentTitle"),
                        (String) doc.getMetadata().get("fileName"),
                        text.length() > 240 ? text.substring(0, 240) + "..." : text,
                        null
                );
                source.setCategory((String) doc.getMetadata().get("category"));
                source.setChunkIndex(parseInteger(doc.getMetadata().get("chunkIndex")));
                source.setRank(i + 1);
                evidence.add(source);
            }
            return evidence;
        } catch (Exception e) {
            log.warn("研究模式知识库检索失败，回退为空证据: question={}, error={}", question, e.toString());
            return List.of();
        }
    }

    private List<SourceReference> retrieveWebEvidence(String rewrittenQuestion, int existingEvidenceCount) {
        WebResearchService.WebResearchResult result = webResearchService.research(rewrittenQuestion);
        if (result == null || result.sources() == null || result.sources().isEmpty()) {
            return List.of();
        }

        List<SourceReference> evidence = new ArrayList<>();
        for (int i = 0; i < result.sources().size(); i++) {
            SourceReference raw = result.sources().get(i);
            SourceReference source = new SourceReference(
                    raw.getDocumentId(),
                    raw.getDocumentTitle(),
                    raw.getFileName(),
                    raw.getRelevantContent(),
                    raw.getScore()
            );
            source.setCategory(raw.getCategory() != null ? raw.getCategory() : "web");
            source.setChunkIndex(raw.getChunkIndex());
            source.setRank(existingEvidenceCount + i + 1);
            source.setTraceId(raw.getTraceId());
            evidence.add(source);
        }
        return evidence;
    }

    private List<String> buildSubQuestions(String rewrittenQuestion) {
        return List.of(
                "这个问题的核心判断是什么？",
                "有哪些来自知识库或外部资料的关键证据？",
                "还缺少哪些信息需要进一步验证？"
        );
    }

    private List<String> buildInformationGaps(List<SourceReference> kbEvidence,
                                              List<SourceReference> webEvidence,
                                              boolean usedWebResearch) {
        List<String> gaps = new ArrayList<>();
        if (kbEvidence.isEmpty()) {
            gaps.add("知识库中缺少足够的直接证据");
        } else if (kbEvidence.size() < 2) {
            gaps.add("知识库证据较少，结论稳定性仍需补充更多内部材料");
        }
        if (usedWebResearch && webEvidence.isEmpty()) {
            gaps.add("公开网络资料未提供足够补充证据");
        }
        if (usedWebResearch && !webEvidence.isEmpty()) {
            gaps.add("结论依赖公开网络资料，仍需结合内部知识库进一步核验");
        }
        return gaps;
    }

    private boolean shouldUseWebResearch(String originalQuestion,
                                         String rewrittenQuestion,
                                         List<SourceReference> kbEvidence) {
        return kbEvidence.size() < 2 || containsFreshnessSignal(originalQuestion) || containsFreshnessSignal(rewrittenQuestion);
    }

    private boolean containsFreshnessSignal(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        String lower = question.toLowerCase();
        return lower.contains("最新")
                || lower.contains("最近")
                || lower.contains("趋势")
                || lower.contains("行业")
                || lower.contains("市场")
                || lower.contains("竞品")
                || lower.contains("news")
                || lower.contains("latest")
                || lower.contains("trend");
    }

    private long elapsedMs(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }

    private String evidenceSummary(int evidenceCount, String label) {
        return evidenceCount > 0
                ? "已记录 " + evidenceCount + " 条" + label + "。"
                : "未记录" + label + "。";
    }

    public record TimedResearchBrief(ResearchBrief brief, List<TraceTimelineStep> timeline) {
        public TimedResearchBrief {
            timeline = timeline == null ? List.of() : List.copyOf(timeline);
        }
    }

    private Long parseDocumentId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
