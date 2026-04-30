package com.zhida.aiagent.model.enums;

import lombok.Getter;

/**
 * 文档处理状态枚举
 */
@Getter
public enum DocumentStatus {

    PROCESSING("处理中"),
    READY("就绪"),
    ERROR("错误");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }
}
