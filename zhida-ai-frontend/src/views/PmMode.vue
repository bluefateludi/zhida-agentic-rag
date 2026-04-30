<template>
  <div class="pm-page">
    <div class="pm-ambient pm-ambient-left"></div>
    <div class="pm-ambient pm-ambient-right"></div>

    <div class="pm-shell">
      <header class="pm-header">
        <div class="pm-header-main">
          <span class="pm-identity-mark">
            <img src="https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com/zhidalogo.png" alt="智答 AI logo" />
          </span>
          <div class="pm-copy">
            <span class="section-label">Product Manager Mode</span>
            <h1>产品分析模式</h1>
            <p class="pm-subtitle">保留产品判断、联网研究与知识库参考。</p>
          </div>
          <span class="pm-status">联网 + 知识库</span>
        </div>

        <div class="pm-header-actions">
          <button class="pm-action" @click="handleNewSession">新建对话</button>
          <router-link class="pm-action ghost" to="/">首页</router-link>
          <router-link class="pm-action ghost" to="/chat">知识库工作台</router-link>
        </div>
      </header>

      <section class="pm-session-strip" v-if="sessions.length">
        <button
          v-for="session in sessions"
          :key="session.id"
          class="pm-session-chip"
          :class="{ active: session.id === currentSessionId }"
          @click="handleSelectSession(session.id)"
        >
          <span class="chip-title">{{ session.title }}</span>
          <span class="chip-delete" @click.stop="handleDeleteSession(session.id)">删除</span>
        </button>
      </section>

      <main class="pm-stage surface-panel">
        <MessageList
          :messages="messages"
          :streaming="isStreaming"
          :streamingContent="streamingContent"
          assistant-label="PM"
          :empty-state="pmEmptyState"
        />

        <ChatInput
          :disabled="isStreaming"
          placeholder="输入你想讨论的产品、增长、用户价值、组织效率或战略问题..."
          button-label="开始讨论"
          @send="handleSend"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import MessageList from '../components/MessageList.vue'
import ChatInput from '../components/ChatInput.vue'
import { createPmSession, deletePmSession, getPmHistory, listPmSessions, pmChatStream } from '../api'

const sessions = ref([])
const currentSessionId = ref('')
const messages = ref([])
const isStreaming = ref(false)
const streamingContent = ref('')
let currentEventSource = null
let activeStreamId = 0

const currentSession = computed(() => sessions.value.find(item => item.id === currentSessionId.value) || null)
const pmEmptyState = {
  label: 'Product Analysis Mode',
  title: '把问题讲透，再做判断。',
  description: '这里保留产品经理视角的分析能力。系统会同时参考联网研究和本地知识库，用更直接、更偏产品判断的方式回应问题。',
  highlights: ['产品判断', '联网研究', '知识库参考']
}

onMounted(async () => {
  await loadSessions()
  if (sessions.value.length > 0) {
    await handleSelectSession(sessions.value[0].id)
  } else {
    await handleNewSession()
  }
})

async function loadSessions() {
  const res = await listPmSessions()
  sessions.value = res.data || []
}

async function handleNewSession() {
  stopCurrentStream()
  resetStreamingState()
  const res = await createPmSession(currentSession.value ? '新的产品判断' : undefined)
  const session = res.data
  sessions.value.unshift(session)
  await handleSelectSession(session.id)
}

async function handleSelectSession(sessionId) {
  stopCurrentStream()
  resetStreamingState()
  currentSessionId.value = sessionId
  const res = await getPmHistory(sessionId)
  messages.value = (res.data || []).map(msg => ({
    role: msg.role,
    content: msg.content,
    sources: parseSources(msg.sources)
  }))
}

async function handleDeleteSession(sessionId) {
  if (currentSessionId.value === sessionId) {
    stopCurrentStream()
    resetStreamingState()
  }
  await deletePmSession(sessionId)
  sessions.value = sessions.value.filter(item => item.id !== sessionId)
  if (currentSessionId.value === sessionId) {
    currentSessionId.value = ''
    messages.value = []
    if (sessions.value.length > 0) {
      await handleSelectSession(sessions.value[0].id)
    }
  }
}

function handleSend(message) {
  if (!currentSessionId.value) {
    handleNewSession().then(() => doPmChat(message, currentSessionId.value))
    return
  }
  doPmChat(message, currentSessionId.value)
}

function doPmChat(message, sessionId) {
  stopCurrentStream()
  messages.value.push({ role: 'USER', content: message, sources: [] })
  isStreaming.value = true
  streamingContent.value = ''

  let fullContent = ''
  const streamId = ++activeStreamId

  currentEventSource = pmChatStream(
    message,
    sessionId,
    null,
    chunk => {
      if (streamId !== activeStreamId || sessionId !== currentSessionId.value) return
      fullContent += chunk
      streamingContent.value = fullContent
    },
    payload => {
      if (streamId !== activeStreamId || sessionId !== currentSessionId.value) return
      finishStreaming(payload.content || fullContent, payload.sources || [])
    },
    () => {
      if (streamId !== activeStreamId || sessionId !== currentSessionId.value) return
      finishStreaming('', [], '生成失败，请重试。')
    }
  )
}

function stopCurrentStream() {
  activeStreamId += 1
  if (currentEventSource) {
    currentEventSource.close()
    currentEventSource = null
  }
}

function resetStreamingState() {
  isStreaming.value = false
  streamingContent.value = ''
}

function finishStreaming(content, sources = [], fallbackMessage = '') {
  resetStreamingState()
  const finalContent = content || fallbackMessage
  if (finalContent) {
    messages.value.push({ role: 'ASSISTANT', content: finalContent, sources })
  }
  stopCurrentStream()
}

function parseSources(sourcesStr) {
  if (!sourcesStr) return []
  try {
    return JSON.parse(sourcesStr)
  } catch {
    return []
  }
}
</script>

<style scoped>
.pm-page {
  position: relative;
  height: 100vh;
  overflow: hidden;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.08), transparent 34%),
    linear-gradient(180deg, #071014 0%, #080b0d 50%, #050607 100%);
}

.pm-ambient-left {
  display: none;
}

.pm-ambient-right {
  display: none;
}

.pm-shell {
  position: relative;
  z-index: 1;
  width: min(100%, 1220px);
  margin: 0 auto;
  height: calc(100vh - 48px);
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.pm-header-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pm-identity-mark {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(229, 238, 245, 0.42);
  background: #ffffff;
  display: block;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 14px 28px rgba(31, 91, 98, 0.22);
}

.pm-identity-mark img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(1.78);
}

.pm-copy {
  min-width: 0;
}

.pm-header h1 {
  margin-top: 6px;
  color: var(--text-primary);
  font-size: 32px;
  font-weight: 510;
  line-height: 1.16;
  letter-spacing: 0;
}

.pm-subtitle {
  margin-top: 8px;
  color: var(--text-tertiary);
  font-size: 14px;
  line-height: 1.65;
}

.pm-status,
.pm-session-chip,
.pm-action {
  min-height: 40px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
}

.pm-status {
  border: 1px solid rgba(126, 209, 216, 0.24);
  background: rgba(88, 166, 173, 0.1);
  color: var(--accent-hover);
  font-size: 13px;
  white-space: nowrap;
}

.pm-header-actions,
.pm-session-strip {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pm-action {
  min-width: 102px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  color: var(--text-secondary);
  justify-content: center;
  font-size: 13px;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.pm-action:hover,
.pm-session-chip:hover {
  transform: translateY(-1px);
}

.pm-action:not(.ghost) {
  border-color: rgba(126, 209, 216, 0.28);
  background: rgba(88, 166, 173, 0.14);
  color: var(--text-primary);
}

.pm-session-strip {
  overflow-x: auto;
  padding-bottom: 2px;
}

.pm-session-chip {
  gap: 12px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.02);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.pm-session-chip.active {
  border-color: rgba(126, 209, 216, 0.32);
  background: rgba(88, 166, 173, 0.12);
  color: var(--text-primary);
}

.chip-title {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip-delete {
  font-size: 12px;
  color: var(--text-quaternary);
}

.pm-stage {
  flex: 1;
  min-height: 0;
  border-radius: var(--radius-panel);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.06), transparent 32%),
    linear-gradient(180deg, rgba(229, 238, 245, 0.028), rgba(229, 238, 245, 0.014)),
    rgba(11, 12, 14, 0.88);
}

@media (max-width: 768px) {
  .pm-page {
    padding: 16px;
  }

  .pm-shell {
    height: calc(100vh - 32px);
  }

  .pm-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .pm-header-main {
    width: 100%;
    align-items: flex-start;
    gap: 12px;
  }

  .pm-header h1 {
    font-size: 28px;
    line-height: 1.2;
  }

  .pm-status {
    margin-left: auto;
  }

  .pm-header-actions,
  .pm-session-strip {
    width: 100%;
  }

  .pm-action,
  .pm-session-chip {
    width: 100%;
    justify-content: space-between;
  }

  .chip-title {
    max-width: none;
  }

  .pm-stage {
    border-radius: var(--radius-panel);
  }
}
</style>
