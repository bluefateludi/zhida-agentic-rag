package com.zhida.aiagent.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhida.aiagent.model.dto.SourceReference;
import com.zhida.aiagent.tools.WebSearchTool;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 联网研究服务
 */
@Slf4j
@Service
public class WebResearchService {

    private final WebSearchTool webSearchTool;
    private final ExecutorService executorService;
    private long researchTimeoutMs;

    @Autowired
    public WebResearchService(@Value("${search-api.api-key}") String searchApiKey,
                              @Value("${pm.web-research-timeout-ms:5000}") long researchTimeoutMs) {
        this.webSearchTool = new WebSearchTool(searchApiKey);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.researchTimeoutMs = researchTimeoutMs;
    }

    WebResearchService(WebSearchTool webSearchTool,
                       ExecutorService executorService,
                       long researchTimeoutMs) {
        this.webSearchTool = webSearchTool;
        this.executorService = executorService;
        this.researchTimeoutMs = researchTimeoutMs;
    }

    public WebResearchResult research(String query) {
        String rawResult = searchWithTimeout(query);
        if (rawResult == null || rawResult.isBlank() || rawResult.startsWith("Error searching")) {
            return new WebResearchResult("", List.of());
        }
        JSONArray array = JSONUtil.parseArray("[" + rawResult + "]");
        List<SourceReference> sources = new ArrayList<>();
        List<String> summaryLines = new ArrayList<>();
        for (int i = 0; i < Math.min(array.size(), 3); i++) {
            JSONObject item = array.getJSONObject(i);
            String title = item.getStr("title", "联网结果");
            String link = item.getStr("link", item.getStr("displayed_link", "web"));
            String snippet = item.getStr("snippet", item.getStr("snippet_highlighted_words", ""));
            sources.add(new SourceReference(title, link, snippet, null));
            summaryLines.add("- " + title + "： " + snippet);
        }
        return new WebResearchResult(String.join("\n", summaryLines), sources);
    }

    private String searchWithTimeout(String query) {
        Future<String> future = executorService.submit(() -> webSearchTool.searchWeb(query));
        try {
            return future.get(researchTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("联网研究超时，降级为空结果: timeout={}ms, query={}", researchTimeoutMs, query);
            return "";
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            log.warn("联网研究被中断，降级为空结果: query={}", query, e);
            return "";
        } catch (ExecutionException e) {
            future.cancel(true);
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.warn("联网研究异常，降级为空结果: query={}, error={}", query, cause.toString());
            return "";
        } catch (Exception e) {
            future.cancel(true);
            log.warn("联网研究失败，降级为空结果: query={}, error={}", query, e.toString());
            return "";
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    public record WebResearchResult(String summary, List<SourceReference> sources) {}
}
