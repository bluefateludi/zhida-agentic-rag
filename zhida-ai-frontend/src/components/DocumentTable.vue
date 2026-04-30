<template>
  <div class="document-table-wrapper">
    <table class="document-table">
      <thead>
        <tr>
          <th>文档名称</th>
          <th>类型</th>
          <th>分类</th>
          <th>分块数</th>
          <th>状态</th>
          <th>上传时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="doc in documents" :key="doc.id">
          <td class="doc-title-cell">
            <span class="doc-title">{{ doc.title }}</span>
          </td>
          <td><span class="file-type-badge">{{ doc.fileType || '-' }}</span></td>
          <td class="muted-cell">{{ doc.category || '-' }}</td>
          <td class="strong-cell">{{ doc.chunkCount ?? '-' }}</td>
          <td>
            <span class="status-badge" :class="doc.status?.toLowerCase()">
              {{ statusText(doc.status) }}
            </span>
          </td>
          <td class="muted-cell">{{ formatTime(doc.createdAt) }}</td>
          <td>
            <button class="delete-btn" @click="$emit('delete', doc.id)">删除</button>
          </td>
        </tr>

        <tr v-if="documents.length === 0">
          <td colspan="7" class="empty-cell">
            <span class="empty-title">当前没有文档</span>
            <span class="empty-text">上传文件后，这里会显示知识库中的全部资料。</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
defineProps({
  documents: { type: Array, default: () => [] }
})

defineEmits(['delete'])

function statusText(status) {
  const map = { PROCESSING: '处理中', READY: '就绪', ERROR: '异常' }
  return map[status] || status || '未知'
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.document-table-wrapper {
  overflow-x: auto;
}

.document-table {
  width: 100%;
  min-width: 860px;
  border-collapse: collapse;
}

.document-table th {
  padding: 14px 16px;
  text-align: left;
  color: var(--text-quaternary);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-default);
}

.document-table td {
  padding: 18px 16px;
  border-bottom: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-size: 14px;
  vertical-align: middle;
}

.document-table tbody tr {
  transition: background 0.2s ease;
}

.document-table tbody tr:hover {
  background: rgba(255, 255, 255, 0.02);
}

.doc-title-cell {
  max-width: 340px;
}

.doc-title {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 510;
}

.file-type-badge,
.status-badge {
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.file-type-badge {
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
}

.status-badge.processing {
  background: rgba(245, 158, 11, 0.14);
  color: #ffcc7a;
}

.status-badge.ready {
  background: rgba(16, 185, 129, 0.14);
  color: #7ce3bd;
}

.status-badge.error {
  background: rgba(239, 68, 68, 0.14);
  color: #ff9a9a;
}

.muted-cell {
  color: var(--text-tertiary);
}

.strong-cell {
  color: var(--text-primary);
  font-weight: 510;
}

.delete-btn {
  min-height: 32px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(239, 68, 68, 0.18);
  background: rgba(239, 68, 68, 0.06);
  color: #ffadad;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.28);
}

.empty-cell {
  padding: 56px 16px;
  text-align: center;
}

.empty-title,
.empty-text {
  display: block;
}

.empty-title {
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 510;
}

.empty-text {
  margin-top: 8px;
  color: var(--text-quaternary);
  font-size: 13px;
}
</style>
