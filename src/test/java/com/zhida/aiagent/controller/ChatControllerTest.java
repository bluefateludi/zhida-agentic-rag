package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.service.ChatService;
import com.zhida.aiagent.service.ChatSessionService;
import com.zhida.aiagent.service.ChatStreamEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ChatSessionService chatSessionService;

    @Test
    void shouldEmitCompleteEventWithSourcesForStreamChat() throws Exception {
        when(chatService.chatStream("MCP文章讲了什么", "session-1", null))
                .thenReturn(Flux.just(
                        ChatStreamEvent.message("第一段"),
                        ChatStreamEvent.complete(
                                "第一段",
                                List.of(new SourceReference("MCP文章", "mcp.md", "这里是摘要", null))
                        )
                ));

        MvcResult mvcResult = mockMvc.perform(get("/chat/stream")
                        .param("message", "MCP文章讲了什么")
                        .param("sessionId", "session-1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("event:message")))
                .andExpect(content().string(containsString("event:complete")))
                .andExpect(content().string(containsString("\"fileName\":\"mcp.md\"")));
    }
}
