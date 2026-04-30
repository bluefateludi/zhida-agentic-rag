package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.dto.EvalCase;
import com.zhida.aiagent.model.dto.EvalRunResult;
import com.zhida.aiagent.service.RagEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluationController.class)
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RagEvaluationService ragEvaluationService;

    @Test
    void shouldListGoldenSetCases() throws Exception {
        when(ragEvaluationService.listCases()).thenReturn(List.of(
                new EvalCase("case-1", "问题", "agent", List.of("关键词"), true)
        ));

        mockMvc.perform(get("/eval/cases"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("case-1")));
    }

    @Test
    void shouldRunSelectedGoldenSetCases() throws Exception {
        when(ragEvaluationService.run(List.of("case-1"))).thenReturn(List.of(
                new EvalRunResult("case-1", "问题", "回答", List.of("关键词"), List.of("关键词"), 1, true)
        ));

        mockMvc.perform(post("/eval/run")
                        .contentType("application/json")
                        .content("{\"caseIds\":[\"case-1\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"passed\":true")));
    }
}
