package com.zhida.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "智答AI项目说明.pdf";
        String content = "智答 AI 是一个支持知识库问答和来源追溯的项目。";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
