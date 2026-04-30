package com.zhida.aiagent.model.enums;

import lombok.Getter;

/**
 * 消息角色枚举
 */
@Getter
public enum MessageRole {

    USER("用户"),
    ASSISTANT("助手"),
    SYSTEM("系统");

    private final String description;

    MessageRole(String description) {
        this.description = description;
    }
}
