package com.zhida.aiagent.rag;

import com.zhida.aiagent.model.dto.RetrievalTraceItem;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RagTraceCollectorTest {

    @Test
    void shouldCaptureRetrievedDocumentsAsRankedTraceItems() {
        Document document = new Document(
                "这是一段很长的 RAG 召回内容，用来验证预览文本会被保留，并且来源元数据可以被结构化记录。",
                Map.of(
                        "documentId", 7L,
                        "documentTitle", "Agent 知识库",
                        "fileName", "agent.md",
                        "category", "agent",
                        "chunkIndex", 3,
                        "score", 0.86
                )
        );
        RagTraceCollector collector = new RagTraceCollector();

        collector.capture(List.of(document));

        List<RetrievalTraceItem> items = collector.toTraceItems();
        assertThat(collector.getDocuments()).containsExactly(document);
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().rank()).isEqualTo(1);
        assertThat(items.getFirst().documentId()).isEqualTo(7L);
        assertThat(items.getFirst().documentTitle()).isEqualTo("Agent 知识库");
        assertThat(items.getFirst().fileName()).isEqualTo("agent.md");
        assertThat(items.getFirst().category()).isEqualTo("agent");
        assertThat(items.getFirst().chunkIndex()).isEqualTo(3);
        assertThat(items.getFirst().score()).isEqualTo(0.86);
        assertThat(items.getFirst().contentPreview()).contains("RAG 召回内容");
    }
}
