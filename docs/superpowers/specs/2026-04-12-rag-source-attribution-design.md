# RAG Source Attribution Design

**Goal:** Make the model see document metadata during RAG augmentation so assistant replies can mention concrete source documents, while preserving the existing structured `sources` response for the frontend.

## Context

The current retrieval flow already captures retrieved `Document` objects in [`ChatService`](src/main/java/com/zhida/aiagent/service/ChatService.java) and returns `sources` for UI display. The missing link is prompt augmentation: Spring AI's default `ContextualQueryAugmenter` document formatter only concatenates document text, so metadata like `documentTitle` and `fileName` never reaches the model.

## Design

### 1. Source-aware document formatter

Add a focused formatter in the RAG package that converts retrieved documents into a metadata-rich context block. Each entry should include:

- document title when available
- file name when available
- document text

Fallback behavior:

- if title is absent, use file name
- if both title and file name are absent, label the source as "未命名文档"
- if text is absent, render empty content safely

### 2. Plug formatter into RAG advisor

Update [`KnowledgeBaseRagConfig`](src/main/java/com/zhida/aiagent/rag/KnowledgeBaseRagConfig.java) to build a custom `ContextualQueryAugmenter` and inject it into `RetrievalAugmentationAdvisor`. This preserves the existing retriever and search parameters while changing only how retrieved documents are serialized into model context.

### 3. Remove obsolete tests

Delete the two legacy tests that still reference removed `LoveApp` code paths:

- [`LoveAppTest`](src/test/java/com/zhida/aiagent/app/LoveAppTest.java)
- [`LoveAppDocumentLoaderTest`](src/test/java/com/zhida/aiagent/rag/LoveAppDocumentLoaderTest.java)

These tests no longer validate supported behavior and currently block normal `mvn test`.

## Verification

1. Add a unit test for the new formatter that fails until metadata is included in output.
2. Run that test red-green.
3. Run a targeted Maven test command including the formatter test.
4. Run a broader Maven test command to confirm the deleted legacy tests no longer break the build.
