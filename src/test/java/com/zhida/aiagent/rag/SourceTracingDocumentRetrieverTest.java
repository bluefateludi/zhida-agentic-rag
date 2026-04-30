package com.zhida.aiagent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceTracingDocumentRetrieverTest {

    @Test
    void shouldCaptureDocumentsWithRequestCollector() {
        Document document = new Document("MCP 内容", Map.of("documentTitle", "MCP 文档"));
        DocumentRetriever delegate = query -> List.of(document);
        AtomicReference<List<Document>> captured = new AtomicReference<>(List.of());

        SourceTracingDocumentRetriever retriever = new SourceTracingDocumentRetriever(delegate, captured::set);

        List<Document> result = retriever.retrieve(new Query("MCP 是什么"));

        assertEquals(List.of(document), result);
        assertEquals(List.of(document), captured.get());
    }
}
