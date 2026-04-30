<template>
  <div class="ai-avatar-fallback" :class="type">
    <span v-if="type === 'love'">❤️</span>
    <span v-else>🤖</span>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.ai-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  border-radius: 50%;
}

.love {
  background: linear-gradient(135deg, rgba(216, 164, 73, 0.9), rgba(198, 161, 91, 0.72));
}

.default, .super {
  background: linear-gradient(135deg, rgba(126, 209, 216, 0.9), rgba(47, 125, 134, 0.78));
}
</style> 
