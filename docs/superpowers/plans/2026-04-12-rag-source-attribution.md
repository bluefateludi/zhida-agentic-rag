# RAG Source Attribution Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Spring AI RAG augmentation include document metadata so assistant replies can cite source titles or file names, and remove obsolete tests that block `mvn test`.

**Architecture:** Introduce a small formatter in the RAG layer to serialize retrieved `Document` metadata into the augmented context, then inject that formatter into `RetrievalAugmentationAdvisor` via a custom `ContextualQueryAugmenter`. Clean up the two stale tests that reference deleted `LoveApp` classes.

**Tech Stack:** Java 21, Spring Boot 3.4, Spring AI 1.0.0, JUnit 5, Maven

---

## Chunk 1: Source-aware formatter

### Task 1: Add regression test for metadata-rich document context

**Files:**
- Create: `src/test/java/com/zhida/aiagent/rag/SourceAwareDocumentFormatterTest.java`
- Create: `src/main/java/com/zhida/aiagent/rag/SourceAwareDocumentFormatter.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void shouldIncludeDocumentTitleAndFileNameInFormattedContext() {
    var formatter = new SourceAwareDocumentFormatter();
    var documents = List.of(new Document("MCP 内容", Map.of(
            "documentTitle", "MCP 文章",
            "fileName", "article-mcp.md"
    )));

    String context = formatter.apply(documents);

    assertThat(context).contains("MCP 文章");
    assertThat(context).contains("article-mcp.md");
    assertThat(context).contains("MCP 内容");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q test "-Dtest=SourceAwareDocumentFormatterTest"`
Expected: FAIL because `SourceAwareDocumentFormatter` does not exist yet

- [ ] **Step 3: Write minimal implementation**

```java
public class SourceAwareDocumentFormatter implements Function<List<Document>, String> {
    @Override
    public String apply(List<Document> documents) {
        // build metadata-rich context blocks
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q test "-Dtest=SourceAwareDocumentFormatterTest"`
Expected: PASS

### Task 2: Wire formatter into RAG advisor

**Files:**
- Modify: `src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java`

- [ ] **Step 1: Inject a custom query augmenter**

```java
var queryAugmenter = ContextualQueryAugmenter.builder()
        .documentFormatter(new SourceAwareDocumentFormatter())
        .build();
```

- [ ] **Step 2: Use it in the advisor**

```java
return RetrievalAugmentationAdvisor.builder()
        .documentRetriever(retriever)
        .queryAugmenter(queryAugmenter)
        .build();
```

- [ ] **Step 3: Run targeted verification**

Run: `mvn -q test "-Dtest=SourceAwareDocumentFormatterTest"`
Expected: PASS

## Chunk 2: Test cleanup

### Task 3: Remove obsolete LoveApp tests

**Files:**
- Delete: `src/test/java/com/zhida/aiagent/app/LoveAppTest.java`
- Delete: `src/test/java/com/zhida/aiagent/rag/LoveAppDocumentLoaderTest.java`

- [ ] **Step 1: Delete the stale tests**
- [ ] **Step 2: Run the old failing command**

Run: `mvn -q test "-Dtest=LoveAppTest,LoveAppDocumentLoaderTest"`
Expected: `No tests matching pattern` or no `NoClassDefFoundError`

- [ ] **Step 3: Run a broader regression check**

Run: `mvn -q test`
Expected: PASS, or surface any remaining unrelated failures with evidence
