<template>
  <div class="evaluation-page">
    <header class="evaluation-header">
      <div class="evaluation-title">
        <span class="section-label">Golden-Set Evaluation</span>
        <h1>RAG 评测仪表盘</h1>
        <p>用固定问题集检查回答关键词命中和来源返回情况，适合答辩时展示系统可评估性。</p>
      </div>

      <nav class="evaluation-nav">
        <router-link to="/">首页</router-link>
        <router-link to="/chat">知识库工作台</router-link>
        <router-link to="/report">研究报告模式</router-link>
        <router-link to="/traces">Trace Dashboard</router-link>
      </nav>
    </header>

    <main class="evaluation-main">
      <section class="summary-grid">
        <article class="summary-card surface-panel">
          <span>用例总数</span>
          <strong>{{ caseCount }}</strong>
        </article>
        <article class="summary-card surface-panel">
          <span>已运行</span>
          <strong>{{ results.length }}</strong>
        </article>
        <article class="summary-card surface-panel">
          <span>通过数量</span>
          <strong>{{ passCount }}</strong>
        </article>
        <article class="summary-card surface-panel">
          <span>失败数量</span>
          <strong>{{ failedResults.length }}</strong>
        </article>
      </section>

      <section class="evaluation-toolbar surface-panel">
        <div>
          <span class="section-label">Run Control</span>
          <h2>选择用例并运行评测</h2>
        </div>
        <div class="toolbar-actions">
          <button class="control-button" type="button" @click="selectAllCases">全选</button>
          <button class="control-button" type="button" @click="selectedCaseIds = []">清空</button>
          <button class="control-button run-button" type="button" :disabled="isRunning || caseCount === 0" @click="handleRun">
            {{ isRunning ? '评测中...' : '运行评测' }}
          </button>
        </div>
      </section>

      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <section class="dashboard-grid">
        <article class="case-panel surface-panel">
          <div class="panel-heading">
            <span class="section-label">Eval Cases</span>
            <span>{{ selectedCaseIds.length }}/{{ caseCount }}</span>
          </div>

          <div v-if="cases.length" class="case-list">
            <label v-for="item in cases" :key="item.id" class="case-row">
              <input v-model="selectedCaseIds" type="checkbox" :value="item.id" />
              <div>
                <strong>{{ item.question }}</strong>
                <div class="case-meta">
                  <span>{{ item.id }}</span>
                  <span>{{ item.category || 'all' }}</span>
                  <span>{{ item.requireSources ? '需要来源' : '不强制来源' }}</span>
                </div>
                <div class="keyword-row">
                  <span v-for="keyword in item.expectedKeywords" :key="keyword">{{ keyword }}</span>
                </div>
              </div>
            </label>
          </div>
          <p v-else class="empty-text">暂无评测用例。</p>
        </article>

        <article class="result-panel surface-panel">
          <div class="panel-heading">
            <span class="section-label">Run Results</span>
            <span>{{ passRate }}% pass</span>
          </div>

          <div v-if="results.length" class="result-table">
            <article v-for="result in results" :key="result.caseId" class="result-row" :class="{ failed: !result.passed }">
              <div class="result-status">
                <span>{{ result.passed ? 'PASS' : 'FAIL' }}</span>
                <strong>{{ result.caseId }}</strong>
              </div>
              <div class="result-body">
                <h3>{{ result.question }}</h3>
                <p>{{ result.actualAnswer }}</p>
                <div class="result-meta">
                  <span>命中 {{ result.matchedKeywords.length }}/{{ result.expectedKeywords.length }}</span>
                  <span>来源 {{ result.sourceCount }}</span>
                </div>
                <div class="keyword-row">
                  <span v-for="keyword in result.matchedKeywords" :key="keyword">{{ keyword }}</span>
                </div>
              </div>
            </article>
          </div>
          <div v-else class="empty-result">
            <span class="section-label">Waiting</span>
            <h2>运行后展示评测结果。</h2>
            <p>这里会呈现每个用例的通过状态、actualAnswer 预览、关键词命中情况和 sourceCount。</p>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { listEvalCases, runRagEvaluation } from '../api'

const cases = ref([])
const selectedCaseIds = ref([])
const results = ref([])
const isRunning = ref(false)
const errorMessage = ref('')

const caseCount = computed(() => cases.value.length)
const passCount = computed(() => results.value.filter(item => item.passed).length)
const failedResults = computed(() => results.value.filter(item => !item.passed))
const passRate = computed(() => {
  if (results.value.length === 0) return 0
  return Math.round((passCount.value / results.value.length) * 100)
})

onMounted(loadCases)

async function loadCases() {
  try {
    const res = await listEvalCases()
    cases.value = res.data || []
    selectedCaseIds.value = cases.value.map(item => item.id)
  } catch (error) {
    console.error('Failed to load eval cases', error)
    errorMessage.value = '评测用例加载失败，请检查后端服务。'
  }
}

function selectAllCases() {
  selectedCaseIds.value = cases.value.map(item => item.id)
}

async function handleRun() {
  isRunning.value = true
  errorMessage.value = ''

  try {
    const res = await runRagEvaluation(selectedCaseIds.value)
    results.value = res.data || []
  } catch (error) {
    console.error('Failed to run evaluation', error)
    errorMessage.value = 'RAG 评测运行失败，请稍后重试。'
  } finally {
    isRunning.value = false
  }
}
</script>

<style scoped>
.evaluation-page {
  height: 100vh;
  overflow-y: auto;
  padding: 24px 24px 40px;
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.08), transparent 34%),
    linear-gradient(180deg, #071014 0%, #080b0d 38%, #050607 100%);
  -webkit-overflow-scrolling: touch;
}

.evaluation-header,
.evaluation-main {
  width: min(100%, 1320px);
  margin: 0 auto;
}

.evaluation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.evaluation-title h1 {
  margin-top: 8px;
  color: var(--text-primary);
  font-size: 34px;
  font-weight: 510;
  line-height: 1.14;
  letter-spacing: 0;
}

.evaluation-title p {
  margin-top: 10px;
  max-width: 720px;
  color: var(--text-tertiary);
  line-height: 1.65;
}

.evaluation-nav,
.toolbar-actions,
.summary-grid,
.dashboard-grid,
.panel-heading,
.case-meta,
.keyword-row,
.result-meta {
  display: flex;
  align-items: center;
}

.evaluation-nav,
.toolbar-actions,
.case-meta,
.keyword-row,
.result-meta {
  gap: 10px;
  flex-wrap: wrap;
}

.evaluation-nav {
  justify-content: flex-end;
}

.evaluation-nav a {
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

.evaluation-nav a:hover {
  transform: translateY(-1px);
  border-color: var(--border-strong);
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.05);
}

.evaluation-main {
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
.evaluation-toolbar,
.case-panel,
.result-panel {
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

.evaluation-toolbar {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.evaluation-toolbar h2 {
  margin-top: 8px;
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 510;
}

.run-button {
  min-width: 112px;
  border-color: rgba(126, 209, 216, 0.32);
  background: rgba(88, 166, 173, 0.14);
  color: var(--text-primary);
}

.run-button:disabled {
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
}

.dashboard-grid {
  align-items: flex-start;
  display: grid;
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
}

.case-panel,
.result-panel {
  padding: 20px;
}

.panel-heading {
  justify-content: space-between;
  gap: 12px;
  color: var(--text-tertiary);
}

.case-list,
.result-table {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.case-row,
.result-row {
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
}

.case-row {
  padding: 14px;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 12px;
  cursor: pointer;
}

.case-row input {
  margin-top: 3px;
  accent-color: var(--accent);
}

.case-row strong,
.result-body h3 {
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 510;
  line-height: 1.45;
}

.case-meta,
.result-meta {
  margin-top: 8px;
}

.case-meta span,
.result-meta span,
.keyword-row span {
  min-height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-quaternary);
  display: inline-flex;
  align-items: center;
  font-size: 11px;
}

.keyword-row {
  margin-top: 10px;
}

.keyword-row span {
  color: var(--accent-hover);
  border-color: rgba(126, 209, 216, 0.18);
  background: rgba(88, 166, 173, 0.08);
}

.result-row {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
}

.result-row.failed {
  border-color: rgba(217, 103, 103, 0.22);
  background: rgba(217, 103, 103, 0.045);
}

.result-status span {
  min-height: 26px;
  padding: 0 9px;
  border-radius: 999px;
  background: rgba(79, 180, 119, 0.12);
  color: #89e2ac;
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
}

.failed .result-status span {
  background: rgba(217, 103, 103, 0.12);
  color: #f2b1b1;
}

.result-status strong {
  display: block;
  margin-top: 8px;
  color: var(--text-quaternary);
  font-size: 12px;
  line-height: 1.4;
}

.result-body p,
.empty-text,
.empty-result p {
  margin-top: 8px;
  color: var(--text-tertiary);
  line-height: 1.7;
}

.empty-result {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.empty-result h2 {
  margin-top: 10px;
  color: var(--text-primary);
  font-size: 26px;
  font-weight: 510;
}

@media (max-width: 1100px) {
  .evaluation-header,
  .evaluation-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .evaluation-nav {
    justify-content: flex-start;
  }

  .summary-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .evaluation-page {
    padding: 16px 16px 28px;
  }

  .evaluation-title h1 {
    font-size: 28px;
  }

  .result-row {
    grid-template-columns: 1fr;
  }
}
</style>
