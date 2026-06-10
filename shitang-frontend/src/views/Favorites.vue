<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '我的收藏' }
    ]" />
    <h2 class="page-title"><el-icon><Star /></el-icon> 我的收藏</h2>
    <p class="page-subtitle">共 {{ list.length }} 道收藏的菜</p>

    <div v-loading="loading" class="dish-grid">
      <div v-for="d in list" :key="d.id" class="fav-wrap">
        <DishCard :dish="d" :show-feedback="false" />
        <el-button
          type="danger"
          plain
          :icon="Delete"
          class="cancel-btn"
          @click="onCancel(d)"
        >取消收藏</el-button>
      </div>
    </div>
    <EmptyState v-if="!loading && list.length === 0" icon="Star" text="还没有收藏,去菜品页逛逛吧">
      <template #action>
        <el-button type="primary" @click="$router.push('/dishes')">浏览菜品</el-button>
      </template>
    </EmptyState>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import DishCard from '@/components/DishCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { favoriteApi } from '@/api'

const loading = ref(false)
const list = ref([])

async function load() {
  loading.value = true
  try {
    const data = await favoriteApi.my()
    list.value = (data || []).map(f => ({ ...f.dish, tags: f.dish?.tags || [] }))
  } finally { loading.value = false }
}

async function onCancel(d) {
  try {
    await ElMessageBox.confirm(`确定取消收藏「${d.name}」?`, '提示', { type: 'warning' })
    await favoriteApi.cancel(d.id)
    list.value = list.value.filter(x => x.id !== d.id)
    ElMessage.success('已取消')
  } catch (e) { /* cancel */ }
}

onMounted(load)
</script>

<style scoped>
.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.fav-wrap { position: relative; }
.cancel-btn {
  margin-top: 8px;
  width: 100%;
}
</style>
