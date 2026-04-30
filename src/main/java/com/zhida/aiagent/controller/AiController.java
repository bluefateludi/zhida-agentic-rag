package com.zhida.aiagent.controller;

import com.zhida.aiagent.agent.ZhidaAgent;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI Agent 接口（保留超级智能体功能）
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 流式调用通用工具智能体
     */
    @GetMapping("/agent/chat")
    public SseEmitter doChatWithAgent(String message) {
        ZhidaAgent zhidaAgent = new ZhidaAgent(allTools, dashscopeChatModel);
        return zhidaAgent.runStream(message);
    }
}
