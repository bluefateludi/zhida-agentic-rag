<template>
  <div class="trace-page">
    <header class="trace-header">
      <div class="trace-title">
        <span class="section-label">RAG Observability</span>
        <h1>Trace Dashboard</h1>
        <p>查看最近 RAG 执行链路、问题改写、延迟、召回数量和来源片段。</p>
      </div>

      <nav class="trace-nav">
        <router-link to="/">首页</router-link>
        <router-link to="/chat">知识库工作台</router-link>
        <router-link to="/report">研究报告模式</router-link>
        <router-link to="/evaluation">RAG 评测</router-link>
      </nav>
    </header>

    <main class="trace-main">
      <section class="summary-grid">
        <article class="summary-card surface-panel">
          <span>最近 Trace</span>
          <strong>{{ recentTraces.length }}</strong>
        </article>
        <article class="summary-card surface-panel">
          <span>成功执行</span>
          <strong>{{ successCount }}</strong>
        </article>
        <article class="summary-card surface-panel">
          <span>平均延迟</span>
          <strong>{{ averageLatency }}ms</strong>
        </article>
        <article class="summary-card surface-panel">
          <span>平均召回</span>
          <strong>{{ averageRetrievalCount }}</strong>
        </article>
      </section>

      <section class="trace-toolbar surface-panel">
        <div>
          <span class="section-label">Trace Control</span>
          <h2>最近执行记录</h2>
        </div>
        <button class="control-button refresh-button" type="button" :disabled="isLoading" @click="loadTraces">
          {{ isLoading ? '刷新中...' : '刷新' }}
        </button>
      </section>

      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <section class="trace-grid">
        <aside class="trace-list-panel surface-panel">
          <div class="panel-heading">
            <span class="section-label">Recent Traces</span>
            <span>{{ recentTraces.length }} items</span>
          </div>

          <div v-if="recentTraces.length" class="trace-list">
            <button
              v-for="trace in recentTraces"
              :key="trace.traceId"
              class="trace-row"
              :class="{ active: selectedTrace?.traceId === trace.traceId, failed: trace.success === false }"
              type="button"
              @click="selectTrace(trace.traceId)"
            >
              <div class="trace-row-top">
                <span>{{ trace.mode || 'RAG' }}</span>
                <strong>{{ trace.latencyMs ?? 0 }}ms</strong>
              </div>
              <p>{{ trace.originalQuery || '未记录原始问题' }}</p>
              <div class="trace-row-meta">
                <span>{{ trace.retrievalCount ?? 0 }} sources</span>
                <span>{{ formatTime(trace.createdAt) }}</span>
              </div>
            </button>
          </div>
          <p v-else class="empty-text">暂无 trace 记录。</p>
        </aside>

        <article class="trace-detail-panel surface-panel">
          <div v-if="selectedTrace" class="detail-content">
            <div class="detail-heading">
              <div>
                <span class="section-label">Trace Detail</span>
                <h2>{{ selectedTrace.traceId }}</h2>
              </div>
              <span class="status-chip" :class="{ failed: selectedTrace.success === false }">
                {{ selectedTrace.success === false ? 'FAILED' : 'SUCCESS' }}
              </span>
            </div>

            <div class="query-grid">
              <article>
                <span>originalQuery</span>
                <p>{{ selectedTrace.originalQuery || '未记录' }}</p>
              </article>
              <article>
                <span>rewrittenQuery</span>
                <p>{{ selectedTrace.rewrittenQuery || '未记录' }}</p>
              </article>
            </div>

            <div class="metric-strip">
              <span>mode {{ selectedTrace.mode || '-' }}</span>
              <span>category {{ selectedTrace.category || 'all' }}</span>
              <span>latencyMs {{ selectedTrace.latencyMs ?? 0 }}</span>
              <span>retrievalCount {{ selectedTrace.retrievalCount ?? sources.length }}</span>
              <span>session {{ selectedTrace.sessionId || '-' }}</span>
            </div>

            <p v-if="selectedTrace.errorMessage" class="error-message inline-error">
              {{ selectedTrace.errorMessage }}
            </p>

            <section class="source-section">
              <div class="panel-heading">
                <span class="section-label">Sources</span>
                <span>{{ sources.length }} retrieved</span>
              </div>

              <div v-if="sources.length" class="source-list">
                <article v-for="(source, index) in sources" :key="`${source.fileName || 'source'}-${index}`" class="source-card">
                  <div class="source-meta">
                    <span>#{{ source.rank || index + 1 }}</span>
                    <span>{{ source.category || selectedTrace.category || 'knowledge' }}</span>
                    <span v-if="source.score !== null && source.score !== undefined">score {{ formatScore(source.score) }}</span>
                  </div>
                  <h3>{{ source.documentTitle || source.fileName || '知识库片段' }}</h3>
                  <p>{{ source.contentPreview || source.relevantContent || '暂无片段预览' }}</p>
                  <small>{{ source.fileName || '未命名来源' }} · chunk {{ source.chunkIndex || '-' }}</small>
                </article>
              </div>
              <p v-else class="empty-text">这条 trace 没有记录召回来源。</p>
            </section>
          </div>

          <div v-else class="empty-detail">
            <span class="section-label">Waiting</span>
            <h2>选择一条 trace 查看详情。</h2>
            <p>运行 `/chat`、`/pm`、`/report` 或 `/evaluation` 后，RAG 执行记录会出现在这里。</p>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getTraceDetail, listRecentTraces } from '../api'

const route = useRoute()
const recentTraces = ref([])
const selectedTrace = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')

const successCount = computed(() => recentTraces.value.filter(trace => trace.success !== false).length)
const averageLatency = computed(() => {
  if (!recentTraces.value.length) return 0
  const total = recentTraces.value.reduce((sum, trace) => sum + Number(trace.latencyMs || 0), 0)
  return Math.round(total / recentTraces.value.length)
})
const averageRetrievalCount = computed(() => {
  if (!recentTraces.value.length) return 0
  const total = recentTraces.value.reduce((sum, trace) => sum + Number(trace.retrievalCount || 0), 0)
  return (total / recentTraces.value.length).toFixed(1)
})
const requestedTraceId = computed(() => {
  const value = route.query.traceId
  return Array.isArray(value) ? value[0] : value
})
const sources = computed(() => parseSources(selectedTrace.value))

onMounted(loadTraces)

async function loadTraces() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const res = await listRecentTraces(20)
    recentTraces.value = res.data || []
    const targetTraceId = requestedTraceId.value || recentTraces.value[0]?.traceId
    if (targetTraceId) {
      await selectTrace(targetTraceId)
    } else {
      selectedTrace.value = null
    }
  } catch (error) {
    console.error('Failed to load traces', error)
    errorMessage.value = 'Trace 加载失败，请检查后端服务。'
  } finally {
    isLoading.value = false
  }
}

async function selectTrace(traceId) {
  if (!traceId) return
  try {
    const res = await getTraceDetail(traceId)
    selectedTrace.value = res.data
  } catch (error) {
    console.error('Failed to load trace detail', error)
    errorMessage.value = 'Trace 详情加载失败。'
  }
}

function parseSources(trace) {
  if (!trace) return []
  const fromTraceJson = parseJson(trace.traceJson)?.retrievals
  if (Array.isArray(fromTraceJson)) return fromTraceJson
  const fromSourcesJson = parseJson(trace.sourcesJson)
  return Array.isArray(fromSourcesJson) ? fromSourcesJson : []
}

function parseJson(value) {
  if (!value) return null
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

function formatTime(value) {
  if (!value) return 'unknown'
  return new Date(value).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}

function formatScore(score) {
  return Number(score).toFixed(2)
}
</script>

<style scoped>
.trace-page {
  height: 100vh;
  overflow-y: auto;
  padding: 24px 24px 40px;
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.08), transparent 34%),
    linear-gradient(180deg, #071014 0%, #080b0d 38%, #050607 100%);
  -webkit-overflow-scrolling: touch;
}

.trace-header,
.trace-main {
  width: min(100%, 1360px);
  margin: 0 auto;
}

.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
}

.trace-title h1 {
  margin-top: 8px;
  color: var(--text-primary);
  font-size: 34px;
  font-weight: 510;
  line-height: 1.14;
  letter-spacing: 0;
}

.trace-title p {
  margin-top: 10px;
  max-width: 720px;
  color: var(--text-tertiary);
  line-height: 1.65;
}

.trace-nav,
.summary-grid,
.trace-toolbar,
.panel-heading,
.trace-row-top,
.trace-row-meta,
.detail-heading,
.metric-strip,
.source-meta {
  display: flex;
  align-items: center;
}

.trace-nav,
.metric-strip,
.source-meta,
.trace-row-meta {
  gap: 10px;
  flex-wrap: wrap;
}

.trace-nav {
  justify-content: flex-end;
}

.trace-nav a {
  min-height: 40px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.trace-nav a:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.05);
}

.trace-main {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card,
.trace-toolbar,
.trace-list-panel,
.trace-detail-panel {
  background:
    linear-gradient(180deg, rgba(229, 238, 245, 0.035), rgba(229, 238, 245, 0.012)),
    rgba(11, 14, 16, 0.9);
}

.summary-card {
  padding: 18px 20px;
}

.summary-card span {
  color: var(--text-quaternary);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.summary-card strong {
  display: block;
  margin-top: 10px;
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 510;
  line-height: 1.2;
}

.trace-toolbar {
  padding: 18px 20px;
  justify-content: space-between;
  gap: 16px;
}

.trace-toolbar h2,
.detail-heading h2,
.empty-detail h2 {
  margin-top: 8px;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 510;
  line-height: 1.2;
  letter-spacing: 0;
}

.refresh-button {
  min-width: 92px;
  border-color: rgba(126, 209, 216, 0.32);
  background: rgba(88, 166, 173, 0.14);
  color: var(--text-primary);
}

.refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
  transform: none;
}

.error-message {
  padding: 12px 14px;
  border-radius: var(--radius-panel);
  border: 1px solid rgba(217, 103, 103, 0.24);
  background: rgba(217, 103, 103, 0.08);
  color: #f6c4c4;
  line-height: 1.6;
}

.trace-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.82fr) minmax(0, 1.18fr);
  gap: 16px;
  align-items: flex-start;
}

.trace-list-panel,
.trace-detail-panel {
  padding: 20px;
}

.panel-heading {
  justify-content: space-between;
  gap: 12px;
  color: var(--text-tertiary);
}

.trace-list {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.trace-row {
  width: 100%;
  padding: 14px;
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  text-align: left;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.trace-row:hover,
.trace-row.active {
  transform: translateY(-1px);
  border-color: rgba(126, 209, 216, 0.34);
  background: rgba(88, 166, 173, 0.08);
}

.trace-row.failed {
  border-color: rgba(217, 103, 103, 0.2);
}

.trace-row-top,
.trace-row-meta {
  justify-content: space-between;
}

.trace-row-top span,
.trace-row-meta span,
.metric-strip span,
.source-meta span,
.status-chip {
  min-height: 26px;
  padding: 0 9px;
  border-radius: 999px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-quaternary);
  display: inline-flex;
  align-items: center;
  font-size: 11px;
}

.trace-row-top strong {
  color: var(--accent-hover);
  font-size: 13px;
}

.trace-row p {
  margin-top: 10px;
  color: var(--text-primary);
  line-height: 1.55;
}

.trace-row-meta {
  margin-top: 10px;
}

.detail-heading {
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.detail-heading h2 {
  max-width: 680px;
  font-size: 22px;
  word-break: break-all;
}

.status-chip {
  border-color: rgba(79, 180, 119, 0.22);
  background: rgba(79, 180, 119, 0.1);
  color: #89e2ac;
}

.status-chip.failed {
  border-color: rgba(217, 103, 103, 0.22);
  background: rgba(217, 103, 103, 0.12);
  color: #f2b1b1;
}

.query-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.query-grid article,
.source-card {
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
}

.query-grid article {
  padding: 16px;
}

.query-grid span {
  color: var(--accent-hover);
  font-size: 12px;
  font-weight: 600;
}

.query-grid p,
.empty-text,
.empty-detail p,
.source-card p {
  margin-top: 10px;
  color: var(--text-tertiary);
  line-height: 1.7;
}

.metric-strip {
  margin-top: 16px;
}

.metric-strip span {
  color: var(--text-secondary);
}

.inline-error {
  margin-top: 16px;
}

.source-section {
  margin-top: 22px;
}

.source-list {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.source-card {
  padding: 16px;
}

.source-meta {
  justify-content: space-between;
}

.source-meta span:first-child {
  color: var(--accent-hover);
  border-color: rgba(126, 209, 216, 0.2);
  background: rgba(88, 166, 173, 0.1);
}

.source-card h3 {
  margin-top: 12px;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 510;
  line-height: 1.32;
}

.source-card small {
  display: inline-block;
  margin-top: 12px;
  color: var(--text-quaternary);
  line-height: 1.55;
  word-break: break-all;
}

.empty-detail {
  min-height: 460px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.empty-detail h2 {
  font-size: 28px;
}

@media (max-width: 1100px) {
  .trace-header,
  .trace-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .trace-nav {
    justify-content: flex-start;
  }

  .summary-grid,
  .trace-grid,
  .query-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .trace-page {
    padding: 16px 16px 28px;
  }

  .trace-title h1 {
    font-size: 28px;
  }

  .trace-list-panel,
  .trace-detail-panel {
    padding: 16px;
  }

  .detail-heading {
    flex-direction: column;
  }
}
</style>
