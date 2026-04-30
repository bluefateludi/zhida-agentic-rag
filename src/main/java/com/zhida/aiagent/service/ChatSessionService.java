package com.zhida.aiagent.service;

import com.zhida.aiagent.model.entity.ChatSession;
import com.zhida.aiagent.model.enums.ChatMode;
import com.zhida.aiagent.repository.ChatMessageRepository;
import com.zhida.aiagent.repository.ChatSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 聊天会话管理服务
 */
@Slf4j
@Service
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatSessionService(ChatSessionRepository chatSessionRepository,
                              ChatMessageRepository chatMessageRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 创建新会话
     */
    public ChatSession createSession(String title) {
        return createSession(title, ChatMode.CHAT);
    }

    public ChatSession createSession(String title, ChatMode mode) {
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setTitle(title != null ? title : "新对话");
        session.setMode(mode != null ? mode : ChatMode.CHAT);
        return chatSessionRepository.save(session);
    }

    /**
     * 获取所有会话（按更新时间倒序）
     */
    public List<ChatSession> listSessions() {
        return listSessions(ChatMode.CHAT);
    }

    public List<ChatSession> listSessions(ChatMode mode) {
        ChatMode targetMode = mode != null ? mode : ChatMode.CHAT;
        if (targetMode == ChatMode.CHAT) {
            return chatSessionRepository.findByModeOrModeIsNullOrderByUpdatedAtDesc(ChatMode.CHAT);
        }
        return chatSessionRepository.findByModeOrderByUpdatedAtDesc(targetMode);
    }

    /**
     * 重命名会话
     */
    public ChatSession renameSession(String sessionId, String title) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionId));
        session.setTitle(title);
        return chatSessionRepository.save(session);
    }

    /**
     * 删除会话（级联删除消息）
     */
    @Transactional
    public void deleteSession(String sessionId) {
        chatMessageRepository.deleteBySessionId(sessionId);
        chatSessionRepository.deleteById(sessionId);
        log.info("会话已删除: {}", sessionId);
    }

    /**
     * 获取会话
     */
    public ChatSession getSession(String sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionId));
    }

    public ChatSession getSession(String sessionId, ChatMode mode) {
        ChatSession session = getSession(sessionId);
        ChatMode expectedMode = mode != null ? mode : ChatMode.CHAT;
        ChatMode actualMode = session.getMode() != null ? session.getMode() : ChatMode.CHAT;
        if (actualMode != expectedMode) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }
        return session;
    }
}
