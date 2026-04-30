<template>
  <div class="source-card">
    <div class="source-header">
      <div class="source-heading">
        <span class="source-kicker">Citation</span>
        <span class="source-title">{{ source.documentTitle || source.fileName || '未命名来源' }}</span>
      </div>
      <span v-if="source.score" class="source-score">{{ (source.score * 100).toFixed(0) }}%</span>
    </div>
    <div class="source-meta">
      <span v-if="source.rank">#{{ source.rank }}</span>
      <span v-if="source.category">{{ source.category }}</span>
      <span v-if="source.chunkIndex">Chunk {{ source.chunkIndex }}</span>
      <span v-if="source.fileName">{{ source.fileName }}</span>
    </div>
    <div class="source-content">{{ source.relevantContent }}</div>
  </div>
</template>

<script setup>
defineProps({
  source: { type: Object, required: true }
})
</script>

<style scoped>
.source-card {
  padding: 14px 16px;
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.03), rgba(255, 255, 255, 0.015)),
    rgba(10, 11, 13, 0.7);
  border: 1px solid var(--border-default);
}

.source-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.source-heading {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.source-kicker {
  color: var(--text-quaternary);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.source-title {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 510;
  line-height: 1.4;
}

.source-score {
  min-width: 52px;
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(16, 185, 129, 0.12);
  color: #7ce3bd;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.source-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.source-meta span {
  min-height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: var(--text-quaternary);
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  line-height: 1;
}

.source-content {
  margin-top: 12px;
  color: var(--text-tertiary);
  font-size: 13px;
  line-height: 1.75;
  white-space: pre-wrap;
}

</style>
