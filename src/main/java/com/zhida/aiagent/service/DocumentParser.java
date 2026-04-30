package com.zhida.aiagent.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档解析器（支持 PDF、DOCX、TXT、Markdown）
 */
@Slf4j
@Component
public class DocumentParser {

    /**
     * 根据文件类型解析文档内容
     */
    public List<Document> parse(File file, String fileType) {
        return switch (fileType.toUpperCase()) {
            case "PDF" -> parsePdf(file);
            case "DOCX" -> parseDocx(file);
            case "TXT" -> parseTxt(file);
            case "MD" -> parseTxt(file); // Markdown 按 TXT 处理
            default -> {
                log.warn("不支持的文件类型: {}", fileType);
                yield List.of();
            }
        };
    }

    private List<Document> parsePdf(File file) {
        try (PDDocument pdDocument = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdDocument);
            return createDocuments(text, Map.of("source", file.getName()));
        } catch (IOException e) {
            log.error("解析 PDF 文件失败: {}", file.getName(), e);
            return List.of();
        }
    }

    private List<Document> parseDocx(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            return createDocuments(sb.toString(), Map.of("source", file.getName()));
        } catch (IOException e) {
            log.error("解析 DOCX 文件失败: {}", file.getName(), e);
            return List.of();
        }
    }

    private List<Document> parseTxt(File file) {
        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return createDocuments(text, Map.of("source", file.getName()));
        } catch (IOException e) {
            log.error("解析 TXT/MD 文件失败: {}", file.getName(), e);
            return List.of();
        }
    }

    /**
     * 将文本按段落拆分为多个 Document（避免单文档过大）
     */
    private List<Document> createDocuments(String text, Map<String, Object> baseMetadata) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<Document> documents = new ArrayList<>();
        // 按双换行分段
        String[] paragraphs = text.split("\\n\\s*\\n");
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (!paragraph.isEmpty()) {
                Map<String, Object> metadata = new HashMap<>(baseMetadata);
                metadata.put("chunk_index", i);
                documents.add(new Document(paragraph, metadata));
            }
        }
        // 如果没有按段落拆分成功，直接用全文
        if (documents.isEmpty() && !text.trim().isEmpty()) {
            documents.add(new Document(text.trim(), baseMetadata));
        }
        return documents;
    }
}
