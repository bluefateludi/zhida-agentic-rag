# Zhang Yiming Mode Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an independent `/pm` mode with its own route, sessions, and backend chat pipeline that answers in a Zhang Yiming-style product voice using both web search and knowledge-base retrieval.

**Architecture:** Keep `/pm` isolated from `/chat` by introducing a session mode field and dedicated `/pm` controller/service pair. Reuse the existing RAG and persistence stack, add a lightweight web-research service for search result summarization, then build a minimal standalone frontend page that talks only to the new `/pm` endpoints.

**Tech Stack:** Spring Boot, Spring AI ChatClient + RAG advisor, JPA, Vue 3, Vue Router 4, Vite, JUnit/WebMvcTest, Node assertion tests

---

## Chunk 1: Backend Session Isolation

### Task 1: Add mode-aware session storage

**Files:**
- Create: `src/main/java/com/zhida/aiagent/model/enums/ChatMode.java`
- Modify: `src/main/java/com/zhida/aiagent/model/entity/ChatSession.java`
- Modify: `src/main/java/com/zhida/aiagent/repository/ChatSessionRepository.java`
- Modify: `src/main/java/com/zhida/aiagent/service/ChatSessionService.java`
- Test: `src/test/java/com/zhida/aiagent/controller/PmControllerTest.java`

- [ ] **Step 1: Write a failing controller test for `/pm/sessions` that expects PM-only session listing**
- [ ] **Step 2: Run the targeted test and verify it fails because `/pm` session APIs and mode filtering do not exist**
- [ ] **Step 3: Add `ChatMode`, persist it on `ChatSession`, and extend `ChatSessionService` with mode-aware create/list/get helpers**
- [ ] **Step 4: Re-run the targeted test and verify the new mode-aware behavior passes**

## Chunk 2: PM Chat Pipeline

### Task 2: Build the Zhang Yiming chat service

**Files:**
- Create: `src/main/java/com/zhida/aiagent/service/WebResearchService.java`
- Create: `src/main/java/com/zhida/aiagent/service/PmChatService.java`
- Create: `src/main/java/com/zhida/aiagent/controller/PmController.java`
- Test: `src/test/java/com/zhida/aiagent/service/WebResearchServiceTest.java`
- Test: `src/test/java/com/zhida/aiagent/controller/PmControllerTest.java`

- [ ] **Step 1: Write a failing unit test for web result parsing/building into PM sources**
- [ ] **Step 2: Write a failing WebMvc test for `/pm/stream` complete events carrying sources**
- [ ] **Step 3: Implement `WebResearchService` to call `WebSearchTool` and map results into compact web evidence**
- [ ] **Step 4: Implement `PmChatService` with PM system prompt, RAG advisor reuse, web evidence fusion, persistence, and stream support**
- [ ] **Step 5: Implement `PmController` with independent `/pm` session/history/chat endpoints**
- [ ] **Step 6: Re-run the PM backend tests and verify they pass**

## Chunk 3: Frontend PM Mode

### Task 3: Add the independent `/pm` page

**Files:**
- Modify: `zhida-ai-frontend/src/router/routes.js`
- Modify: `zhida-ai-frontend/tests/routes.test.mjs`
- Modify: `zhida-ai-frontend/src/api/index.js`
- Modify: `zhida-ai-frontend/src/components/Sidebar.vue`
- Modify: `zhida-ai-frontend/src/views/LandingPage.vue`
- Create: `zhida-ai-frontend/src/views/PmMode.vue`
- Create: `zhida-ai-frontend/tests/pm-route.test.mjs`

- [ ] **Step 1: Write a failing frontend route test for `/pm`**
- [ ] **Step 2: Run the route tests and verify `/pm` is missing**
- [ ] **Step 3: Add PM API methods, the `/pm` route, and entry links from the landing page/sidebar**
- [ ] **Step 4: Implement the minimal standalone PM page with session list, message area, and large input**
- [ ] **Step 5: Re-run the frontend route tests and verify they pass**

## Chunk 4: Verification

### Task 4: Verify the full feature

**Files:**
- No additional files expected

- [ ] **Step 1: Run PM-focused backend tests**
- [ ] **Step 2: Run existing frontend route/content tests plus the new PM route test**
- [ ] **Step 3: Run `npm run build` in `zhida-ai-frontend`**
- [ ] **Step 4: Review the changed routes, endpoints, and prompts against the approved design before reporting completion**
