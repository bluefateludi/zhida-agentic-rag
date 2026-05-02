package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.dto.ResearchBrief;
import com.zhida.aiagent.service.ResearchAgentService;
import com.zhida.aiagent.service.WriterAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 研究报告模式接口。
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    private final ResearchAgentService researchAgentService;
    private final WriterAgentService writerAgentService;

    public ReportController(ResearchAgentService researchAgentService,
                            WriterAgentService writerAgentService) {
        this.researchAgentService = researchAgentService;
        this.writerAgentService = writerAgentService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ReportGenerateResponse> generate(@RequestBody ReportGenerateRequest request) {
        ResearchBrief brief = researchAgentService.buildBrief(request.question(), request.category());
        String reportMarkdown = writerAgentService.writeReport(brief);
        return ResponseEntity.ok(new ReportGenerateResponse(brief, reportMarkdown));
    }

    private record ReportGenerateRequest(String question, String category) {
    }

    private record ReportGenerateResponse(ResearchBrief brief, String reportMarkdown) {
    }
}
