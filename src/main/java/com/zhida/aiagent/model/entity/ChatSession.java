package com.zhida.aiagent.model.entity;

import com.zhida.aiagent.model.enums.ChatMode;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 */
@Data
@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ChatMode mode;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (title == null) {
            title = "新对话";
        }
        if (mode == null) {
            mode = ChatMode.CHAT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
