package com.zhida.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.Query;

import java.util.List;
import java.util.function.Consumer;

/**
 * 带来源追踪的文档检索器
 * 包装 VectorStoreDocumentRetriever，在检索后捕获文档到 ThreadLocal
 */
@Slf4j
public class SourceTracingDocumentRetriever implements DocumentRetriever {

    private final DocumentRetriever delegate;
    private final Consumer<List<Document>> sourceConsumer;

    public SourceTracingDocumentRetriever(DocumentRetriever delegate) {
        this(delegate, SourceContextHolder::setSources);
    }

    public SourceTracingDocumentRetriever(DocumentRetriever delegate,
                                          Consumer<List<Document>> sourceConsumer) {
        this.delegate = delegate;
        this.sourceConsumer = sourceConsumer;
    }

    @Override
    public List<Document> retrieve(Query query) {
        log.info("RAG 检索查询: {}", query.text());
        List<Document> documents = delegate.retrieve(query);
        log.info("RAG 检索命中: {} 条文档", documents.size());
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String text = doc.getText();
            String preview = text != null && text.length() > 80
                    ? text.substring(0, 80) + "..."
                    : text;
            log.info("  [{}] 来源: {} | 内容预览: {}", i,
                    doc.getMetadata().get("documentTitle"), preview);
        }
        sourceConsumer.accept(documents);
        return documents;
    }
}
