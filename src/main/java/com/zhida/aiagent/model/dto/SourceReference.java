package com.zhida.aiagent.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 来源引用 DTO
 */
@Data
@NoArgsConstructor
public class SourceReference {

    /**
     * 知识库文档 ID，用于来源卡片追溯
     */
    private Long documentId;

    /**
     * 文档标题
     */
    private String documentTitle;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 匹配的文本片段
     */
    private String relevantContent;

    /**
     * 相似度分数
     */
    private Double score;

    /**
     * 知识库分类，用于按领域解释来源。
     */
    private String category;

    /**
     * 文档分块序号，用于定位来源片段。
     */
    private Integer chunkIndex;

    /**
     * 本次检索中的排名。
     */
    private Integer rank;

    /**
     * 本次问答链路 ID，用于后续追踪。
     */
    private String traceId;

    public SourceReference(String documentTitle, String fileName, String relevantContent, Double score) {
        this(null, documentTitle, fileName, relevantContent, score);
    }

    public SourceReference(Long documentId,
                           String documentTitle,
                           String fileName,
                           String relevantContent,
                           Double score) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.fileName = fileName;
        this.relevantContent = relevantContent;
        this.score = score;
    }
}
