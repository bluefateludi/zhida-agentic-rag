package com.zhida.aiagent.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PmChatServicePromptTest {

    @Test
    void pmSystemPromptShouldUseGenericProductAnalysisVoice() throws Exception {
        Field field = PmChatService.class.getDeclaredField("PM_SYSTEM_PROMPT");
        field.setAccessible(true);

        String prompt = (String) field.get(null);

        assertThat(prompt).contains("产品分析助手");
        assertThat(prompt).contains("什么是一个好产品");
        assertThat(prompt).contains("知识库没有命中");
        assertThat(prompt).contains("不能拒答");
        assertThat(prompt).doesNotContain("张一鸣");
        assertThat(prompt).doesNotContain("人格");
    }

    @Test
    void pmChatServiceShouldNotUseAutomaticChatMemoryPersistence() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/zhida/aiagent/service/PmChatService.java"
        ));

        assertThat(source).doesNotContain("MessageChatMemoryAdvisor");
        assertThat(source).doesNotContain("MessageWindowChatMemory");
        assertThat(source).doesNotContain("DatabaseChatMemoryRepository");
    }

    @Test
    void pmChatServiceShouldUsePmRagAdvisor() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/zhida/aiagent/service/PmChatService.java"
        ));

        assertThat(source).contains("KnowledgeBaseRagConfig.createPmRagAdvisor");
    }
}
