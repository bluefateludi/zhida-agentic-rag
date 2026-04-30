package com.zhida.aiagent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天响应 DTO（含来源追溯）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * AI 回答内容
     */
    private String content;

    /**
     * 来源引用列表
     */
    private List<SourceReference> sources;
}
