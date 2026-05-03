<template>
  <div class="kb-shell">
    <Sidebar
      :sessions="sessions"
      :activeSessionId="currentSessionId"
      :collapsed="sidebarCollapsed"
      @new-chat="handleNewChat"
      @select-session="handleSelectSession"
      @delete-session="handleDeleteSession"
      @toggle-collapse="sidebarCollapsed = !sidebarCollapsed"
    />

    <div
      v-if="!sidebarCollapsed"
      class="mobile-overlay"
      @click="sidebarCollapsed = true"
    ></div>

    <main class="kb-main">
      <header class="workspace-header">
        <div class="header-leading">
          <button class="menu-btn" @click="sidebarCollapsed = !sidebarCollapsed" aria-label="Toggle sidebar">
            <span></span>
            <span></span>
          </button>

          <h1 class="page-title">{{ currentSessionTitle }}</h1>
        </div>

        <div class="header-actions">
          <router-link class="header-link" to="/">产品首页</router-link>
          <router-link class="header-link header-link-accent" to="/report">研究报告模式</router-link>
          <router-link class="header-link" to="/evaluation">RAG 评测</router-link>
          <router-link class="header-link" to="/traces">Trace Dashboard</router-link>
          <router-link class="header-link" to="/chat/documents">知识库管理</router-link>
        </div>
      </header>

      <section class="loop-status-bar">
        <div>
          <span class="loop-kicker">Document Status</span>
          <strong>{{ documentStatus.label }}</strong>
        </div>
        <div class="loop-metrics">
          <span>{{ documentStatus.ready }}/{{ documentStatus.total }} 就绪</span>
          <span>{{ documentStatus.processing }} 处理中</span>
          <span>{{ documentStatus.error }} 异常</span>
        </div>
      </section>

      <section class="conversation-stage">
        <div class="conversation-shell surface-panel">
          <MessageList
            :messages="messages"
            :streaming="isStreaming"
            :streamingContent="streamingContent"
          />

          <ChatInput
            :disabled="isStreaming"
            :suggestions="recommendedQuestions"
            @send="handleSend"
          />
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import MessageList from '../components/MessageList.vue'
import ChatInput from '../components/ChatInput.vue'
import { createSession, listSessions, deleteSession, getChatHistory, chatStream, listDocuments } from '../api'
import { buildRecommendedQuestions, getDocumentStatusSummary } from '../utils/knowledgeLoop'

const sessions = ref([])
const currentSessionId = ref('')
const messages = ref([])
const documents = ref([])
const isStreaming = ref(false)
const streamingContent = ref('')
const sidebarCollapsed = ref(false)
let currentEventSource = null

const currentSessionTitle = computed(() => {
  const session = sessions.value.find(s => s.id === currentSessionId.value)
  return session ? session.title : '准备开始新的知识问答'
})

const documentStatus = computed(() => getDocumentStatusSummary(documents.value))
const recommendedQuestions = computed(() => buildRecommendedQuestions(documents.value))

onMounted(async () => {
  if (window.innerWidth < 1100) {
    sidebarCollapsed.value = true
  }
  await loadSessions()
  await loadDocuments()
  if (sessions.value.length > 0) {
    await handleSelectSession(sessions.value[0].id)
  }
})

async function loadSessions() {
  try {
    const res = await listSessions()
    sessions.value = res.data || []
  } catch (e) {
    console.error('Failed to load sessions', e)
  }
}

async function loadDocuments() {
  try {
    const res = await listDocuments()
    documents.value = res.data || []
  } catch (e) {
    console.error('Failed to load documents', e)
    documents.value = []
  }
}

async function handleNewChat() {
  try {
    const res = await createSession()
    const session = res.data
    sessions.value.unshift(session)
    await handleSelectSession(session.id)
  } catch (e) {
    console.error('Failed to create session', e)
  }
}

async function handleSelectSession(sessionId) {
  currentSessionId.value = sessionId
  if (window.innerWidth < 1100) {
    sidebarCollapsed.value = true
  }
  try {
    const res = await getChatHistory(sessionId)
    messages.value = (res.data || []).map(msg => ({
      role: msg.role,
      content: msg.content,
      sources: parseSources(msg.sources)
    }))
  } catch (e) {
    console.error('Failed to load chat history', e)
    messages.value = []
  }
}

async function handleDeleteSession(sessionId) {
  try {
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = ''
      messages.value = []
      if (sessions.value.length > 0) {
        await handleSelectSession(sessions.value[0].id)
      }
    }
  } catch (e) {
    console.error('Failed to delete session', e)
  }
}

function handleSend(message) {
  if (!currentSessionId.value) {
    createSession()
      .then(res => {
        const session = res.data
        sessions.value.unshift(session)
        currentSessionId.value = session.id
        doStreamChat(message, session.id)
      })
      .catch(e => console.error('Failed to create session', e))
    return
  }
  doStreamChat(message, currentSessionId.value)
}

function doStreamChat(message, sessionId) {
  messages.value.push({ role: 'USER', content: message, sources: [] })
  isStreaming.value = true
  streamingContent.value = ''

  let fullContent = ''

  if (currentEventSource) {
    currentEventSource.close()
  }

  currentEventSource = chatStream(
    message,
    sessionId,
    null,
    chunk => {
      fullContent += chunk
      streamingContent.value = fullContent
    },
    payload => {
      finishStreaming(payload.content || fullContent, payload.sources || [])
    },
    error => {
      console.error('Streaming response error', error)
      finishStreaming(fullContent, [])
    }
  )
}

function finishStreaming(content, sources = []) {
  if (!isStreaming.value && !currentEventSource) {
    return
  }
  isStreaming.value = false
  streamingContent.value = ''
  if (content) {
    messages.value.push({ role: 'ASSISTANT', content, sources })
  }
  if (currentEventSource) {
    currentEventSource.close()
    currentEventSource = null
  }
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
.kb-shell {
  position: relative;
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.kb-main {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 24px 24px 0;
}

.workspace-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 8px 8px 32px;
}

.header-leading {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.menu-btn {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex-direction: column;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.menu-btn span {
  width: 14px;
  height: 1.5px;
  border-radius: 999px;
  background: var(--text-secondary);
}

.menu-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--border-strong);
}

.page-title {
  font-size: 24px;
  font-weight: 510;
  line-height: 1.18;
  letter-spacing: 0;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.header-link {
  min-height: 40px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 13px;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.header-link:hover {
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
}

.header-link-accent {
  border-color: rgba(126, 209, 216, 0.3);
  background: rgba(88, 166, 173, 0.14);
  color: var(--text-primary);
}

.header-link-accent:hover {
  border-color: rgba(126, 209, 216, 0.44);
  background: rgba(88, 166, 173, 0.2);
}

.conversation-stage {
  flex: 1 1 auto;
  min-height: 0;
  padding-left: 32px;
  padding-top: 2px;
}

.loop-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin: 0 0 12px 32px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
}

.loop-kicker {
  display: block;
  margin-bottom: 4px;
  color: var(--text-quaternary);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.loop-status-bar strong {
  color: var(--text-primary);
  font-size: 14px;
}

.loop-metrics {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.loop-metrics span {
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.035);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  font-size: 12px;
}

.conversation-shell {
  position: relative;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-panel);
  overflow: hidden;
}

.mobile-overlay {
  display: none;
}

@media (max-width: 1200px) {
  .kb-main {
    padding-left: 24px;
  }

  .workspace-header,
  .conversation-stage {
    padding-left: 0;
  }

  .workspace-header {
    padding-right: 0;
  }

  .loop-status-bar {
    margin-left: 0;
  }

}

@media (max-width: 768px) {
  .kb-main {
    padding: 16px;
  }

  .workspace-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 0 0 8px;
  }

  .loop-status-bar {
    flex-direction: column;
    align-items: flex-start;
    margin-left: 0;
  }

  .loop-metrics {
    justify-content: flex-start;
  }

  .header-leading {
    width: 100%;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .conversation-stage {
    padding: 0;
  }

  .conversation-shell {
    border-radius: var(--radius-panel);
  }

  .mobile-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(2, 2, 3, 0.62);
    backdrop-filter: blur(4px);
    z-index: 8;
  }
}
</style>
