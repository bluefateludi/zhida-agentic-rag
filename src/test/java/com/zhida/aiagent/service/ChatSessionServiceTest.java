package com.zhida.aiagent.service;

import com.zhida.aiagent.model.entity.ChatSession;
import com.zhida.aiagent.model.enums.ChatMode;
import com.zhida.aiagent.repository.ChatMessageRepository;
import com.zhida.aiagent.repository.ChatSessionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatSessionServiceTest {

    private final ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
    private final ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
    private final ChatSessionService chatSessionService = new ChatSessionService(
            chatSessionRepository,
            chatMessageRepository
    );

    @Test
    void createSessionShouldPersistRequestedMode() {
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSession session = chatSessionService.createSession(null, ChatMode.PM);

        assertThat(session.getMode()).isEqualTo(ChatMode.PM);
        assertThat(session.getTitle()).isEqualTo("新对话");
    }

    @Test
    void listSessionsShouldFilterByMode() {
        ChatSession pmSession = new ChatSession();
        pmSession.setMode(ChatMode.PM);
        when(chatSessionRepository.findByModeOrderByUpdatedAtDesc(ChatMode.PM)).thenReturn(List.of(pmSession));

        List<ChatSession> sessions = chatSessionService.listSessions(ChatMode.PM);

        assertThat(sessions).containsExactly(pmSession);
        verify(chatSessionRepository).findByModeOrderByUpdatedAtDesc(ChatMode.PM);
    }

    @Test
    void listSessionsShouldKeepLegacyRowsWithoutModeInDefaultChatMode() {
        ChatSession legacySession = new ChatSession();
        when(chatSessionRepository.findByModeOrModeIsNullOrderByUpdatedAtDesc(ChatMode.CHAT))
                .thenReturn(List.of(legacySession));

        List<ChatSession> sessions = chatSessionService.listSessions();

        assertThat(sessions).containsExactly(legacySession);
        verify(chatSessionRepository).findByModeOrModeIsNullOrderByUpdatedAtDesc(ChatMode.CHAT);
    }

    @Test
    void getSessionShouldRejectSessionFromAnotherMode() {
        ChatSession chatSession = new ChatSession();
        chatSession.setId("chat-session");
        chatSession.setMode(ChatMode.CHAT);
        when(chatSessionRepository.findById("chat-session")).thenReturn(Optional.of(chatSession));

        assertThatThrownBy(() -> chatSessionService.getSession("chat-session", ChatMode.PM))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("会话不存在");
    }
}
