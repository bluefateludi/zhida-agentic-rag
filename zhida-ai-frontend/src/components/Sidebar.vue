<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="sidebar-header">
      <span class="brand-mark">
        <img src="https://econgencode-1379443208.cos.ap-shanghai.myqcloud.com/zhidalogo.png" alt="智答 AI logo" />
      </span>
      <div class="brand-copy">
        <span class="section-label">RAG Console</span>
        <h2>智答 AI</h2>
      </div>
      <button class="collapse-icon" @click="$emit('toggle-collapse')" aria-label="Collapse sidebar">
        <span></span>
        <span></span>
      </button>
    </div>

    <button class="new-chat-btn" @click="$emit('new-chat')">
      <span class="new-chat-icon">+</span>
      <span class="new-chat-text">新建对话</span>
    </button>

    <div class="sidebar-section">
      <span class="section-label">会话列表</span>
    </div>

    <div class="session-list">
      <button
        v-for="session in sessions"
        :key="session.id"
        class="session-item"
        :class="{ active: session.id === activeSessionId }"
        @click="$emit('select-session', session.id)"
      >
        <span class="session-icon"></span>
        <span class="session-title">{{ session.title }}</span>
        <span class="session-delete" @click.stop="$emit('delete-session', session.id)">删除</span>
      </button>

      <div v-if="sessions.length === 0" class="empty-sessions">
        <span class="empty-title">还没有会话</span>
        <span class="empty-text">创建新对话后，这里会展示你的答辩演示流程。</span>
      </div>
    </div>

    <div class="sidebar-footer">
      <router-link to="/" class="footer-link">
        <span class="footer-dot"></span>
        <span class="footer-text">产品首页</span>
      </router-link>
      <router-link to="/pm" class="footer-link">
        <span class="footer-dot"></span>
        <span class="footer-text">产品分析模式</span>
      </router-link>
      <router-link to="/chat/documents" class="footer-link">
        <span class="footer-dot"></span>
        <span class="footer-text">知识库管理</span>
      </router-link>
      <button class="footer-link" @click="$emit('toggle-collapse')">
        <span class="footer-dot"></span>
        <span class="footer-text">{{ collapsed ? '展开侧栏' : '收起侧栏' }}</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
defineProps({
  sessions: { type: Array, default: () => [] },
  activeSessionId: { type: String, default: '' },
  collapsed: { type: Boolean, default: false }
})

defineEmits(['new-chat', 'select-session', 'delete-session', 'toggle-collapse'])
</script>

<style scoped>
.sidebar {
  position: relative;
  z-index: 10;
  width: 312px;
  min-width: 312px;
  height: 100%;
  padding: 24px 18px 20px;
  background: linear-gradient(180deg, rgba(12, 17, 20, 0.96), rgba(8, 11, 13, 0.94));
  border-right: 1px solid var(--border-subtle);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  gap: 18px;
  transition: width 0.24s ease, min-width 0.24s ease, padding 0.24s ease;
}

.sidebar.collapsed {
  width: 96px;
  min-width: 96px;
  padding-inline: 14px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-md);
  background: #ffffff;
  display: block;
  overflow: hidden;
  box-shadow:
    inset 0 0 0 1px rgba(229, 238, 245, 0.42),
    0 10px 24px rgba(31, 91, 98, 0.22);
  flex-shrink: 0;
}

.brand-mark img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(1.78);
}

.brand-copy {
  flex: 1;
  min-width: 0;
}

.brand-copy h2 {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 590;
  letter-spacing: 0;
}

.collapse-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-default);
  background: rgba(255, 255, 255, 0.02);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex-direction: column;
  flex-shrink: 0;
}

.collapse-icon span {
  width: 12px;
  height: 1.5px;
  background: var(--text-secondary);
  border-radius: 999px;
}

.new-chat-btn {
  width: 100%;
  min-height: 48px;
  padding: 0 16px;
  border-radius: var(--radius-md);
  background:
    linear-gradient(180deg, rgba(88, 166, 173, 0.2), rgba(47, 125, 134, 0.24)),
    rgba(229, 238, 245, 0.02);
  border: 1px solid rgba(126, 209, 216, 0.3);
  color: var(--text-primary);
  display: inline-flex;
  align-items: center;
  gap: 12px;
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.new-chat-btn:hover {
  transform: translateY(-1px);
  border-color: rgba(126, 209, 216, 0.5);
  background:
    linear-gradient(180deg, rgba(126, 209, 216, 0.26), rgba(47, 125, 134, 0.28)),
    rgba(229, 238, 245, 0.03);
}

.new-chat-icon {
  width: 18px;
  display: inline-flex;
  justify-content: center;
  font-size: 20px;
  line-height: 1;
}

.new-chat-text {
  font-weight: 510;
  white-space: nowrap;
}

.sidebar-section {
  padding: 0 4px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  width: 100%;
  min-height: 54px;
  padding: 12px 12px 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.session-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--border-default);
}

.session-item.active {
  border-color: rgba(126, 209, 216, 0.32);
  background:
    linear-gradient(180deg, rgba(88, 166, 173, 0.12), rgba(88, 166, 173, 0.08)),
    rgba(229, 238, 245, 0.03);
}

.session-icon {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.4));
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.03);
  flex-shrink: 0;
}

.session-title {
  flex: 1;
  min-width: 0;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item.active .session-title {
  color: var(--text-primary);
}

.session-delete {
  opacity: 0;
  color: var(--text-quaternary);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0;
  text-transform: uppercase;
  transition: opacity 0.2s ease, color 0.2s ease;
}

.session-item:hover .session-delete {
  opacity: 1;
}

.session-delete:hover {
  color: #ff8a8a;
}

.empty-sessions {
  padding: 18px 16px;
  border-radius: var(--radius-panel);
  border: 1px dashed var(--border-default);
  background: rgba(255, 255, 255, 0.015);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.empty-title {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 510;
}

.empty-text {
  color: var(--text-quaternary);
  font-size: 13px;
  line-height: 1.5;
}

.sidebar-footer {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-subtle);
}

.footer-link {
  min-height: 42px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid transparent;
  color: var(--text-secondary);
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.footer-link:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--border-default);
  color: var(--text-primary);
}

.footer-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.4);
  flex-shrink: 0;
}

.footer-text {
  white-space: nowrap;
}

.sidebar.collapsed .brand-copy,
.sidebar.collapsed .section-label,
.sidebar.collapsed .new-chat-text,
.sidebar.collapsed .session-title,
.sidebar.collapsed .session-delete,
.sidebar.collapsed .footer-text,
.sidebar.collapsed .empty-sessions {
  display: none;
}

.sidebar.collapsed .sidebar-header,
.sidebar.collapsed .sidebar-footer {
  align-items: center;
}

.sidebar.collapsed .new-chat-btn,
.sidebar.collapsed .footer-link,
.sidebar.collapsed .session-item {
  justify-content: center;
  padding-inline: 0;
}

.sidebar.collapsed .collapse-icon {
  display: none;
}

.sidebar.collapsed .sidebar-section {
  display: none;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    inset: 0 auto 0 0;
    width: min(88vw, 320px);
    min-width: min(88vw, 320px);
    box-shadow: var(--shadow-panel);
  }

  .sidebar.collapsed {
    transform: translateX(-100%);
    width: min(88vw, 320px);
    min-width: min(88vw, 320px);
  }

  .sidebar.collapsed .brand-copy,
  .sidebar.collapsed .section-label,
  .sidebar.collapsed .new-chat-text,
  .sidebar.collapsed .session-title,
  .sidebar.collapsed .footer-text,
  .sidebar.collapsed .empty-sessions {
    display: initial;
  }

  .sidebar.collapsed .session-delete {
    display: none;
  }

  .sidebar.collapsed .new-chat-btn,
  .sidebar.collapsed .footer-link,
  .sidebar.collapsed .session-item {
    justify-content: flex-start;
    padding-inline: 14px;
  }
}
</style>
