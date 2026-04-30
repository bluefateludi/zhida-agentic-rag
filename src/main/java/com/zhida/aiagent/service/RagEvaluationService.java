package com.zhida.aiagent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhida.aiagent.model.dto.EvalCase;
import com.zhida.aiagent.model.dto.EvalRunResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Runs deterministic golden-set checks for RAG regression evaluation.
 */
@Slf4j
@Service
public class RagEvaluationService {

    private static final String GOLDEN_SET_PATH = "eval/golden-set.json";

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public RagEvaluationService(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    public List<EvalCase> listCases() {
        try {
            ClassPathResource resource = new ClassPathResource(GOLDEN_SET_PATH);
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("无法读取 RAG 评测用例: " + GOLDEN_SET_PATH, e);
        }
    }

    public List<EvalRunResult> run(List<String> caseIds) {
        Set<String> selectedIds = caseIds == null ? Set.of() : new HashSet<>(caseIds);
        return listCases().stream()
                .filter(evalCase -> selectedIds.isEmpty() || selectedIds.contains(evalCase.id()))
                .map(this::runCase)
                .toList();
    }

    private EvalRunResult runCase(EvalCase evalCase) {
        ChatService.ChatServiceResponse response = chatService.chat(
                evalCase.question(),
                "eval-" + evalCase.id(),
                evalCase.category()
        );
        String answer = response.content() == null ? "" : response.content();
        List<String> matchedKeywords = evalCase.expectedKeywords().stream()
                .filter(answer::contains)
                .toList();
        int sourceCount = response.sources() == null ? 0 : response.sources().size();
        boolean keywordPassed = matchedKeywords.size() == evalCase.expectedKeywords().size();
        boolean sourcePassed = !evalCase.requireSources() || sourceCount > 0;
        boolean passed = keywordPassed && sourcePassed;
        log.info("RAG 评测用例 {}: passed={}, matched={}/{} sources={}",
                evalCase.id(), passed, matchedKeywords.size(), evalCase.expectedKeywords().size(), sourceCount);

        return new EvalRunResult(
                evalCase.id(),
                evalCase.question(),
                answer,
                evalCase.expectedKeywords(),
                matchedKeywords,
                sourceCount,
                passed
        );
    }
}
