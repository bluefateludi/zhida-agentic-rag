package com.zhida.aiagent.model.dto;

import lombok.Data;

/**
 * 聊天请求 DTO
 */
@Data
public class ChatRequest {

    private String message;

    private String sessionId;

    /**
     * 可选：限定知识库分类
     */
    private String category;
}
