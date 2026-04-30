package com.zhida.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 查询重写器
 * 将用户的自然语言问题优化为更适合向量检索的查询
 */
@Slf4j
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;
    private final ExecutorService executorService;
    private long rewriteTimeoutMs;

    @Autowired
    public QueryRewriter(@Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel) {
        this(buildQueryTransformer(dashscopeChatModel), Executors.newVirtualThreadPerTaskExecutor(), 3000L);
    }

    QueryRewriter(QueryTransformer queryTransformer, ExecutorService executorService, long rewriteTimeoutMs) {
        this.queryTransformer = queryTransformer;
        this.executorService = executorService;
        this.rewriteTimeoutMs = rewriteTimeoutMs;
    }

    @Value("${chat.query-rewrite-timeout-ms:3000}")
    public void setRewriteTimeoutMs(long rewriteTimeoutMs) {
        this.rewriteTimeoutMs = rewriteTimeoutMs;
    }

    /**
     * 执行查询重写
     */
    public String doQueryRewrite(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return prompt;
        }

        log.info("查询重写开始: timeout={}ms, prompt={}", rewriteTimeoutMs, prompt);
        Future<Query> future = executorService.submit(() -> queryTransformer.transform(new Query(prompt)));
        try {
            Query transformedQuery = future.get(rewriteTimeoutMs, TimeUnit.MILLISECONDS);
            String rewritten = transformedQuery != null && transformedQuery.text() != null && !transformedQuery.text().isBlank()
                    ? transformedQuery.text()
                    : prompt;
            log.info("查询重写: [{}] → [{}]", prompt, rewritten);
            return rewritten;
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("查询重写超时，回退原始问题: timeout={}ms, prompt={}", rewriteTimeoutMs, prompt);
            return prompt;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            log.warn("查询重写被中断，回退原始问题: prompt={}", prompt, e);
            return prompt;
        } catch (ExecutionException e) {
            future.cancel(true);
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.warn("查询重写异常，回退原始问题: prompt={}, error={}", prompt, cause.toString());
            return prompt;
        } catch (Exception e) {
            future.cancel(true);
            log.warn("查询重写失败，回退原始问题: prompt={}, error={}", prompt, e.toString());
            return prompt;
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    private static QueryTransformer buildQueryTransformer(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .promptTemplate(new PromptTemplate("""
                        重写以下用户问题，使其更适合在知识库（{target}）中进行语义检索。
                        要求：
                        1. 保留原始问题的核心意图和关键实体
                        2. 补充可能相关的同义词或专业术语
                        3. 如果是口语化表达，转换为更正式的书面表达
                        4. 如果问题已经很清晰，可以保持原样
                        5. 只输出重写后的问题，不要有任何解释
                        原始问题: {query}
                        """))
                .build();
    }
}
