<template>
  <div
    class="upload-zone"
    :class="{ dragging: isDragging }"
    @dragover.prevent="isDragging = true"
    @dragleave="isDragging = false"
    @drop.prevent="handleDrop"
    @click="triggerFileInput"
  >
    <input
      ref="fileInputRef"
      type="file"
      accept=".pdf,.docx,.txt,.md"
      multiple
      class="hidden-input"
      @change="handleFileSelect"
    />

    <div class="upload-grid">
      <div class="upload-mark">
        <span></span>
      </div>

      <div class="upload-copy">
        <span class="section-label">Document Intake</span>
        <h3>拖拽文件到这里，或点击加入知识源材料。</h3>
        <p>
          支持 PDF、DOCX、TXT 和 Markdown。在不改变原有流程的前提下，提供更克制、更专业的上传体验。
        </p>
      </div>

      <div class="upload-specs">
        <div class="spec-item">
          <span class="spec-label">支持格式</span>
          <strong>PDF / DOCX / TXT / MD</strong>
        </div>
        <div class="spec-item">
          <span class="spec-label">文件限制</span>
          <strong>单文件 50 MB</strong>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['files-selected'])
const isDragging = ref(false)
const fileInputRef = ref(null)

function triggerFileInput() {
  fileInputRef.value.click()
}

function handleFileSelect(event) {
  const files = Array.from(event.target.files)
  if (files.length) emit('files-selected', files)
  event.target.value = ''
}

function handleDrop(event) {
  isDragging.value = false
  const files = Array.from(event.dataTransfer.files)
  if (files.length) emit('files-selected', files)
}
</script>

<style scoped>
.upload-zone {
  border-radius: var(--radius-panel);
  border: 1px dashed var(--border-default);
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.08), transparent 36%),
    linear-gradient(180deg, rgba(229, 238, 245, 0.04), rgba(229, 238, 245, 0.015));
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.upload-zone:hover,
.upload-zone.dragging {
  border-color: rgba(126, 209, 216, 0.4);
  background:
    linear-gradient(135deg, rgba(88, 166, 173, 0.12), transparent 38%),
    linear-gradient(180deg, rgba(229, 238, 245, 0.055), rgba(229, 238, 245, 0.02));
  transform: translateY(-1px);
}

.hidden-input {
  display: none;
}

.upload-grid {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 24px;
  align-items: center;
  padding: 28px;
}

.upload-mark {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-panel);
  border: 1px solid rgba(126, 209, 216, 0.28);
  background:
    linear-gradient(145deg, rgba(126, 209, 216, 0.82), rgba(47, 125, 134, 0.66));
  display: grid;
  place-items: center;
  box-shadow: 0 16px 34px rgba(31, 91, 98, 0.2);
}

.upload-mark span {
  width: 22px;
  height: 22px;
  border-radius: 8px;
  border: 2px solid rgba(255, 255, 255, 0.86);
  position: relative;
}

.upload-mark span::before {
  content: '';
  position: absolute;
  left: 50%;
  top: -8px;
  width: 2px;
  height: 14px;
  background: rgba(255, 255, 255, 0.86);
  transform: translateX(-50%);
}

.upload-mark span::after {
  content: '';
  position: absolute;
  left: 50%;
  top: -9px;
  width: 10px;
  height: 10px;
  border-top: 2px solid rgba(255, 255, 255, 0.86);
  border-right: 2px solid rgba(255, 255, 255, 0.86);
  transform: translateX(-50%) rotate(-45deg);
}

.upload-copy h3 {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 510;
  letter-spacing: 0;
}

.upload-copy p {
  margin-top: 12px;
  color: var(--text-tertiary);
  line-height: 1.7;
}

.upload-specs {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 190px;
}

.spec-item {
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.025);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.spec-label {
  color: var(--text-quaternary);
  font-size: 12px;
}

.spec-item strong {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 510;
}

@media (max-width: 960px) {
  .upload-grid {
    grid-template-columns: 1fr;
  }

  .upload-specs {
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .upload-grid {
    padding: 22px;
    gap: 18px;
  }

  .upload-copy h3 {
    font-size: 20px;
  }
}
</style>
