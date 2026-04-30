package com.zhida.aiagent.service;

import com.zhida.aiagent.model.dto.SourceReference;

import java.util.List;

/**
 * 流式聊天事件
 */
public record ChatStreamEvent(String event, String content, List<SourceReference> sources) {

    public ChatStreamEvent {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }

    public static ChatStreamEvent message(String content) {
        return new ChatStreamEvent("message", content, List.of());
    }

    public static ChatStreamEvent complete(String content, List<SourceReference> sources) {
        return new ChatStreamEvent("complete", content, sources);
    }
}
