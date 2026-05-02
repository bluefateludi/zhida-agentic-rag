<template>
  <div class="report-page">
    <header class="report-header">
      <div class="report-brand">
        <span class="report-mark">
          <img src="https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com/zhidalogo.png" alt="智答 AI logo" />
        </span>
        <div>
          <span class="section-label">Research Report Mode</span>
          <h1>研究报告模式</h1>
          <p>研究 Agent 整理证据，写作 Agent 输出 Markdown 报告。</p>
        </div>
      </div>

      <nav class="report-nav">
        <router-link to="/">首页</router-link>
        <router-link to="/chat">知识库工作台</router-link>
        <router-link to="/pm">产品分析模式</router-link>
      </nav>
    </header>

    <main class="report-main">
      <section class="report-console surface-panel">
        <div class="console-heading">
          <div>
            <span class="section-label">Research Input</span>
            <h2>发起研究</h2>
          </div>
          <span class="status-chip" :class="{ loading: isLoading }">{{ statusLabel }}</span>
        </div>

        <form class="report-form" @submit.prevent="handleGenerate">
          <label class="field">
            <span>研究问题</span>
            <textarea
              v-model.trim="form.question"
              rows="7"
              placeholder="例如：面向企业知识库的 Agentic RAG 系统，如何体现可追溯性和作品展示价值？"
            ></textarea>
          </label>

          <label class="field">
            <span>知识库分类</span>
            <select v-model="form.category">
              <option value="">全部分类</option>
              <option value="product">产品设计</option>
              <option value="rag">RAG / 检索增强</option>
              <option value="agent">Agent 架构</option>
              <option value="interview">面试 / 简历材料</option>
            </select>
          </label>

          <div class="button-row">
            <button class="control-button submit-button" type="submit" :disabled="isLoading || !form.question">
              {{ isLoading ? '生成中...' : '生成研究报告' }}
            </button>
            <button class="control-button ghost-button" type="button" :disabled="isLoading" @click="handleReset">
              清空
            </button>
          </div>
        </form>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div class="suggestion-stack">
          <span class="section-label">Demo Prompts</span>
          <button
            v-for="suggestion in suggestions"
            :key="suggestion"
            type="button"
            class="suggestion-button"
            @click="form.question = suggestion"
          >
            {{ suggestion }}
          </button>
        </div>
      </section>

      <section class="report-workspace">
        <div class="metric-grid">
          <article class="metric-card surface-panel">
            <span>改写问题</span>
            <strong>{{ brief?.rewrittenQuestion || '等待生成' }}</strong>
          </article>
          <article class="metric-card surface-panel">
            <span>知识库证据</span>
            <strong>{{ kbEvidence.length }} 条</strong>
          </article>
          <article class="metric-card surface-panel">
            <span>联网证据</span>
            <strong>{{ webEvidence.length }} 条</strong>
          </article>
          <article class="metric-card surface-panel">
            <span>信息缺口</span>
            <strong>{{ informationGaps.length }} 项</strong>
          </article>
        </div>

        <div v-if="hasReport" class="result-layout">
          <article class="markdown-panel surface-panel">
            <div class="panel-heading">
              <div>
                <span class="section-label">Markdown Report</span>
                <h2>研究报告正文</h2>
              </div>
              <span class="generated-time">{{ formattedGeneratedAt }}</span>
            </div>
            <div class="markdown-body" v-html="renderedReport"></div>
          </article>

          <aside class="brief-panel">
            <article class="brief-card surface-panel">
              <span class="section-label">Research Brief</span>
              <h2>研究简报</h2>

              <div class="brief-block">
                <span>原始问题</span>
                <p>{{ brief?.originalQuestion }}</p>
              </div>

              <div class="brief-block">
                <span>rewrittenQuestion</span>
                <p>{{ brief?.rewrittenQuestion }}</p>
              </div>

              <div class="brief-block">
                <span>informationGaps</span>
                <ul v-if="informationGaps.length">
                  <li v-for="item in informationGaps" :key="item">{{ item }}</li>
                </ul>
                <p v-else>暂无明显信息缺口。</p>
              </div>
            </article>

            <article class="brief-card surface-panel">
              <div class="panel-heading">
                <div>
                  <span class="section-label">kbEvidence</span>
                  <h2>知识库证据</h2>
                </div>
                <span class="count-chip">{{ kbEvidence.length }}</span>
              </div>

              <div v-if="kbEvidence.length" class="evidence-list">
                <article v-for="(item, index) in kbEvidence" :key="`kb-${index}`" class="evidence-card">
                  <div class="evidence-meta">
                    <span>#{{ item.rank || index + 1 }}</span>
                    <span>{{ item.category || 'knowledge' }}</span>
                  </div>
                  <h3>{{ item.documentTitle || item.fileName || '知识库片段' }}</h3>
                  <p>{{ item.relevantContent || '暂无摘要' }}</p>
                  <small>{{ item.fileName || '未命名来源' }}</small>
                </article>
              </div>
              <p v-else class="empty-text">本轮研究没有检索到知识库证据。</p>
            </article>

            <article class="brief-card surface-panel">
              <div class="panel-heading">
                <div>
                  <span class="section-label">webEvidence</span>
                  <h2>联网证据</h2>
                </div>
                <span class="count-chip">{{ webEvidence.length }}</span>
              </div>

              <div v-if="webEvidence.length" class="evidence-list">
                <article v-for="(item, index) in webEvidence" :key="`web-${index}`" class="evidence-card web-card">
                  <div class="evidence-meta">
                    <span>#{{ item.rank || index + 1 }}</span>
                    <span>{{ item.category || 'web' }}</span>
                  </div>
                  <h3>{{ item.documentTitle || '联网资料' }}</h3>
                  <p>{{ item.relevantContent || '暂无摘要' }}</p>
                  <a v-if="item.fileName" :href="item.fileName" target="_blank" rel="noreferrer">{{ item.fileName }}</a>
                </article>
              </div>
              <p v-else class="empty-text">当前没有使用联网证据。</p>
            </article>
          </aside>
        </div>

        <div v-else class="empty-panel surface-panel">
          <span class="section-label">Report Workspace</span>
          <h2>报告会在这里展开。</h2>
          <p>生成后会展示 Markdown 正文、rewrittenQuestion、kbEvidence、webEvidence 和 informationGaps，方便直接用于项目演示或面试讲解。</p>
          <div class="empty-tags">
            <span>Markdown</span>
            <span>ResearchBrief</span>
            <span>Evidence</span>
            <span>Gaps</span>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { generateReport } from '../api'

const suggestions = [
  '企业知识库问答系统在作品集展示中，最值得强调的工程亮点有哪些？',
  '如何从 RAG 可追溯性角度解释这个系统为什么适合企业知识库场景？',
  '研究报告模式应该如何帮助面试官快速理解 Agentic RAG 系统价值？'
]

const form = reactive({
  question: '',
  category: ''
})

const brief = ref(null)
const reportMarkdown = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

const hasReport = computed(() => Boolean(brief.value && reportMarkdown.value))
const kbEvidence = computed(() => brief.value?.kbEvidence || [])
const webEvidence = computed(() => brief.value?.webEvidence || [])
const informationGaps = computed(() => brief.value?.informationGaps || [])
const statusLabel = computed(() => {
  if (isLoading.value) return '生成中'
  if (hasReport.value) return '已生成'
  return '等待输入'
})
const renderedReport = computed(() => {
  if (!reportMarkdown.value) return ''
  return DOMPurify.sanitize(marked.parse(reportMarkdown.value))
})
const formattedGeneratedAt = computed(() => {
  const generatedAt = brief.value?.generatedAt
  if (!generatedAt) return '等待生成时间'
  return new Date(generatedAt).toLocaleString('zh-CN', { hour12: false })
})

async function handleGenerate() {
  if (!form.question) {
    errorMessage.value = '请输入要研究的问题。'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const res = await generateReport(form.question, form.category)
    brief.value = res.data?.brief || null
    reportMarkdown.value = res.data?.reportMarkdown || ''
    if (!brief.value || !reportMarkdown.value) {
      errorMessage.value = '报告结果为空，请稍后重试。'
    }
  } catch (error) {
    console.error('Failed to generate report', error)
    errorMessage.value = '研究报告生成失败，请检查后端服务后重试。'
  } finally {
    isLoading.value = false
  }
}

function handleReset() {
  form.question = ''
  form.category = ''
  brief.value = null
  reportMarkdown.value = ''
  errorMessage.value = ''
}
</script>

<style scoped>
.report-page {
  min-height: 100vh;
  overflow-y: auto;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.08), transparent 34%),
    linear-gradient(180deg, #071014 0%, #080b0d 38%, #050607 100%);
}

.report-header {
  width: min(100%, 1360px);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
}

.report-brand,
.report-nav,
.button-row,
.panel-heading,
.evidence-meta,
.empty-tags {
  display: flex;
  align-items: center;
}

.report-brand {
  gap: 16px;
}

.report-mark {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  display: block;
  overflow: hidden;
  border-radius: var(--radius-md);
  border: 1px solid rgba(229, 238, 245, 0.42);
  background: #ffffff;
  box-shadow: 0 14px 28px rgba(31, 91, 98, 0.22);
}

.report-mark img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(1.78);
}

.report-brand h1 {
  margin-top: 6px;
  color: var(--text-primary);
  font-size: 34px;
  font-weight: 510;
  line-height: 1.14;
  letter-spacing: 0;
}

.report-brand p {
  margin-top: 8px;
  color: var(--text-tertiary);
  font-size: 14px;
  line-height: 1.65;
}

.report-nav {
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.report-nav a {
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

.report-nav a:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.05);
}

.report-main {
  width: min(100%, 1360px);
  margin: 18px auto 0;
  display: grid;
  grid-template-columns: minmax(320px, 380px) minmax(0, 1fr);
  gap: 18px;
}

.report-console,
.markdown-panel,
.brief-card,
.empty-panel,
.metric-card {
  background:
    linear-gradient(180deg, rgba(229, 238, 245, 0.035), rgba(229, 238, 245, 0.012)),
    rgba(11, 14, 16, 0.9);
}

.report-console {
  padding: 24px;
  align-self: start;
}

.console-heading,
.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.console-heading h2,
.panel-heading h2,
.brief-card h2,
.empty-panel h2 {
  margin-top: 10px;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 510;
  line-height: 1.18;
  letter-spacing: 0;
}

.status-chip,
.count-chip,
.empty-tags span {
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid rgba(126, 209, 216, 0.24);
  background: rgba(88, 166, 173, 0.12);
  color: var(--accent-hover);
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  font-size: 12px;
}

.status-chip.loading {
  border-color: rgba(198, 161, 91, 0.24);
  background: rgba(198, 161, 91, 0.12);
  color: #f6d9a7;
}

.report-form {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field span {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.field textarea,
.field select {
  width: 100%;
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.025), rgba(255, 255, 255, 0.015)),
    rgba(8, 10, 12, 0.94);
  color: var(--text-primary);
  padding: 15px 16px;
  resize: vertical;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field textarea:focus,
.field select:focus {
  border-color: rgba(126, 209, 216, 0.42);
  box-shadow: 0 0 0 3px rgba(88, 166, 173, 0.12);
}

.button-row {
  gap: 10px;
  flex-wrap: wrap;
}

.submit-button {
  min-width: 144px;
}

.submit-button:disabled,
.ghost-button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
  transform: none;
}

.ghost-button {
  background: rgba(255, 255, 255, 0.03);
}

.error-message {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: var(--radius-panel);
  border: 1px solid rgba(217, 103, 103, 0.24);
  background: rgba(217, 103, 103, 0.08);
  color: #f6c4c4;
  line-height: 1.6;
}

.suggestion-stack {
  margin-top: 24px;
  display: grid;
  gap: 10px;
}

.suggestion-button {
  padding: 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  color: var(--text-secondary);
  text-align: left;
  line-height: 1.55;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.suggestion-button:hover {
  transform: translateY(-1px);
  border-color: rgba(198, 161, 91, 0.32);
  background: rgba(198, 161, 91, 0.08);
}

.report-workspace {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  padding: 18px 20px;
}

.metric-card span {
  color: var(--text-quaternary);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.metric-card strong {
  display: block;
  margin-top: 12px;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 510;
  line-height: 1.35;
}

.result-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(320px, 0.82fr);
  gap: 18px;
}

.markdown-panel,
.brief-card,
.empty-panel {
  padding: 24px;
}

.generated-time {
  color: var(--text-quaternary);
  font-size: 12px;
  white-space: nowrap;
}

.markdown-body {
  margin-top: 22px;
  color: var(--text-tertiary);
  font-size: 15px;
  line-height: 1.75;
  word-break: break-word;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin-top: 1.2em;
  margin-bottom: 0.65em;
  color: var(--text-primary);
  line-height: 1.22;
  letter-spacing: 0;
}

.markdown-body :deep(p),
.markdown-body :deep(ul),
.markdown-body :deep(ol),
.markdown-body :deep(pre),
.markdown-body :deep(blockquote) {
  margin: 0.8em 0;
}

.markdown-body :deep(p:first-child),
.markdown-body :deep(h1:first-child),
.markdown-body :deep(h2:first-child) {
  margin-top: 0;
}

.markdown-body :deep(strong) {
  color: var(--text-primary);
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 1.3em;
}

.markdown-body :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.92em;
}

.markdown-body :deep(p code),
.markdown-body :deep(li code) {
  padding: 0.16em 0.42em;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.06);
  color: #f2f5ff;
}

.markdown-body :deep(pre) {
  overflow-x: auto;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(0, 0, 0, 0.34);
  border: 1px solid var(--border-default);
}

.markdown-body :deep(blockquote) {
  padding-left: 14px;
  border-left: 2px solid rgba(198, 161, 91, 0.5);
  color: var(--text-tertiary);
}

.brief-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.brief-block {
  margin-top: 20px;
}

.brief-block span {
  display: inline-flex;
  min-height: 26px;
  padding: 0 10px;
  align-items: center;
  border-radius: 999px;
  border: 1px solid rgba(126, 209, 216, 0.22);
  background: rgba(88, 166, 173, 0.1);
  color: var(--accent-hover);
  font-size: 12px;
}

.brief-block p,
.brief-block ul,
.empty-text,
.empty-panel p {
  margin-top: 12px;
  color: var(--text-tertiary);
  line-height: 1.72;
}

.brief-block ul {
  padding-left: 1.2em;
}

.evidence-list {
  margin-top: 18px;
  display: grid;
  gap: 12px;
}

.evidence-card {
  padding: 16px;
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), rgba(255, 255, 255, 0.015)),
    rgba(10, 12, 14, 0.92);
}

.web-card {
  background:
    linear-gradient(135deg, rgba(198, 161, 91, 0.08), transparent 44%),
    rgba(10, 12, 14, 0.92);
}

.evidence-meta {
  justify-content: space-between;
  gap: 10px;
  color: var(--text-quaternary);
  font-size: 12px;
}

.evidence-card h3 {
  margin-top: 12px;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 510;
  line-height: 1.3;
}

.evidence-card p {
  margin-top: 10px;
  color: var(--text-tertiary);
  line-height: 1.7;
}

.evidence-card small,
.evidence-card a {
  display: inline-block;
  margin-top: 12px;
  color: var(--text-quaternary);
  line-height: 1.55;
  word-break: break-all;
}

.evidence-card a {
  color: var(--accent-hover);
}

.empty-panel {
  min-height: 520px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.empty-panel h2 {
  font-size: 30px;
}

.empty-panel p {
  max-width: 720px;
  font-size: 16px;
}

.empty-tags {
  margin-top: 24px;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 1200px) {
  .report-main,
  .result-layout {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .report-page {
    padding: 16px;
  }

  .report-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .report-nav {
    justify-content: flex-start;
  }

  .report-brand {
    align-items: flex-start;
  }

  .report-brand h1 {
    font-size: 28px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .report-console,
  .markdown-panel,
  .brief-card,
  .empty-panel {
    padding: 18px;
  }

  .panel-heading {
    flex-direction: column;
  }
}
</style>
