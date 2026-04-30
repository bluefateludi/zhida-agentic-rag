# PM Fallback Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make PM mode answer normal product analysis questions and degrade gracefully when knowledge-base or web research support is unavailable.

**Architecture:** Keep `/chat` strict and add PM-specific behavior in `PmChatService`, `KnowledgeBaseRagConfig`, and `WebResearchService`. PM mode can use empty RAG context, web search has a timeout fallback, and stream failures produce a final readable assistant reply.

**Tech Stack:** Spring Boot, Spring AI RAG, Reactor `Flux`, JUnit 5, AssertJ, Mockito.

---

### Task 1: PM Scope and RAG Behavior

**Files:**
- Modify: `src/main/java/com/zhida/aiagent/service/PmChatService.java`
- Modify: `src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java`
- Test: `src/test/java/com/zhida/aiagent/service/PmChatServicePromptTest.java`
- Test: `src/test/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfigTest.java`

- [ ] Write failing tests that PM prompt treats general product questions as in scope and PM service uses the PM RAG advisor.
- [ ] Write failing test that PM RAG config calls `allowEmptyContext(true)`.
- [ ] Implement PM prompt additions and `createPmRagAdvisor`.
- [ ] Run targeted tests.

### Task 2: Web Research Timeout

**Files:**
- Modify: `src/main/java/com/zhida/aiagent/service/WebResearchService.java`
- Modify: `src/main/java/com/zhida/aiagent/tools/WebSearchTool.java`
- Test: `src/test/java/com/zhida/aiagent/service/WebResearchServiceTest.java`

- [ ] Write failing timeout fallback test.
- [ ] Add injectable constructor and timeout-controlled research execution.
- [ ] Run targeted test.

### Task 3: PM Stream Fallback

**Files:**
- Modify: `src/main/java/com/zhida/aiagent/service/PmChatService.java`
- Test: `src/test/java/com/zhida/aiagent/controller/PmControllerTest.java`

- [ ] Write failing controller test for readable fallback stream events.
- [ ] Add `onErrorResume` fallback in PM streaming pipeline.
- [ ] Run PM backend tests.
