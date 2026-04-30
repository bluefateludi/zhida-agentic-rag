package com.zhida.aiagent.rag;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeBaseRagConfigTest {

    @Test
    void pmRagAdvisorShouldAllowEmptyContext() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java"
        ));

        assertThat(source).contains("createPmRagAdvisor");
        assertThat(source).contains("createRagAdvisor(vectorStore, category, SourceContextHolder::setSources, true)");
        assertThat(source).contains("allowEmptyContext(allowEmptyContext)");
    }
}
