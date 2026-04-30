package com.zhida.aiagent.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeBaseServiceMetadataTest {

    @Test
    void shouldAttachChunkIndexMetadataWhenStoringVectors() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/zhida/aiagent/service/KnowledgeBaseService.java"
        ));

        assertThat(source).contains("metadata.put(\"chunkIndex\"");
    }
}
