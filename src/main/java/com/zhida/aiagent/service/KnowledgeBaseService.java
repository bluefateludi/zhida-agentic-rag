package com.zhida.aiagent.service;

import com.zhida.aiagent.model.entity.KbDocument;
import com.zhida.aiagent.model.enums.DocumentStatus;
import com.zhida.aiagent.rag.MyTokenTextSplitter;
import com.zhida.aiagent.repository.KbDocumentRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识库文档管理服务
 */
@Slf4j
@Service
public class KnowledgeBaseService {

    @Resource
    private KbDocumentRepository kbDocumentRepository;

    @Resource
    private DocumentParser documentParser;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${kb.upload-dir:${user.dir}/uploads/kb}")
    private String uploadDir;

    /**
     * 上传文档：解析 → 分块 → 向量化 → 存储
     */
    public KbDocument uploadDocument(MultipartFile file, String title, String category) {
        // 1. 保存文件到磁盘
        String originalFilename = file.getOriginalFilename();
        String fileType = getFileExtension(originalFilename);
        String filePath = saveFile(file);

        // 2. 创建文档记录
        KbDocument kbDocument = new KbDocument();
        kbDocument.setTitle(title != null ? title : originalFilename);
        kbDocument.setFileName(originalFilename);
        kbDocument.setFileType(fileType);
        kbDocument.setFileSize(file.getSize());
        kbDocument.setFilePath(filePath);
        kbDocument.setCategory(category != null ? category : "default");
        kbDocument.setStatus(DocumentStatus.PROCESSING);
        kbDocument = kbDocumentRepository.save(kbDocument);

        // 3. 异步处理：解析 → 分块 → 向量化
        try {
            processDocument(kbDocument);
        } catch (Exception e) {
            log.error("文档处理失败: {}", kbDocument.getFileName(), e);
            kbDocument.setStatus(DocumentStatus.ERROR);
            kbDocument.setErrorMessage(e.getMessage());
            kbDocumentRepository.save(kbDocument);
        }

        return kbDocument;
    }

    /**
     * 处理文档：解析 → 分块 → 添加元信息 → 向量化
     */
    private void processDocument(KbDocument kbDocument) {
        File file = new File(kbDocument.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件不存在: " + kbDocument.getFilePath());
        }

        // 解析文档
        List<Document> documents = documentParser.parse(file, kbDocument.getFileType());
        if (documents.isEmpty()) {
            throw new RuntimeException("文档解析结果为空");
        }

        // 添加文档元信息（用于来源追溯和按文档删除）
        List<Document> enrichedDocuments = new ArrayList<>();
        for (Document doc : documents) {
            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
            metadata.put("documentId", kbDocument.getId());
            metadata.put("documentTitle", kbDocument.getTitle());
            metadata.put("fileName", kbDocument.getFileName());
            metadata.put("category", kbDocument.getCategory());
            enrichedDocuments.add(new Document(doc.getText(), metadata));
        }

        // 分块
        List<Document> chunks = myTokenTextSplitter.splitCustomized(enrichedDocuments);
        List<Document> indexedChunks = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
            metadata.put("chunkIndex", i + 1);
            indexedChunks.add(new Document(chunk.getText(), metadata));
        }
        chunks = indexedChunks;

        // 向量化并存储（分批处理，DashScope 每次最多 25 条）
        int batchSize = 20;
        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<Document> batch = chunks.subList(i, end);
            pgVectorVectorStore.add(batch);
            log.info("向量化进度: {}/{}", end, chunks.size());
        }

        // 更新文档状态
        kbDocument.setStatus(DocumentStatus.READY);
        kbDocument.setChunkCount(chunks.size());
        kbDocumentRepository.save(kbDocument);

        log.info("文档处理完成: {} → {} 个分块", kbDocument.getFileName(), chunks.size());
    }

    /**
     * 删除文档（同时删除向量数据）
     */
    public void deleteDocument(Long documentId) {
        KbDocument kbDocument = kbDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + documentId));

        // 删除 pgvector 中的向量数据（按 metadata 中的 documentId 过滤）
        jdbcTemplate.update(
                "DELETE FROM vector_store WHERE metadata->>'documentId' = ?",
                String.valueOf(documentId)
        );

        // 删除磁盘文件
        if (kbDocument.getFilePath() != null) {
            File file = new File(kbDocument.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }

        // 删除数据库记录
        kbDocumentRepository.delete(kbDocument);
        log.info("文档已删除: {}", kbDocument.getFileName());
    }

    /**
     * 获取文档列表
     */
    public List<KbDocument> listDocuments(String category) {
        if (category != null && !category.isBlank()) {
            return kbDocumentRepository.findByCategoryOrderByCreatedAtDesc(category);
        }
        return kbDocumentRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 获取文档详情
     */
    public KbDocument getDocument(Long id) {
        return kbDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在: " + id));
    }

    private String saveFile(MultipartFile file) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            String uniqueName = UUID.randomUUID().toString().substring(0, 8) + "_" + originalFilename;
            File dest = new File(dir, uniqueName);
            // 使用 getAbsolutePath() 确保路径正确
            dest = new File(dest.getAbsolutePath());
            file.transferTo(dest);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "TXT";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
    }
}
