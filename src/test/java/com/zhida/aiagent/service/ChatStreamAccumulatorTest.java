package com.zhida.aiagent.service;

import com.zhida.aiagent.model.dto.SourceReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatStreamAccumulatorTest {

    @Test
    void shouldBuildCompleteEventAndPersistAssistantMessage() {
        List<SourceReference> expectedSources = List.of(
                new SourceReference("MCP文章", "mcp.md", "这里是摘要", null)
        );
        AtomicReference<String> savedContent = new AtomicReference<>();
        AtomicReference<List<SourceReference>> savedSources = new AtomicReference<>(new ArrayList<>());

        ChatStreamAccumulator accumulator = new ChatStreamAccumulator(
                () -> expectedSources,
                (content, sources) -> {
                    savedContent.set(content);
                    savedSources.set(sources);
                }
        );

        assertEquals(
                ChatStreamEvent.message("你好，"),
                accumulator.append("你好，")
        );
        assertEquals(
                ChatStreamEvent.message("世界"),
                accumulator.append("世界")
        );

        ChatStreamEvent completionEvent = accumulator.complete();

        assertEquals("complete", completionEvent.event());
        assertEquals("你好，世界", completionEvent.content());
        assertEquals(expectedSources, completionEvent.sources());
        assertEquals("你好，世界", savedContent.get());
        assertEquals(expectedSources, savedSources.get());
    }
}
