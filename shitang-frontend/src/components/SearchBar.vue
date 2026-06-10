<template>
  <div class="search-bar">
    <el-input
      v-model="model"
      :placeholder="placeholder"
      :prefix-icon="Search"
      clearable
      size="large"
      @keyup.enter="onSearch"
    >
      <template #append>
        <el-button :icon="Search" @click="onSearch">搜索</el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '搜索菜品...' }
})
const emit = defineEmits(['update:modelValue', 'search'])
const model = ref(props.modelValue)
watch(() => props.modelValue, v => model.value = v)
watch(model, v => emit('update:modelValue', v))

function onSearch() { emit('search', model.value) }
</script>

<style scoped>
.search-bar { margin-bottom: 16px; }
</style>
