package com.zhida.aiagent.rag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryRewriterTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean("dashscopeChatModel", ChatModel.class, () -> mock(ChatModel.class))
            .withBean("ollamaChatModel", ChatModel.class, () -> mock(ChatModel.class))
            .withBean(QueryRewriter.class);

    @Mock
    private QueryTransformer queryTransformer;

    @Test
    void shouldInstantiateWithQualifiedDashscopeChatModelWhenMultipleChatModelsExist() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(QueryRewriter.class));
    }

    @Test
    void shouldReturnRewrittenQueryWhenTransformerSucceeds() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        QueryRewriter queryRewriter = new QueryRewriter(queryTransformer, executorService, 3000L);

        try {
            when(queryTransformer.transform(any(Query.class)))
                    .thenReturn(new Query("优化后的检索问题"));

            String rewritten = queryRewriter.doQueryRewrite("原始问题");

            assertEquals("优化后的检索问题", rewritten);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void shouldFallbackToOriginalPromptWhenTransformerThrows() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        QueryRewriter queryRewriter = new QueryRewriter(queryTransformer, executorService, 3000L);

        try {
            when(queryTransformer.transform(any(Query.class)))
                    .thenThrow(new RuntimeException("rewrite failed"));

            String rewritten = queryRewriter.doQueryRewrite("原始问题");

            assertEquals("原始问题", rewritten);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void shouldFallbackToOriginalPromptWhenTransformerTimesOut() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        QueryRewriter queryRewriter = new QueryRewriter(queryTransformer, executorService, 50L);

        try {
            when(queryTransformer.transform(any(Query.class)))
                    .thenAnswer(invocation -> {
                        Thread.sleep(200L);
                        return new Query("优化后的检索问题");
                    });

            String rewritten = queryRewriter.doQueryRewrite("原始问题");

            assertEquals("原始问题", rewritten);
        } finally {
            executorService.shutdownNow();
        }
    }
}
