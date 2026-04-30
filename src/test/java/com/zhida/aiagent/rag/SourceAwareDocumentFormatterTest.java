package com.zhida.aiagent.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

class SourceAwareDocumentFormatterTest {

    @Test
    void shouldIncludeDocumentMetadataInFormattedContext() {
        SourceAwareDocumentFormatter formatter = new SourceAwareDocumentFormatter();
        List<Document> documents = List.of(
                new Document("MCP 文章介绍了模型上下文协议的用途", Map.of(
                        "documentTitle", "MCP 文章",
                        "fileName", "article-mcp.md"
                ))
        );

        String formattedContext = formatter.apply(documents);

        Assertions.assertTrue(formattedContext.contains("MCP 文章"));
        Assertions.assertTrue(formattedContext.contains("article-mcp.md"));
        Assertions.assertTrue(formattedContext.contains("MCP 文章介绍了模型上下文协议的用途"));
    }

    @Test
    void shouldFallbackToUnnamedDocumentWhenMetadataMissing() {
        SourceAwareDocumentFormatter formatter = new SourceAwareDocumentFormatter();
        List<Document> documents = List.of(new Document("只有正文"));

        String formattedContext = formatter.apply(documents);

        Assertions.assertTrue(formattedContext.contains("未命名文档"));
        Assertions.assertTrue(formattedContext.contains("只有正文"));
    }
}
