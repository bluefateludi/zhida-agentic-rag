package com.zhida.aiagent.chatmemory;

import com.zhida.aiagent.model.entity.ChatMessage;
import com.zhida.aiagent.model.enums.MessageRole;
import com.zhida.aiagent.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于数据库的聊天记忆存储
 */
@Slf4j
@Component
public class DatabaseChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMessageRepository chatMessageRepository;

    public DatabaseChatMemoryRepository(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public List<String> findConversationIds() {
        return chatMessageRepository.findAll().stream()
                .map(ChatMessage::getSessionId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(conversationId);
            chatMessage.setRole(toRole(message.getMessageType()));
            chatMessage.setContent(message.getText());
            chatMessageRepository.save(chatMessage);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        chatMessageRepository.deleteBySessionId(conversationId);
    }

    private Message toMessage(ChatMessage chatMessage) {
        return switch (chatMessage.getRole()) {
            case USER -> new UserMessage(chatMessage.getContent());
            case ASSISTANT -> new AssistantMessage(chatMessage.getContent());
            case SYSTEM -> new SystemMessage(chatMessage.getContent());
        };
    }

    private MessageRole toRole(MessageType messageType) {
        return switch (messageType) {
            case USER -> MessageRole.USER;
            case ASSISTANT -> MessageRole.ASSISTANT;
            case SYSTEM -> MessageRole.SYSTEM;
            case TOOL -> MessageRole.ASSISTANT;
        };
    }
}
