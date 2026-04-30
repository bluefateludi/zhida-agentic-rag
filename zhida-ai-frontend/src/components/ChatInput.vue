<template>
  <div class="chat-input-shell">
    <div class="chat-input">
      <div v-if="suggestions.length" class="suggestion-row">
        <button
          v-for="suggestion in suggestions"
          :key="suggestion"
          class="suggestion-chip"
          :disabled="disabled"
          @click="sendSuggestion(suggestion)"
        >
          {{ suggestion }}
        </button>
      </div>
      <div class="input-wrapper">
        <textarea
          ref="textareaRef"
          v-model="text"
          :placeholder="placeholder"
          rows="1"
          :disabled="disabled"
          @keydown.enter.exact.prevent="send"
          @input="autoResize"
        ></textarea>

        <div class="input-actions">
          <button class="send-btn" :disabled="disabled || !text.trim()" @click="send">
            {{ buttonLabel }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  disabled: { type: Boolean, default: false },
  placeholder: {
    type: String,
    default: '请输入关于需求、架构、实现细节或知识库内容的问题...'
  },
  buttonLabel: { type: String, default: '发送' },
  suggestions: { type: Array, default: () => [] }
})

const emit = defineEmits(['send'])
const text = ref('')
const textareaRef = ref(null)

function send() {
  const message = text.value.trim()
  if (!message || props.disabled) return
  emit('send', message)
  text.value = ''
  nextTick(() => autoResize())
}

function sendSuggestion(suggestion) {
  if (!suggestion || props.disabled) return
  emit('send', suggestion)
  text.value = ''
  nextTick(() => autoResize())
}

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 180) + 'px'
}
</script>

<style scoped>
.chat-input-shell {
  padding: 0 24px 24px;
  flex-shrink: 0;
}

.chat-input {
  width: min(100%, 920px);
  margin: 0 auto;
  padding: 14px;
  border-radius: var(--radius-panel);
  background:
    linear-gradient(180deg, rgba(229, 238, 245, 0.04), rgba(229, 238, 245, 0.015)),
    rgba(8, 12, 14, 0.86);
  border: 1px solid var(--border-default);
  box-shadow:
    inset 0 1px 0 rgba(229, 238, 245, 0.04),
    0 22px 50px rgba(0, 0, 0, 0.24);
  backdrop-filter: blur(18px);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.suggestion-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.suggestion-chip {
  min-height: 34px;
  padding: 0 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-default);
  background: var(--bg-soft);
  color: var(--text-secondary);
  font-size: 13px;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.suggestion-chip:hover:not(:disabled) {
  border-color: var(--border-strong);
  background: rgba(255, 255, 255, 0.055);
  color: var(--text-primary);
}

.suggestion-chip:disabled {
  opacity: 0.5;
  cursor: wait;
}

textarea {
  flex: 1;
  min-height: 96px;
  max-height: 220px;
  border: none;
  resize: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.7;
}

textarea::placeholder {
  color: var(--text-quaternary);
}

textarea:disabled {
  opacity: 0.7;
}

.input-actions {
  display: flex;
  align-items: flex-end;
  flex-shrink: 0;
}

.send-btn {
  min-width: 92px;
  height: 44px;
  padding: 0 18px;
  border-radius: var(--radius-sm);
  background:
    linear-gradient(180deg, rgba(88, 166, 173, 0.26), rgba(47, 125, 134, 0.24)),
    var(--accent-strong);
  color: #f7fbfb;
  font-size: 14px;
  font-weight: 600;
  box-shadow:
    inset 0 1px 0 rgba(229, 238, 245, 0.16),
    0 14px 26px rgba(31, 91, 98, 0.26);
  transition: transform 0.2s ease, filter 0.2s ease, opacity 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.05);
}

.send-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 768px) {
  .chat-input-shell {
    padding: 0 14px 14px;
  }

  .chat-input {
    padding: 14px;
    border-radius: var(--radius-panel);
  }

  .input-wrapper,
  .input-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
