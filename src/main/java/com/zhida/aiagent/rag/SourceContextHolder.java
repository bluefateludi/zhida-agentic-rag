package com.zhida.aiagent.rag;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 线程级别的来源文档持有器，用于在 RAG 检索过程中捕获来源
 */
public class SourceContextHolder {

    private static final ThreadLocal<List<Document>> SOURCES = new ThreadLocal<>();

    public static void setSources(List<Document> documents) {
        SOURCES.set(documents);
    }

    public static List<Document> getSources() {
        return SOURCES.get();
    }

    public static void clear() {
        SOURCES.remove();
    }
}
