<template>
  <div class="doc-manager">
    <header class="doc-header">
      <div class="header-copy">
        <router-link to="/chat" class="back-btn">返回工作台</router-link>
        <span class="section-label">Knowledge Base Console</span>
        <h1>管理答辩演示所依赖的知识库文档。</h1>
        <p>
          保持上传、筛选和删除功能不变，只对展示层进行升级，让知识库管理页成为 `/chat` 工作台的一部分，更适合软件工程课设答辩。
        </p>
      </div>

      <div class="header-stats">
        <div class="stat-card">
          <span>文档总数</span>
          <strong>{{ documents.length }}</strong>
        </div>
        <div class="stat-card">
          <span>已就绪</span>
          <strong>{{ readyCount }}</strong>
        </div>
        <div class="stat-card">
          <span>分类数</span>
          <strong>{{ categories.length }}</strong>
        </div>
      </div>
    </header>

    <main class="doc-content">
      <section class="hero-grid">
        <div class="hero-panel surface-panel">
          <UploadZone @files-selected="handleUpload" />
          <div v-if="uploading" class="upload-status">正在上传并建立索引...</div>
        </div>

        <div class="overview-panel surface-panel">
          <div class="overview-block">
            <span class="section-label">Overview</span>
            <h2>结构化检索，从清晰可管理的知识资产开始。</h2>
            <p>
              通过分类筛选快速聚焦当前文档集合，同时保持原有后端行为和上传流程完全不变。
            </p>
          </div>

          <div class="filter-card">
            <label for="category-select">分类筛选</label>
            <select id="category-select" v-model="filterCategory" @change="loadDocuments">
              <option value="">全部分类</option>
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>

          <div class="mini-stats">
            <div class="mini-stat">
              <span>处理中</span>
              <strong>{{ processingCount }}</strong>
            </div>
            <div class="mini-stat">
              <span>异常</span>
              <strong>{{ errorCount }}</strong>
            </div>
          </div>
        </div>
      </section>

      <section class="insight-grid">
        <article class="insight-card surface-panel">
          <span class="section-label">Index Readiness</span>
          <div class="readiness-row">
            <div class="readiness-value">{{ readinessRate }}%</div>
            <div class="readiness-track">
              <span class="readiness-fill" :style="{ width: `${readinessRate}%` }"></span>
            </div>
          </div>
          <p>就绪率用于答辩现场快速说明知识库是否已经完成向量化与可检索准备。</p>
        </article>

        <article class="insight-card surface-panel">
          <span class="section-label">Retrieval Posture</span>
          <div class="insight-metric">{{ dominantStatusLabel }}</div>
          <p>根据当前文档状态自动概括索引态势，帮助你更自然地介绍系统运行阶段。</p>
        </article>

        <article class="insight-card surface-panel">
          <span class="section-label">Demo Narrative</span>
          <div class="insight-metric">上传 → 建索引 → 问答 → 引用</div>
          <p>这一页负责展示知识接入与管理，上一个页面负责展示智能问答和来源引用。</p>
        </article>
      </section>

      <section class="table-panel surface-panel">
        <div class="table-header">
          <div>
            <span class="section-label">Document Inventory</span>
            <h2>当前已接入的知识材料</h2>
          </div>
          <span class="table-hint">{{ filterCategory || '全部分类' }}</span>
        </div>

        <DocumentTable :documents="documents" @delete="handleDelete" />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import UploadZone from '../components/UploadZone.vue'
import DocumentTable from '../components/DocumentTable.vue'
import { uploadDocument, listDocuments, deleteDocument } from '../api'

const documents = ref([])
const filterCategory = ref('')
const uploading = ref(false)

const categories = computed(() => {
  const cats = new Set(documents.value.map(d => d.category).filter(Boolean))
  return [...cats]
})

const readyCount = computed(() => documents.value.filter(doc => doc.status === 'READY').length)
const processingCount = computed(() => documents.value.filter(doc => doc.status === 'PROCESSING').length)
const errorCount = computed(() => documents.value.filter(doc => doc.status === 'ERROR').length)
const readinessRate = computed(() => {
  if (!documents.value.length) return 0
  return Math.round((readyCount.value / documents.value.length) * 100)
})
const dominantStatusLabel = computed(() => {
  if (!documents.value.length) return '等待接入文档'
  if (processingCount.value > 0) return '知识索引构建中'
  if (errorCount.value > 0 && readyCount.value === 0) return '索引存在异常'
  if (readyCount.value === documents.value.length) return '知识库可直接用于演示'
  return '知识库部分可用'
})

onMounted(() => {
  loadDocuments()
})

async function loadDocuments() {
  try {
    const res = await listDocuments(filterCategory.value || undefined)
    documents.value = res.data || []
  } catch (e) {
    console.error('Failed to load documents', e)
  }
}

async function handleUpload(files) {
  uploading.value = true
  for (const file of files) {
    try {
      await uploadDocument(file)
    } catch (e) {
      console.error('Upload failed:', file.name, e)
      alert(`上传失败: ${file.name}`)
    }
  }
  uploading.value = false
  await loadDocuments()
}

async function handleDelete(id) {
  if (!confirm('确定删除该文档吗？删除后相关回答可能失去来源引用。')) return
  try {
    await deleteDocument(id)
    await loadDocuments()
  } catch (e) {
    console.error('Delete failed', e)
  }
}
</script>

<style scoped>
.doc-manager {
  height: 100vh;
  overflow-y: auto;
  padding: 32px 24px 40px;
}

.doc-header,
.doc-content {
  width: min(100%, var(--content-width));
  margin: 0 auto;
}

.doc-header {
  display: flex;
  justify-content: space-between;
  gap: 28px;
  padding-bottom: 28px;
}

.header-copy {
  max-width: 740px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  margin-bottom: 16px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.02);
  color: var(--text-secondary);
  transition: background 0.2s ease, border-color 0.2s ease;
}

.back-btn + .section-label {
  margin-left: 8px;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--border-strong);
}

.header-copy h1 {
  margin-top: 10px;
  font-size: 44px;
  font-weight: 510;
  line-height: 1.12;
  letter-spacing: 0;
}

.header-copy p {
  margin-top: 14px;
  color: var(--text-tertiary);
  font-size: 16px;
  line-height: 1.72;
}

.header-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 12px;
  align-self: flex-start;
}

.stat-card {
  padding: 16px 18px;
  border-radius: var(--radius-panel);
  background:
    linear-gradient(180deg, rgba(229, 238, 245, 0.035), rgba(229, 238, 245, 0.02)),
    rgba(15, 21, 24, 0.78);
  border: 1px solid var(--border-default);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-card span {
  color: var(--text-quaternary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0;
}

.stat-card strong {
  color: var(--text-primary);
  font-size: 30px;
  font-weight: 510;
  letter-spacing: 0;
}

.doc-content {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.9fr);
  gap: 22px;
}

.hero-panel,
.overview-panel,
.table-panel {
  border-radius: var(--radius-panel);
}

.hero-panel {
  padding: 22px;
}

.upload-status {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  background: rgba(88, 166, 173, 0.12);
  border: 1px solid rgba(126, 209, 216, 0.2);
  color: var(--accent-hover);
  font-size: 14px;
}

.overview-panel {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.overview-block h2 {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 510;
  letter-spacing: 0;
}

.overview-block p {
  margin-top: 12px;
  color: var(--text-tertiary);
  line-height: 1.72;
}

.filter-card {
  padding: 16px;
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-card label {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 510;
}

.filter-card select {
  min-height: 44px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-default);
  background: rgba(8, 9, 10, 0.86);
  color: var(--text-primary);
}

.mini-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mini-stat {
  padding: 16px;
  border-radius: var(--radius-panel);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.02);
}

.mini-stat span {
  color: var(--text-quaternary);
  font-size: 12px;
}

.mini-stat strong {
  display: block;
  margin-top: 8px;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 510;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
}

.insight-card {
  padding: 20px;
  border-radius: var(--radius-panel);
}

.readiness-row {
  margin-top: 12px;
}

.readiness-value,
.insight-metric {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 510;
  letter-spacing: 0;
  line-height: 1.12;
}

.readiness-track {
  margin-top: 14px;
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.06);
  overflow: hidden;
}

.readiness-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(88, 166, 173, 0.85), rgba(198, 161, 91, 0.95));
  box-shadow: 0 0 18px rgba(88, 166, 173, 0.22);
}

.insight-card p {
  margin-top: 12px;
  color: var(--text-tertiary);
  font-size: 14px;
  line-height: 1.7;
}

.table-panel {
  padding: 24px;
}

.table-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 14px;
}

.table-header h2 {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 510;
  letter-spacing: 0;
}

.table-hint {
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.02);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  font-size: 13px;
}

@media (max-width: 1080px) {
  .doc-header,
  .hero-grid,
  .insight-grid {
    grid-template-columns: 1fr;
  }

  .doc-header {
    flex-direction: column;
  }

  .header-stats {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .doc-manager {
    padding: 20px 16px 28px;
  }

  .back-btn + .section-label {
    display: block;
    margin-top: 10px;
    margin-left: 0;
  }

  .header-stats {
    grid-template-columns: 1fr;
  }

  .hero-panel,
  .overview-panel,
  .insight-card,
  .table-panel {
    border-radius: var(--radius-panel);
  }

  .header-copy h1 {
    font-size: 34px;
  }

  .hero-panel,
  .overview-panel,
  .insight-card,
  .table-panel {
    padding: 18px;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
