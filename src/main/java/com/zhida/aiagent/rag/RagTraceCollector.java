package com.zhida.aiagent.rag;

import com.zhida.aiagent.model.dto.RetrievalTraceItem;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

/**
 * Request-scoped collector that turns retrieved documents into trace items.
 */
public class RagTraceCollector {

    private final AtomicReference<List<Document>> documents = new AtomicReference<>(List.of());

    public void capture(List<Document> retrievedDocuments) {
        documents.set(retrievedDocuments == null ? List.of() : List.copyOf(retrievedDocuments));
    }

    public List<Document> getDocuments() {
        return documents.get();
    }

    public List<RetrievalTraceItem> toTraceItems() {
        List<Document> currentDocuments = documents.get();
        return IntStream.range(0, currentDocuments.size())
                .mapToObj(index -> toTraceItem(currentDocuments.get(index), index + 1))
                .toList();
    }

    private RetrievalTraceItem toTraceItem(Document document, int rank) {
        String text = document.getText() == null ? "" : document.getText();
        return new RetrievalTraceItem(
                rank,
                parseLong(document.getMetadata().get("documentId")),
                (String) document.getMetadata().get("documentTitle"),
                (String) document.getMetadata().get("fileName"),
                (String) document.getMetadata().get("category"),
                parseInteger(document.getMetadata().get("chunkIndex")),
                parseDouble(document.getMetadata().get("score")),
                text.length() > 240 ? text.substring(0, 240) + "..." : text
        );
    }

    private Long parseLong(Object value) {
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

    private Double parseDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
