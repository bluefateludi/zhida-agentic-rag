package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.service.ChatSessionService;
import com.zhida.aiagent.service.ChatStreamEvent;
import com.zhida.aiagent.service.PmChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PmController.class)
class PmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PmChatService pmChatService;

    @MockBean
    private ChatSessionService chatSessionService;

    @Test
    void shouldEmitCompleteEventWithSourcesForPmStreamChat() throws Exception {
        when(pmChatService.chatStream("帮我判断这个产品方向", "pm-session-1", null))
                .thenReturn(Flux.just(
                        ChatStreamEvent.message("先看结论，"),
                        ChatStreamEvent.complete(
                                "先看结论，值得做。",
                                List.of(new SourceReference("36kr", "https://36kr.com/p/1", "这是一条联网摘要", null))
                        )
                ));

        MvcResult mvcResult = mockMvc.perform(get("/pm/stream")
                        .param("message", "帮我判断这个产品方向")
                        .param("sessionId", "pm-session-1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("event:message")))
                .andExpect(content().string(containsString("event:complete")))
                .andExpect(content().string(containsString("36kr")));
    }

    @Test
    void shouldEmitReadableFallbackWhenPmStreamFails() throws Exception {
        when(pmChatService.chatStream("什么是一个好产品", "pm-session-1", null))
                .thenReturn(Flux.error(new RuntimeException("model timeout")));

        MvcResult mvcResult = mockMvc.perform(get("/pm/stream")
                        .param("message", "什么是一个好产品")
                        .param("sessionId", "pm-session-1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult dispatched = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("event:complete")))
                .andReturn();

        assertThat(dispatched.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("PM 模式暂时无法完成完整分析");
    }
}
