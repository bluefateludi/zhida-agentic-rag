package com.zhida.aiagent.repository;

import com.zhida.aiagent.model.entity.ChatSession;
import com.zhida.aiagent.model.enums.ChatMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    List<ChatSession> findAllByOrderByUpdatedAtDesc();

    List<ChatSession> findByModeOrderByUpdatedAtDesc(ChatMode mode);

    List<ChatSession> findByModeOrModeIsNullOrderByUpdatedAtDesc(ChatMode mode);
}
