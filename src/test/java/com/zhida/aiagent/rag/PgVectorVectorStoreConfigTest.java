package com.zhida.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("智答 AI 可以用于企业知识库问答和课程设计答辩", Map.of("meta1", "meta1")),
                new Document("RAG 系统需要支持文档检索、来源追溯和回答生成"),
                new Document("知识图谱可以辅助解释文档之间的关联", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("知识库问答怎么追溯来源").topK(3).build());
        Assertions.assertNotNull(results);
    }
}
