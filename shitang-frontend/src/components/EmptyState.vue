<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon><component :is="iconComp" /></el-icon>
    </div>
    <div class="empty-text">{{ text }}</div>
    <div v-if="$slots.action" class="empty-action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Box, Search, Star, Tickets, ChatDotRound, DataAnalysis, Bowl } from '@element-plus/icons-vue'

const props = defineProps({
  text: { type: String, default: '暂无数据' },
  icon: { type: String, default: 'Box' }
})

const iconMap = { Box, Search, Star, Tickets, ChatDotRound, DataAnalysis, Bowl }
const iconComp = computed(() => iconMap[props.icon] || Box)
</script>
