package com.zhida.aiagent.service;

import com.zhida.aiagent.model.dto.ResearchBrief;
import com.zhida.aiagent.model.dto.SourceReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 写作 Agent，负责将 ResearchBrief 转成研究报告。
 */
@Service
public class WriterAgentService {

    private static final String WRITER_SYSTEM_PROMPT = """
            你是「智答 AI」的研究报告写作助手。

            你的任务是基于研究简报输出结构化 Markdown 报告。

            写作要求：
            1. 只能使用研究简报中的证据，不要编造未提供的事实
            2. 先给结论，再给证据与分析
            3. 如果证据不足，必须明确标记“信息不足”
            4. 需要区分知识库证据与公开网络证据
            5. 输出简洁、专业、适合求职作品展示
            """;

    private final ChatClient chatClient;

    public WriterAgentService(ChatModel dashscopeChatModel) {
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(WRITER_SYSTEM_PROMPT)
                .build();
    }

    public String writeReport(ResearchBrief brief) {
        return chatClient.prompt()
                .user(buildReportPrompt(brief))
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }

    String buildReportPrompt(ResearchBrief brief) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于下面的研究简报生成 Markdown 研究报告。\n\n");
        prompt.append("原始问题：").append(nullToEmpty(brief.originalQuestion())).append("\n");
        prompt.append("改写后的研究问题：").append(nullToEmpty(brief.rewrittenQuestion())).append("\n");
        prompt.append("生成时间：").append(brief.generatedAt()).append("\n\n");

        prompt.append("建议覆盖的子问题：\n");
        appendLines(prompt, brief.subQuestions());
        prompt.append("\n知识库证据：\n");
        appendEvidence(prompt, brief.kbEvidence());
        prompt.append("\n公开网络证据：\n");
        appendEvidence(prompt, brief.webEvidence());
        prompt.append("\n信息缺口：\n");
        appendLines(prompt, brief.informationGaps());

        prompt.append("""

                报告结构要求：
                1. 标题
                2. 结论摘要
                3. 核心证据
                4. 风险与信息不足
                5. 下一步建议

                如果证据不足，必须明确标记“信息不足”，并指出缺少哪些关键依据。
                """);
        return prompt.toString();
    }

    private void appendEvidence(StringBuilder prompt, List<SourceReference> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            prompt.append("- 无\n");
            return;
        }
        for (SourceReference source : evidence) {
            prompt.append("- [")
                    .append(nullToEmpty(source.getDocumentTitle()))
                    .append("] ");
            if (source.getFileName() != null && !source.getFileName().isBlank()) {
                prompt.append("(").append(source.getFileName()).append(") ");
            }
            if (source.getCategory() != null && !source.getCategory().isBlank()) {
                prompt.append("分类=").append(source.getCategory()).append(" ");
            }
            if (source.getRank() != null) {
                prompt.append("排名=").append(source.getRank()).append(" ");
            }
            if (source.getChunkIndex() != null) {
                prompt.append("Chunk=").append(source.getChunkIndex()).append(" ");
            }
            prompt.append(": ").append(nullToEmpty(source.getRelevantContent())).append("\n");
        }
    }

    private void appendLines(StringBuilder prompt, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            prompt.append("- 无\n");
            return;
        }
        for (String line : lines) {
            prompt.append("- ").append(nullToEmpty(line)).append("\n");
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
