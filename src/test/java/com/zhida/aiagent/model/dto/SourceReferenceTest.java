package com.zhida.aiagent.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SourceReferenceTest {

    @Test
    void shouldSupportDocumentIdAndKeepLegacyConstructorCompatible() {
        SourceReference withDocumentId = new SourceReference(12L, "MCP文章", "mcp.md", "这里是摘要", 0.87);
        SourceReference legacy = new SourceReference("旧文章", "old.md", "旧摘要", null);

        assertEquals(12L, withDocumentId.getDocumentId());
        assertEquals("MCP文章", withDocumentId.getDocumentTitle());
        assertNull(legacy.getDocumentId());
    }

    @Test
    void shouldSupportRetrievalMetadataForTraceableCitations() {
        SourceReference source = new SourceReference(12L, "RAG文章", "rag.md", "这里是召回片段", 0.91);

        source.setCategory("agent");
        source.setChunkIndex(3);
        source.setRank(1);
        source.setTraceId("trace-001");

        assertEquals("agent", source.getCategory());
        assertEquals(3, source.getChunkIndex());
        assertEquals(1, source.getRank());
        assertEquals("trace-001", source.getTraceId());
    }
}
