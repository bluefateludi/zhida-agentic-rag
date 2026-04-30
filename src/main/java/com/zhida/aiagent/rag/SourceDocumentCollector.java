package com.zhida.aiagent.rag;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Request-scoped holder for RAG source documents.
 */
public class SourceDocumentCollector {

    private final AtomicReference<List<Document>> documents = new AtomicReference<>(List.of());

    public void capture(List<Document> retrievedDocuments) {
        documents.set(retrievedDocuments == null ? List.of() : List.copyOf(retrievedDocuments));
    }

    public List<Document> getDocuments() {
        return documents.get();
    }
}
