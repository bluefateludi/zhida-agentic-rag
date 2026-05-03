package com.zhida.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

/**
 * 知识库 RAG 配置
 */
@Slf4j
@Configuration
public class KnowledgeBaseRagConfig {

    /**
     * 创建带来源追踪的 RAG Advisor
     * 相似度阈值 0.3（中文 embedding 相似度普遍偏低，阈值过高会导致召回为空）
     * Top-K: 8（适当增加召回数量，给 AI 更多参考上下文）
     */
    public static RetrievalAugmentationAdvisor createRagAdvisor(VectorStore vectorStore, String category) {
        return createRagAdvisor(vectorStore, category, SourceContextHolder::setSources, false);
    }

    public static RetrievalAugmentationAdvisor createRagAdvisor(VectorStore vectorStore,
                                                               String category,
                                                               Consumer<List<Document>> sourceConsumer) {
        return createRagAdvisor(vectorStore, category, sourceConsumer, false);
    }

    public static RetrievalAugmentationAdvisor createPmRagAdvisor(VectorStore vectorStore, String category) {
        return createRagAdvisor(vectorStore, category, SourceContextHolder::setSources, true);
    }

    public static RetrievalAugmentationAdvisor createPmRagAdvisor(VectorStore vectorStore,
                                                                  String category,
                                                                  Consumer<List<Document>> sourceConsumer) {
        return createRagAdvisor(vectorStore, category, sourceConsumer, true);
    }

    private static RetrievalAugmentationAdvisor createRagAdvisor(VectorStore vectorStore,
                                                               String category,
                                                               Consumer<List<Document>> sourceConsumer,
                                                               boolean allowEmptyContext) {
        VectorStoreDocumentRetriever.Builder retrieverBuilder = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.3)
                .topK(8);

        if (category != null && !category.isBlank()) {
            log.info("RAG 检索 - 启用分类过滤: category={}", category);
            retrieverBuilder.filterExpression(
                    new FilterExpressionBuilder().eq("category", category).build()
            );
        } else {
            log.info("RAG 检索 - 无分类过滤，检索全部知识库");
        }

        DocumentRetriever retriever = new SourceTracingDocumentRetriever(retrieverBuilder.build(), sourceConsumer);
        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .documentFormatter(new SourceAwareDocumentFormatter())
                .allowEmptyContext(allowEmptyContext)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryAugmenter(queryAugmenter)
                .build();
    }
}
