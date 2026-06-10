<template>
  <el-breadcrumb separator="/" class="bc">
    <el-breadcrumb-item v-for="(it, i) in items" :key="i" :to="it.to">
      {{ it.label }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
defineProps({
  items: { type: Array, required: true } // [{ label, to }]
})
</script>

<style scoped>
.bc { font-size: 13px; margin-bottom: 12px; }
.bc :deep(a) { color: var(--text-secondary); }
.bc :deep(a:hover) { color: var(--primary); }
</style>
