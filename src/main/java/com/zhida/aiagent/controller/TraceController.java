package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.entity.RagTraceLog;
import com.zhida.aiagent.service.RagTraceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only RAG trace observability API.
 */
@RestController
@RequestMapping("/traces")
public class TraceController {

    private final RagTraceService ragTraceService;

    public TraceController(RagTraceService ragTraceService) {
        this.ragTraceService = ragTraceService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RagTraceLog>> listRecent(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ragTraceService.listRecent(limit));
    }

    @GetMapping("/{traceId}")
    public ResponseEntity<RagTraceLog> getTrace(@PathVariable String traceId) {
        return ragTraceService.getTrace(traceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
