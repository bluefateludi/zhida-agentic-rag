# PM Fallback Behavior Design

**Goal:** Make PM mode answer normal product analysis questions even when the knowledge base is empty, and prevent slow external calls from leaving the user waiting forever.

## Behavior

PM mode treats product principles, product judgment, growth, user value, strategy, and organizational execution questions as in scope. If the knowledge base has no matching documents, PM mode should still answer from general product analysis ability and mention that no clear knowledge-base evidence was found.

Web research is helpful but optional. If search is slow, unavailable, or malformed, PM mode should continue with an empty research result instead of blocking the response.

Streamed PM responses should always end in a readable assistant message. If the model, RAG, or upstream calls fail, the user should receive a concise fallback reply instead of a hanging stream.

## Design

- Add a PM-specific RAG advisor factory that allows empty context while keeping the default chat advisor strict.
- Update the PM system prompt to explicitly include general product analysis questions and forbid knowledge-base-empty refusals for in-scope PM topics.
- Add timeout fallback inside `WebResearchService` around `WebSearchTool.searchWeb`.
- Add PM stream fallback handling so exceptions become a final assistant-visible response.

## Tests

- PM prompt includes normal product analysis scope and no-empty-knowledge-base refusal rule.
- PM service uses PM-specific RAG advisor.
- PM RAG config enables empty context only for PM.
- Web research times out and returns an empty result quickly.
- PM stream emits a readable fallback response when upstream streaming fails.
