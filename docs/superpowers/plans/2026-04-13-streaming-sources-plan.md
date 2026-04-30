# Streaming Sources Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the streaming chat path return final source metadata to the frontend and persist the assistant reply plus sources into chat history.

**Architecture:** Keep SSE text chunks for incremental rendering, then emit one structured completion event carrying the final content and sources. Update the frontend stream client to listen for the completion event and store the returned sources in the rendered/history message shape.

**Tech Stack:** Spring Boot, Spring MVC `SseEmitter`, Reactor `Flux`, Vue 3, Axios/EventSource, JUnit 5

---

### Task 1: Lock the streaming protocol with a failing backend test

**Files:**
- Create: `src/test/java/com/zhida/aiagent/controller/ChatControllerTest.java`
- Modify: `src/main/java/com/zhida/aiagent/controller/ChatController.java`

- [ ] **Step 1: Write the failing test**
- [ ] **Step 2: Run the focused Maven test and confirm it fails for the expected reason**
- [ ] **Step 3: Implement the minimal controller/service contract needed for the stream completion event**
- [ ] **Step 4: Re-run the focused Maven test and confirm it passes**

### Task 2: Persist streaming assistant replies with sources

**Files:**
- Modify: `src/main/java/com/zhida/aiagent/service/ChatService.java`

- [ ] **Step 1: Add a focused regression test for streaming completion/persistence or extend the controller test coverage**
- [ ] **Step 2: Run the focused Maven test and confirm it fails for the expected reason**
- [ ] **Step 3: Implement stream accumulation, source extraction, logging, and persistence on completion**
- [ ] **Step 4: Re-run the focused Maven test and confirm it passes**

### Task 3: Wire the frontend to consume completion metadata

**Files:**
- Modify: `zhida-ai-frontend/src/api/index.js`
- Modify: `zhida-ai-frontend/src/views/KnowledgeBase.vue`

- [ ] **Step 1: Update the EventSource client to listen for the completion event**
- [ ] **Step 2: Update the page state logic so the final assistant message uses returned sources instead of an empty array**
- [ ] **Step 3: Run the frontend build to verify the integration compiles**
