package com.zhida.aiagent.controller;

import com.zhida.aiagent.model.entity.KbDocument;
import com.zhida.aiagent.service.KnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 知识库文档管理接口
 */
@Slf4j
@RestController
@RequestMapping("/knowledge/document")
public class DocumentController {

    private final KnowledgeBaseService knowledgeBaseService;

    public DocumentController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ResponseEntity<KbDocument> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category) {
        log.info("上传文档: {}, 分类: {}", file.getOriginalFilename(), category);
        KbDocument document = knowledgeBaseService.uploadDocument(file, title, category);
        return ResponseEntity.ok(document);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable Long id) {
        log.info("删除文档: {}", id);
        knowledgeBaseService.deleteDocument(id);
        return ResponseEntity.ok(Map.of("message", "文档已删除"));
    }

    /**
     * 获取文档列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<KbDocument>> listDocuments(
            @RequestParam(value = "category", required = false) String category) {
        List<KbDocument> documents = knowledgeBaseService.listDocuments(category);
        return ResponseEntity.ok(documents);
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<KbDocument> getDocument(@PathVariable Long id) {
        KbDocument document = knowledgeBaseService.getDocument(id);
        return ResponseEntity.ok(document);
    }
}
