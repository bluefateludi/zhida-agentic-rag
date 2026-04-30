package com.zhida.aiagent.agent;

import com.zhida.aiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 通用工具智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class ZhidaAgent extends ToolCallAgent {

    public ZhidaAgent(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("zhidaAgent");
        String SYSTEM_PROMPT = """
                You are ZhiDa Agent, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.

                IMPORTANT RULES:
                1. When you get tool results, you MUST summarize and respond to the user in a clear and helpful way BEFORE terminating.
                2. Do NOT call the terminate tool immediately after getting tool results. First, analyze the results and provide a meaningful answer to the user.
                3. Only call the terminate tool AFTER you have provided your final answer to the user.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
