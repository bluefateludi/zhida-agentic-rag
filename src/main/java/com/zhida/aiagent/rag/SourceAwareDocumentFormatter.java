package com.zhida.aiagent.rag;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.function.Function;

/**
 * 将检索到的文档格式化为带来源元信息的上下文，便于模型在回答中引用来源。
 */
public class SourceAwareDocumentFormatter implements Function<List<Document>, String> {

    private static final String UNKNOWN_DOCUMENT = "未命名文档";

    @Override
    public String apply(List<Document> documents) {
        StringBuilder contextBuilder = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            String documentTitle = getMetadata(document, "documentTitle");
            String fileName = getMetadata(document, "fileName");
            String displayName = !documentTitle.isBlank()
                    ? documentTitle
                    : (!fileName.isBlank() ? fileName : UNKNOWN_DOCUMENT);
            String content = document.getText() != null ? document.getText() : "";

            if (i > 0) {
                contextBuilder.append("\n\n");
            }

            contextBuilder.append("【来源 ").append(i + 1).append("】\n")
                    .append("文档标题: ").append(displayName).append("\n");

            if (!fileName.isBlank()) {
                contextBuilder.append("文件名: ").append(fileName).append("\n");
            }

            contextBuilder.append("文档内容:\n")
                    .append(content);
        }

        return contextBuilder.toString();
    }

    private String getMetadata(Document document, String key) {
        Object value = document.getMetadata().get(key);
        return value instanceof String stringValue ? stringValue : "";
    }
}
