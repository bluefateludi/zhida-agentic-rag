# Agentic RAG Observability Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade Zhida AI into a portfolio-ready Agentic RAG system with measurable retrieval quality, a research-and-writing report mode, and production-oriented observability.

**Architecture:** Keep the existing Spring Boot + Vue structure and add focused service modules around the current RAG flow. The normal chat path gains trace collection and richer citations; the report path composes a controlled `ResearchAgent` step and `WriterAgent` step; observability stores execution metadata for review, evaluation, and demos.

**Tech Stack:** Java 21, Spring Boot 3, Spring AI, LangChain4j, PostgreSQL/pgvector, JPA, Vue 3, Vite, Axios, SSE, JUnit 5, Mockito.

---

## File Structure

**Backend files to create:**
- `src/main/java/com/zhida/aiagent/model/dto/RagTrace.java` stores one RAG execution trace for API responses and persistence.
- `src/main/java/com/zhida/aiagent/model/dto/RetrievalTraceItem.java` stores one retrieved chunk with metadata, rank, and optional score.
- `src/main/java/com/zhida/aiagent/model/dto/ResearchBrief.java` stores the output of the research step.
- `src/main/java/com/zhida/aiagent/model/dto/ResearchReportResponse.java` stores final report content, research brief, sources, and trace.
- `src/main/java/com/zhida/aiagent/model/dto/EvalCase.java` stores one golden-set question.
- `src/main/java/com/zhida/aiagent/model/dto/EvalRunResult.java` stores one evaluation run result.
- `src/main/java/com/zhida/aiagent/model/entity/RagTraceLog.java` persists observability data.
- `src/main/java/com/zhida/aiagent/repository/RagTraceLogRepository.java` queries trace logs.
- `src/main/java/com/zhida/aiagent/rag/RagTraceCollector.java` captures retrieved documents and timing for a request.
- `src/main/java/com/zhida/aiagent/service/RagTraceService.java` builds and persists trace logs.
- `src/main/java/com/zhida/aiagent/service/RagEvaluationService.java` runs golden-set evaluation cases.
- `src/main/java/com/zhida/aiagent/service/ResearchAgentService.java` creates structured research briefs from KB and web evidence.
- `src/main/java/com/zhida/aiagent/service/WriterAgentService.java` turns a research brief into a report.
- `src/main/java/com/zhida/aiagent/controller/EvaluationController.java` exposes evaluation endpoints.
- `src/main/java/com/zhida/aiagent/controller/ReportController.java` exposes report mode endpoints.
- `src/main/java/com/zhida/aiagent/controller/TraceController.java` exposes recent trace logs for demo and debugging.
- `src/main/resources/eval/golden-set.json` contains demo evaluation cases.

**Backend files to modify:**
- `src/main/java/com/zhida/aiagent/model/dto/SourceReference.java` adds `category`, `chunkIndex`, `rank`, and `traceId`.
- `src/main/java/com/zhida/aiagent/service/ChatService.java` records rewritten query, retrieval trace, latency, and trace ID.
- `src/main/java/com/zhida/aiagent/service/PmChatService.java` records the same trace data for PM mode.
- `src/main/java/com/zhida/aiagent/service/KnowledgeBaseService.java` adds chunk index metadata during ingestion.
- `src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java` accepts a trace collector and preserves the current source callback behavior.
- `src/main/java/com/zhida/aiagent/service/ChatStreamEvent.java` carries optional trace ID in the complete event.
- `src/main/resources/application.yml` adds observability and evaluation configuration flags.

**Frontend files to modify:**
- `zhida-ai-frontend/src/api/index.js` adds report, evaluation, and trace APIs.
- `zhida-ai-frontend/src/components/SourceCard.vue` displays document, category, rank, chunk index, and score when present.
- `zhida-ai-frontend/src/components/MessageList.vue` shows trace metadata on completed answers.
- `zhida-ai-frontend/src/views/PmMode.vue` adds report mode entry points if this remains the PM workspace.
- `zhida-ai-frontend/src/router/routes.js` adds routes for evaluation and trace views if separate pages are chosen.

**Frontend files to create if time allows:**
- `zhida-ai-frontend/src/views/ResearchReport.vue` provides a report generation workspace.
- `zhida-ai-frontend/src/views/EvaluationDashboard.vue` shows golden-set run results.
- `zhida-ai-frontend/src/views/TraceDashboard.vue` shows recent RAG traces.

---

## Chunk 1: RAG Evaluation And Citation Trace

### Task 1: Add Chunk Metadata During Document Ingestion

**Files:**
- Modify: `src/main/java/com/zhida/aiagent/service/KnowledgeBaseService.java`
- Test: `src/test/java/com/zhida/aiagent/service/KnowledgeBaseServiceTest.java`

- [ ] **Step 1: Write a failing test for chunk index metadata**

Create or extend a service-level test that verifies processed chunks contain:

```java
assertThat(document.getMetadata()).containsEntry("documentId", kbDocument.getId());
assertThat(document.getMetadata()).containsEntry("documentTitle", kbDocument.getTitle());
assertThat(document.getMetadata()).containsEntry("fileName", kbDocument.getFileName());
assertThat(document.getMetadata()).containsEntry("category", kbDocument.getCategory());
assertThat(document.getMetadata()).containsKey("chunkIndex");
```

- [ ] **Step 2: Run the targeted test**

Run: `mvn -Dtest=KnowledgeBaseServiceTest test`

Expected: FAIL because `chunkIndex` is not yet added.

- [ ] **Step 3: Add chunk index metadata**

After `myTokenTextSplitter.splitCustomized(enrichedDocuments)`, rebuild the chunk list and add `chunkIndex` starting from 1. Keep existing metadata unchanged.

- [ ] **Step 4: Run the targeted test again**

Run: `mvn -Dtest=KnowledgeBaseServiceTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/service/KnowledgeBaseService.java src/test/java/com/zhida/aiagent/service/KnowledgeBaseServiceTest.java
git commit -m "feat: add chunk metadata for citations"
```

### Task 2: Extend SourceReference For Better Citations

**Files:**
- Modify: `src/main/java/com/zhida/aiagent/model/dto/SourceReference.java`
- Modify: `src/main/java/com/zhida/aiagent/service/ChatService.java`
- Modify: `src/main/java/com/zhida/aiagent/service/PmChatService.java`
- Test: `src/test/java/com/zhida/aiagent/model/dto/SourceReferenceTest.java`

- [ ] **Step 1: Write failing tests for new source fields**

Assert that source references can hold:

```java
assertThat(source.getCategory()).isEqualTo("product");
assertThat(source.getChunkIndex()).isEqualTo(3);
assertThat(source.getRank()).isEqualTo(1);
assertThat(source.getTraceId()).isNotBlank();
```

- [ ] **Step 2: Run source reference tests**

Run: `mvn -Dtest=SourceReferenceTest test`

Expected: FAIL because the new fields do not exist.

- [ ] **Step 3: Add fields and constructors**

Add `category`, `chunkIndex`, `rank`, and `traceId` to `SourceReference`. Preserve existing constructors so current callers keep compiling.

- [ ] **Step 4: Populate the fields from document metadata**

In `ChatService.buildSourceReferences` and `PmChatService.mergeSources`, map `category`, `chunkIndex`, and source rank from retrieved `Document` metadata.

- [ ] **Step 5: Run related tests**

Run: `mvn -Dtest=SourceReferenceTest,ChatStreamAccumulatorTest,PmChatServicePromptTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/model/dto/SourceReference.java src/main/java/com/zhida/aiagent/service/ChatService.java src/main/java/com/zhida/aiagent/service/PmChatService.java src/test/java/com/zhida/aiagent/model/dto/SourceReferenceTest.java
git commit -m "feat: enrich source citation metadata"
```

### Task 3: Add RAG Trace DTOs And Collector

**Files:**
- Create: `src/main/java/com/zhida/aiagent/model/dto/RagTrace.java`
- Create: `src/main/java/com/zhida/aiagent/model/dto/RetrievalTraceItem.java`
- Create: `src/main/java/com/zhida/aiagent/rag/RagTraceCollector.java`
- Modify: `src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java`
- Test: `src/test/java/com/zhida/aiagent/rag/RagTraceCollectorTest.java`

- [ ] **Step 1: Write a failing collector test**

Test that a collector captures retrieved documents with rank, title, file name, category, chunk index, and text preview.

- [ ] **Step 2: Run the collector test**

Run: `mvn -Dtest=RagTraceCollectorTest test`

Expected: FAIL because collector classes do not exist.

- [ ] **Step 3: Implement DTOs and collector**

`RagTraceCollector` should expose:

```java
void capture(List<Document> documents);
List<Document> getDocuments();
List<RetrievalTraceItem> toTraceItems();
```

- [ ] **Step 4: Wire collector into RAG config without breaking existing callbacks**

Keep `createRagAdvisor(VectorStore, String, Consumer<List<Document>>)` and add overloads only if needed. Existing chat and PM services should continue compiling.

- [ ] **Step 5: Run RAG tests**

Run: `mvn -Dtest=RagTraceCollectorTest,KnowledgeBaseRagConfigTest,SourceTracingDocumentRetrieverTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/model/dto/RagTrace.java src/main/java/com/zhida/aiagent/model/dto/RetrievalTraceItem.java src/main/java/com/zhida/aiagent/rag/RagTraceCollector.java src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java src/test/java/com/zhida/aiagent/rag/RagTraceCollectorTest.java
git commit -m "feat: capture rag retrieval traces"
```

### Task 4: Add Golden Set Evaluation Endpoint

**Files:**
- Create: `src/main/resources/eval/golden-set.json`
- Create: `src/main/java/com/zhida/aiagent/model/dto/EvalCase.java`
- Create: `src/main/java/com/zhida/aiagent/model/dto/EvalRunResult.java`
- Create: `src/main/java/com/zhida/aiagent/service/RagEvaluationService.java`
- Create: `src/main/java/com/zhida/aiagent/controller/EvaluationController.java`
- Test: `src/test/java/com/zhida/aiagent/service/RagEvaluationServiceTest.java`
- Test: `src/test/java/com/zhida/aiagent/controller/EvaluationControllerTest.java`

- [ ] **Step 1: Add a tiny golden set**

Start with 8 to 12 cases, not 50. Include normal KB answer, no-answer refusal, cross-document summary, and PM-style question.

- [ ] **Step 2: Write failing service tests**

Test that the service loads cases and returns one result per case with `question`, `expectedKeywords`, `actualAnswer`, `sources`, and `passed`.

- [ ] **Step 3: Implement a deterministic evaluation method**

Use existing `ChatService.chat` for each case. For deadline safety, score with keyword containment and source presence, not another LLM judge.

- [ ] **Step 4: Add controller endpoints**

Add:

```text
GET /api/eval/cases
POST /api/eval/run
```

- [ ] **Step 5: Run targeted tests**

Run: `mvn -Dtest=RagEvaluationServiceTest,EvaluationControllerTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add src/main/resources/eval/golden-set.json src/main/java/com/zhida/aiagent/model/dto/EvalCase.java src/main/java/com/zhida/aiagent/model/dto/EvalRunResult.java src/main/java/com/zhida/aiagent/service/RagEvaluationService.java src/main/java/com/zhida/aiagent/controller/EvaluationController.java src/test/java/com/zhida/aiagent/service/RagEvaluationServiceTest.java src/test/java/com/zhida/aiagent/controller/EvaluationControllerTest.java
git commit -m "feat: add rag golden set evaluation"
```

---

## Chunk 2: Research Agent And Writer Agent Report Mode

### Task 5: Define Research Brief And Report Response Contracts

**Files:**
- Create: `src/main/java/com/zhida/aiagent/model/dto/ResearchBrief.java`
- Create: `src/main/java/com/zhida/aiagent/model/dto/ResearchReportResponse.java`
- Test: `src/test/java/com/zhida/aiagent/model/dto/ResearchBriefTest.java`

- [ ] **Step 1: Write DTO tests**

Assert the brief supports:

```text
originalQuestion
rewrittenQuestion
subQuestions
kbEvidence
webEvidence
informationGaps
generatedAt
```

- [ ] **Step 2: Run DTO tests**

Run: `mvn -Dtest=ResearchBriefTest test`

Expected: FAIL because DTOs do not exist.

- [ ] **Step 3: Implement DTOs as records or Lombok data classes**

Use simple serializable shapes that are easy for Vue to render.

- [ ] **Step 4: Run DTO tests again**

Run: `mvn -Dtest=ResearchBriefTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/model/dto/ResearchBrief.java src/main/java/com/zhida/aiagent/model/dto/ResearchReportResponse.java src/test/java/com/zhida/aiagent/model/dto/ResearchBriefTest.java
git commit -m "feat: define research report contracts"
```

### Task 6: Implement ResearchAgentService

**Files:**
- Create: `src/main/java/com/zhida/aiagent/service/ResearchAgentService.java`
- Modify: `src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java`
- Test: `src/test/java/com/zhida/aiagent/service/ResearchAgentServiceTest.java`

- [ ] **Step 1: Write failing tests with mocked dependencies**

Mock `QueryRewriter`, `VectorStore`, and `WebResearchService`. Verify the service returns a `ResearchBrief` with rewritten query, KB evidence, web evidence, and information gaps.

- [ ] **Step 2: Run service test**

Run: `mvn -Dtest=ResearchAgentServiceTest test`

Expected: FAIL.

- [ ] **Step 3: Implement the research step**

Implementation shape:

```text
rewrite question
retrieve top documents from KB using existing RAG path or vector retriever
run web research when enabled
build ResearchBrief with concise evidence and gaps
```

Keep this deterministic. Avoid free-form multi-agent conversation for the first version.

- [ ] **Step 4: Run service test**

Run: `mvn -Dtest=ResearchAgentServiceTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/service/ResearchAgentService.java src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java src/test/java/com/zhida/aiagent/service/ResearchAgentServiceTest.java
git commit -m "feat: add research agent service"
```

### Task 7: Implement WriterAgentService

**Files:**
- Create: `src/main/java/com/zhida/aiagent/service/WriterAgentService.java`
- Test: `src/test/java/com/zhida/aiagent/service/WriterAgentServiceTest.java`

- [ ] **Step 1: Write failing prompt-construction tests**

Verify the writer prompt includes the original question, subquestions, KB evidence, web evidence, and a rule that conclusions must be evidence-backed.

- [ ] **Step 2: Run writer tests**

Run: `mvn -Dtest=WriterAgentServiceTest test`

Expected: FAIL.

- [ ] **Step 3: Implement writer service**

Use `ChatClient` with a system prompt like:

```text
你是研究报告写作 Agent。只能基于 ResearchBrief 中的证据写作；证据不足时必须标记为信息不足；输出 Markdown，包含结论、依据、风险与下一步建议。
```

- [ ] **Step 4: Run writer tests**

Run: `mvn -Dtest=WriterAgentServiceTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/service/WriterAgentService.java src/test/java/com/zhida/aiagent/service/WriterAgentServiceTest.java
git commit -m "feat: add writer agent service"
```

### Task 8: Add Report Controller And API

**Files:**
- Create: `src/main/java/com/zhida/aiagent/controller/ReportController.java`
- Modify: `zhida-ai-frontend/src/api/index.js`
- Test: `src/test/java/com/zhida/aiagent/controller/ReportControllerTest.java`

- [ ] **Step 1: Write controller test**

Test:

```text
POST /api/report/generate
body: { "message": "...", "category": "..." }
response: { "content": "...", "brief": {...}, "sources": [...], "trace": {...} }
```

- [ ] **Step 2: Run controller test**

Run: `mvn -Dtest=ReportControllerTest test`

Expected: FAIL.

- [ ] **Step 3: Implement controller**

Controller composes:

```text
ResearchAgentService.research(...)
WriterAgentService.write(...)
RagTraceService.persist(...)
```

- [ ] **Step 4: Add frontend API wrapper**

In `zhida-ai-frontend/src/api/index.js` add:

```js
export const generateResearchReport = (message, category) => {
  return request.post('/report/generate', { message, category })
}
```

- [ ] **Step 5: Run tests**

Run: `mvn -Dtest=ReportControllerTest,ResearchAgentServiceTest,WriterAgentServiceTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/controller/ReportController.java zhida-ai-frontend/src/api/index.js src/test/java/com/zhida/aiagent/controller/ReportControllerTest.java
git commit -m "feat: add research report endpoint"
```

---

## Chunk 3: Production Observability

### Task 9: Persist RAG Trace Logs

**Files:**
- Create: `src/main/java/com/zhida/aiagent/model/entity/RagTraceLog.java`
- Create: `src/main/java/com/zhida/aiagent/repository/RagTraceLogRepository.java`
- Create: `src/main/java/com/zhida/aiagent/service/RagTraceService.java`
- Modify: `src/main/java/com/zhida/aiagent/service/ChatService.java`
- Modify: `src/main/java/com/zhida/aiagent/service/PmChatService.java`
- Test: `src/test/java/com/zhida/aiagent/service/RagTraceServiceTest.java`

- [ ] **Step 1: Write failing persistence tests**

Verify a trace log stores:

```text
traceId
mode
sessionId
originalQuery
rewrittenQuery
category
retrievalCount
latencyMs
success
errorMessage
sourcesJson
createdAt
```

- [ ] **Step 2: Run trace service tests**

Run: `mvn -Dtest=RagTraceServiceTest test`

Expected: FAIL.

- [ ] **Step 3: Implement entity, repository, and service**

Use JSON strings for `sourcesJson` and `traceJson` to avoid schema churn before the deadline.

- [ ] **Step 4: Wire trace persistence into chat and PM paths**

Persist success traces on completion and failure traces in error handlers. Do not block the user response if trace persistence fails.

- [ ] **Step 5: Run related tests**

Run: `mvn -Dtest=RagTraceServiceTest,ChatControllerTest,PmControllerTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/model/entity/RagTraceLog.java src/main/java/com/zhida/aiagent/repository/RagTraceLogRepository.java src/main/java/com/zhida/aiagent/service/RagTraceService.java src/main/java/com/zhida/aiagent/service/ChatService.java src/main/java/com/zhida/aiagent/service/PmChatService.java src/test/java/com/zhida/aiagent/service/RagTraceServiceTest.java
git commit -m "feat: persist rag execution traces"
```

### Task 10: Add Trace Query API

**Files:**
- Create: `src/main/java/com/zhida/aiagent/controller/TraceController.java`
- Test: `src/test/java/com/zhida/aiagent/controller/TraceControllerTest.java`

- [ ] **Step 1: Write controller test**

Test:

```text
GET /api/traces/recent?limit=20
GET /api/traces/{traceId}
```

- [ ] **Step 2: Run controller test**

Run: `mvn -Dtest=TraceControllerTest test`

Expected: FAIL.

- [ ] **Step 3: Implement controller**

Expose recent traces and trace details. Keep response read-only.

- [ ] **Step 4: Run controller test**

Run: `mvn -Dtest=TraceControllerTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
git add src/main/java/com/zhida/aiagent/controller/TraceController.java src/test/java/com/zhida/aiagent/controller/TraceControllerTest.java
git commit -m "feat: expose rag trace logs"
```

### Task 11: Add Frontend Display For Sources, Reports, And Traces

**Files:**
- Modify: `zhida-ai-frontend/src/components/SourceCard.vue`
- Modify: `zhida-ai-frontend/src/components/MessageList.vue`
- Modify: `zhida-ai-frontend/src/api/index.js`
- Create: `zhida-ai-frontend/src/views/ResearchReport.vue`
- Create: `zhida-ai-frontend/src/views/EvaluationDashboard.vue`
- Create: `zhida-ai-frontend/src/views/TraceDashboard.vue`
- Modify: `zhida-ai-frontend/src/router/routes.js`

- [ ] **Step 1: Enhance source cards first**

Display title, category, rank, chunk index, score, and snippet. Keep layout compact.

- [ ] **Step 2: Add research report page**

Page sections:

```text
question input
category selector
generated report
research brief
sources
information gaps
```

- [ ] **Step 3: Add evaluation dashboard**

Show case count, pass count, failed cases, answer preview, and sources. Keep it table-like and simple.

- [ ] **Step 4: Add trace dashboard**

Show recent trace list and selected trace details: query, rewritten query, latency, retrieval count, and sources.

- [ ] **Step 5: Run frontend build**

Run: `cd zhida-ai-frontend; npm run build`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add zhida-ai-frontend/src/components/SourceCard.vue zhida-ai-frontend/src/components/MessageList.vue zhida-ai-frontend/src/api/index.js zhida-ai-frontend/src/views/ResearchReport.vue zhida-ai-frontend/src/views/EvaluationDashboard.vue zhida-ai-frontend/src/views/TraceDashboard.vue zhida-ai-frontend/src/router/routes.js
git commit -m "feat: add rag report and observability views"
```

---

## Chunk 4: Final Verification And Portfolio Packaging

### Task 12: End-To-End Verification

**Files:**
- Modify: `README.md`
- Modify: `DEPLOY.md`
- Optionally create: `docs/demo/agentic-rag-demo-script.md`

- [ ] **Step 1: Run backend tests**

Run: `mvn test`

Expected: PASS.

- [ ] **Step 2: Run frontend build**

Run: `cd zhida-ai-frontend; npm run build`

Expected: PASS.

- [ ] **Step 3: Manually verify demo workflow**

Verify:

```text
upload document
ask normal RAG question
check enhanced source cards
run golden-set evaluation
generate research report
open trace dashboard
```

- [ ] **Step 4: Update README**

Add:

```text
Agentic RAG project positioning
Core features
Architecture diagram
Demo workflow
Evaluation and observability highlights
Resume bullet examples
```

- [ ] **Step 5: Commit docs**

Run:

```bash
git add README.md DEPLOY.md docs/demo/agentic-rag-demo-script.md
git commit -m "docs: document agentic rag portfolio demo"
```

- [ ] **Step 6: Push to new repository**

Run: `git push`

Expected: branch pushes to `ssh://git@ssh.github.com:443/bluefateludi/zhida-agentic-rag.git`.

---

## Deadline Schedule

**April 30:** Finish Chunk 1 Task 1 to Task 3. This creates citation metadata and retrieval traces.

**May 1:** Finish Chunk 1 Task 4 and start Chunk 2. Evaluation endpoint becomes demo-ready.

**May 2:** Finish Chunk 2. Research Agent + Writer Agent report mode works through backend API.

**May 3:** Finish Chunk 3 and frontend display. Trace logs, source cards, report page, and evaluation page are present.

**May 4:** Finish Chunk 4. Run verification, update README, prepare demo script, and push.

---

## Scope Guardrails

- Do not add full user authentication before May 4.
- Do not introduce LangGraph, CrewAI, or a new Python service for this milestone.
- Do not build a complex monitoring dashboard; recent trace list plus trace details is enough.
- Do not use an LLM judge for evaluation until keyword and source-presence scoring works.
- Do not rewrite the whole RAG pipeline. Preserve the current Spring AI advisor flow and wrap trace/evaluation/report capabilities around it.

---

## Acceptance Criteria

- Normal chat returns richer source references with category, rank, chunk index, and optional trace ID.
- Golden-set evaluation can run from an API and show pass/fail results.
- Report mode produces a structured Markdown report from a `ResearchBrief`.
- Research output separates KB evidence, web evidence, and information gaps.
- Trace logs capture query, rewritten query, latency, retrieval count, source metadata, success/failure, and error message.
- Frontend can demonstrate source cards, report mode, evaluation results, and recent traces.
- `mvn test` and `npm run build` pass before final push.
