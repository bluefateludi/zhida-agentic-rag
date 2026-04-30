package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.dto.EvalCase;
import com.zhida.aiagent.model.dto.EvalRunResult;
import com.zhida.aiagent.service.RagEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RAG golden-set evaluation API.
 */
@RestController
@RequestMapping("/eval")
public class EvaluationController {

    private final RagEvaluationService ragEvaluationService;

    public EvaluationController(RagEvaluationService ragEvaluationService) {
        this.ragEvaluationService = ragEvaluationService;
    }

    @GetMapping("/cases")
    public ResponseEntity<List<EvalCase>> listCases() {
        return ResponseEntity.ok(ragEvaluationService.listCases());
    }

    @PostMapping("/run")
    public ResponseEntity<List<EvalRunResult>> run(@RequestBody(required = false) EvalRunRequest request) {
        List<String> caseIds = request == null ? List.of() : request.caseIds();
        return ResponseEntity.ok(ragEvaluationService.run(caseIds));
    }

    private record EvalRunRequest(List<String> caseIds) {
    }
}
