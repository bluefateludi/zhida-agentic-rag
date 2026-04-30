package com.zhida.aiagent.service;

import com.zhida.aiagent.model.dto.SourceReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 负责累计流式内容，并在结束时构建完成事件和持久化数据。
 */
public class ChatStreamAccumulator {

    private final StringBuilder contentBuilder = new StringBuilder();
    private final Supplier<List<SourceReference>> sourcesSupplier;
    private final BiConsumer<String, List<SourceReference>> completionHandler;

    public ChatStreamAccumulator(Supplier<List<SourceReference>> sourcesSupplier,
                                 BiConsumer<String, List<SourceReference>> completionHandler) {
        this.sourcesSupplier = sourcesSupplier;
        this.completionHandler = completionHandler;
    }

    public ChatStreamEvent append(String chunk) {
        contentBuilder.append(chunk);
        return ChatStreamEvent.message(chunk);
    }

    public ChatStreamEvent complete() {
        String content = contentBuilder.toString();
        List<SourceReference> sources = sourcesSupplier.get();
        completionHandler.accept(content, sources);
        return ChatStreamEvent.complete(content, sources);
    }
}
