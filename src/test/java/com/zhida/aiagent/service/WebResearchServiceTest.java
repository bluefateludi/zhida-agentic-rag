package com.zhida.aiagent.service;

import com.zhida.aiagent.tools.WebSearchTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class WebResearchServiceTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "search-api.api-key=test-key",
                    "pm.web-research-timeout-ms=50"
            )
            .withBean(WebResearchService.class);

    @Test
    void shouldInstantiateAsSpringBeanWithValueInjectedConstructor() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(WebResearchService.class));
    }

    @Test
    void shouldFallbackToEmptyResearchResultWhenSearchTimesOut() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        WebResearchService webResearchService = new WebResearchService(
                new SlowWebSearchTool(),
                executorService,
                50L
        );

        try {
            long startedAt = System.currentTimeMillis();
            WebResearchService.WebResearchResult result = webResearchService.research("什么是一个好产品");
            long elapsedMs = System.currentTimeMillis() - startedAt;

            assertThat(elapsedMs).isLessThan(500L);
            assertThat(result.summary()).isBlank();
            assertThat(result.sources()).isEmpty();
        } finally {
            executorService.shutdownNow();
        }
    }

    private static class SlowWebSearchTool extends WebSearchTool {
        SlowWebSearchTool() {
            super("test-key");
        }

        @Override
        public String searchWeb(String query) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "{\"title\":\"结果\",\"link\":\"https://example.com\",\"snippet\":\"摘要\"}";
        }
    }
}
