<template>
  <div class="message-list" ref="listRef">
    <div v-if="messages.length === 0" class="empty-state">
      <div class="empty-state-panel">
        <span class="section-label">{{ emptyState.label }}</span>
        <h2>{{ emptyState.title }}</h2>
        <p>{{ emptyState.description }}</p>
        <div class="empty-highlights">
          <span v-for="item in emptyState.highlights" :key="item">{{ item }}</span>
        </div>
      </div>
    </div>

    <article
      v-for="(msg, index) in messages"
      :key="index"
      class="message-row"
      :class="msg.role === 'USER' ? 'user' : 'assistant'"
    >
      <div class="message-avatar">{{ msg.role === 'USER' ? userLabel : assistantLabel }}</div>
      <div class="message-body">
        <div class="message-meta">
          <span class="role-badge">{{ msg.role === 'USER' ? '提问' : '回答' }}</span>
          <span class="turn-label">第 {{ index + 1 }} 轮</span>
        </div>

        <div class="message-bubble" v-html="renderMarkdown(msg.content)"></div>

        <div v-if="msg.sources && msg.sources.length" class="sources-section">
          <button class="sources-header" @click="toggleSources(index)">
            <span>引用来源</span>
            <span class="sources-count">{{ msg.sources.length }}</span>
          </button>
          <div v-if="expandedSources[index]" class="sources-list">
            <SourceCard
              v-for="(source, si) in msg.sources"
              :key="si"
              :source="source"
            />
          </div>
        </div>
      </div>
    </article>

    <article v-if="streaming" class="message-row assistant">
      <div class="message-avatar">{{ assistantLabel }}</div>
      <div class="message-body">
        <div class="message-meta">
          <span class="role-badge">回答</span>
          <span class="turn-label">生成中</span>
        </div>
        <div class="message-bubble streaming-bubble">
          <span v-html="renderMarkdown(streamingContent)"></span>
          <span class="cursor"></span>
        </div>
      </div>
    </article>

    <div ref="bottomRef"></div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import SourceCard from './SourceCard.vue'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  streaming: { type: Boolean, default: false },
  streamingContent: { type: String, default: '' },
  assistantLabel: { type: String, default: 'AI' },
  userLabel: { type: String, default: 'ME' },
  emptyState: {
    type: Object,
    default: () => ({
      label: 'Knowledge-Grounded Dialogue',
      title: '围绕你的课程资料发起可追溯的智能问答。',
      description: '上传需求文档、报告、接口说明或设计材料后，即可在同一工作区中展示检索增强问答、流式生成和来源引用能力。',
      highlights: ['RAG 问答', '流式响应', '来源引用']
    })
  }
})

const listRef = ref(null)
const bottomRef = ref(null)
const expandedSources = ref({})

function renderMarkdown(text) {
  if (!text) return ''
  const html = marked.parse(text)
  return DOMPurify.sanitize(html)
}

function toggleSources(index) {
  expandedSources.value[index] = !expandedSources.value[index]
}

function scrollToBottom() {
  nextTick(() => {
    if (bottomRef.value) {
      bottomRef.value.scrollIntoView({ behavior: 'smooth', block: 'end' })
    }
  })
}

watch(() => props.messages.length, scrollToBottom)
watch(() => props.streamingContent, scrollToBottom)
</script>

<style scoped>
.message-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 34px 32px 18px;
}

.empty-state {
  min-height: 100%;
  display: grid;
  place-items: center;
}

.empty-state-panel {
  width: min(100%, 760px);
  padding: 34px;
  border-radius: var(--radius-panel);
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.1), transparent 38%),
    linear-gradient(180deg, rgba(229, 238, 245, 0.035), rgba(229, 238, 245, 0.012));
  border: 1px solid var(--border-default);
  box-shadow:
    inset 0 1px 0 rgba(229, 238, 245, 0.04),
    0 24px 80px rgba(0, 0, 0, 0.32);
}

.empty-state-panel h2 {
  margin-top: 14px;
  font-size: 40px;
  font-weight: 510;
  line-height: 1.12;
  letter-spacing: 0;
}

.empty-state-panel p {
  margin-top: 16px;
  color: var(--text-tertiary);
  font-size: 16px;
  line-height: 1.72;
  max-width: 620px;
}

.empty-highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
}

.empty-highlights span {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
  font-size: 13px;
}

.message-row {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 18px;
  width: min(100%, 920px);
  margin: 0 auto 28px;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: 1px solid var(--border-default);
  background:
    linear-gradient(180deg, rgba(229, 238, 245, 0.08), rgba(229, 238, 245, 0.03)),
    rgba(229, 238, 245, 0.02);
  color: var(--text-secondary);
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.user .message-avatar {
  background:
    linear-gradient(180deg, rgba(88, 166, 173, 0.28), rgba(88, 166, 173, 0.12)),
    rgba(229, 238, 245, 0.02);
  color: #fefeff;
  border-color: rgba(126, 209, 216, 0.34);
}

.message-body {
  min-width: 0;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.role-badge,
.turn-label {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.025);
  color: var(--text-tertiary);
  font-size: 12px;
}

.role-badge {
  color: var(--text-secondary);
  font-weight: 600;
}

.message-bubble {
  padding: 18px 20px;
  border-radius: var(--radius-panel);
  background:
    linear-gradient(180deg, rgba(229, 238, 245, 0.03), rgba(229, 238, 245, 0.012)),
    rgba(13, 18, 21, 0.88);
  border: 1px solid var(--border-default);
  color: var(--text-secondary);
  font-size: 15px;
  line-height: 1.75;
  word-break: break-word;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.user .message-bubble {
  background:
    linear-gradient(180deg, rgba(88, 166, 173, 0.18), rgba(88, 166, 173, 0.07)),
    rgba(11, 25, 28, 0.9);
  border-color: rgba(126, 209, 216, 0.24);
  color: #eefafa;
}

.streaming-bubble {
  position: relative;
}

.cursor {
  display: inline-block;
  width: 10px;
  height: 1.05em;
  margin-left: 6px;
  border-radius: 999px;
  background: var(--accent-hover);
  vertical-align: -0.18em;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%,
  45% {
    opacity: 1;
  }

  46%,
  100% {
    opacity: 0;
  }
}

.sources-section {
  margin-top: 12px;
}

.sources-header {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 12px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.02);
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  transition: background 0.2s ease, border-color 0.2s ease;
}

.sources-header:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--border-strong);
}

.sources-count {
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(198, 161, 91, 0.16);
  color: var(--accent-hover);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.sources-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-bubble :deep(p),
.message-bubble :deep(ul),
.message-bubble :deep(ol),
.message-bubble :deep(pre),
.message-bubble :deep(blockquote) {
  margin: 0.75em 0;
}

.message-bubble :deep(p:first-child),
.message-bubble :deep(ul:first-child),
.message-bubble :deep(ol:first-child),
.message-bubble :deep(pre:first-child),
.message-bubble :deep(blockquote:first-child) {
  margin-top: 0;
}

.message-bubble :deep(p:last-child),
.message-bubble :deep(ul:last-child),
.message-bubble :deep(ol:last-child),
.message-bubble :deep(pre:last-child),
.message-bubble :deep(blockquote:last-child) {
  margin-bottom: 0;
}

.message-bubble :deep(strong) {
  color: var(--text-primary);
  font-weight: 600;
}

.message-bubble :deep(a) {
  color: var(--accent-hover);
}

.message-bubble :deep(ul),
.message-bubble :deep(ol) {
  padding-left: 1.3em;
}

.message-bubble :deep(h1),
.message-bubble :deep(h2),
.message-bubble :deep(h3),
.message-bubble :deep(h4) {
  color: var(--text-primary);
  line-height: 1.2;
  margin-top: 1.1em;
  margin-bottom: 0.6em;
  letter-spacing: 0;
}

.message-bubble :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.92em;
}

.message-bubble :deep(p code),
.message-bubble :deep(li code) {
  padding: 0.16em 0.42em;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.06);
  color: #f2f5ff;
}

.message-bubble :deep(pre) {
  overflow-x: auto;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(0, 0, 0, 0.34);
  border: 1px solid var(--border-default);
}

.message-bubble :deep(blockquote) {
  padding-left: 14px;
  border-left: 2px solid rgba(198, 161, 91, 0.5);
  color: var(--text-tertiary);
}

@media (max-width: 768px) {
  .message-list {
    padding: 24px 18px 12px;
  }

  .empty-state-panel {
    padding: 28px 22px;
    border-radius: var(--radius-panel);
  }

  .empty-state-panel h2 {
    font-size: 26px;
    line-height: 1.18;
  }

  .empty-state-panel p {
    font-size: 14px;
    line-height: 1.62;
  }

  .empty-highlights {
    margin-top: 16px;
  }

  .empty-highlights span {
    padding: 7px 10px;
  }

  .empty-state {
    align-items: start;
    padding-top: 12px;
  }

  .message-row {
    grid-template-columns: 38px minmax(0, 1fr);
    gap: 14px;
    margin-bottom: 22px;
  }

  .message-avatar {
    width: 38px;
    height: 38px;
    border-radius: 12px;
  }

  .message-bubble {
    padding: 16px;
    border-radius: var(--radius-panel);
  }
}
</style>
