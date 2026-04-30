package com.zhida.aiagent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceDocumentCollectorTest {

    @Test
    void shouldKeepRetrievedDocumentsAcrossAsyncBoundary() {
        SourceDocumentCollector collector = new SourceDocumentCollector();
        Document document = new Document("RAG 内容", Map.of("documentTitle", "RAG 文档"));

        CompletableFuture.runAsync(() -> collector.capture(List.of(document))).join();

        assertEquals(List.of(document), collector.getDocuments());
    }
}
