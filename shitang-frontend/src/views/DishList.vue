<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: scenarioLabel || '菜品浏览' }
    ]" />
    <h2 class="page-title">
      <span v-if="scenarioLabel" class="scenario-tag">{{ scenarioLabel }}</span>
      菜品浏览
    </h2>
    <p class="page-subtitle">共 {{ filteredList.length }} 道菜 · 按条件筛选</p>

    <!-- 搜索 + 筛选 -->
    <el-card shadow="never" class="filter-bar">
      <el-form :inline="true" size="default">
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="菜名" clearable style="width:160px" @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="filters.minPrice" :min="0" placeholder="最低" controls-position="right" style="width:110px" />
          <span style="margin: 0 8px">~</span>
          <el-input-number v-model="filters.maxPrice" :min="0" placeholder="最高" controls-position="right" style="width:110px" />
        </el-form-item>
        <el-form-item label="口味">
          <el-select v-model="filters.taste" placeholder="全部" clearable style="width:140px">
            <el-option label="微辣" value="微辣" />
            <el-option label="清淡" value="清淡" />
            <el-option label="酸甜" value="酸甜" />
            <el-option label="咸鲜" value="咸鲜" />
            <el-option label="中辣" value="中辣" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="reload">筛选</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排序 + 视图切换 -->
    <div class="toolbar">
      <el-radio-group v-model="sortBy" size="default">
        <el-radio-button value="default">综合</el-radio-button>
        <el-radio-button value="sales_desc">销量 ↓</el-radio-button>
        <el-radio-button value="rating_desc">评分 ↓</el-radio-button>
        <el-radio-button value="price_asc">价格 ↑</el-radio-button>
        <el-radio-button value="price_desc">价格 ↓</el-radio-button>
      </el-radio-group>
      <div class="view-toggle">
        <el-radio-group v-model="view" size="default">
          <el-radio-button value="grid"><el-icon><Grid /></el-icon></el-radio-button>
          <el-radio-button value="list"><el-icon><List /></el-icon></el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-loading="loading" :class="['dish-container', view]">
      <template v-if="loading">
        <SkeletonCard v-for="i in 8" :key="i" />
      </template>
      <template v-else-if="view === 'grid'">
        <div v-for="(d, i) in filteredList" :key="d.id" class="grid-item" :style="{ animationDelay: (i * 50) + 'ms' }">
          <DishCard :dish="d" />
        </div>
      </template>
      <template v-else>
        <DishListItem v-for="d in filteredList" :key="d.id" :dish="d" />
      </template>
    </div>
    <EmptyState v-if="!loading && filteredList.length === 0" icon="Search" text="暂无符合条件的菜品">
      <template #action>
        <el-button type="primary" @click="reset">清除筛选</el-button>
      </template>
    </EmptyState>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, RefreshLeft, Grid, List } from '@element-plus/icons-vue'
import DishCard from '@/components/DishCard.vue'
import DishListItem from '@/components/DishListItem.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { dishApi, recommendApi } from '@/api'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const sortBy = ref('default')
const view = ref('grid')

const filters = reactive({ keyword: '', minPrice: null, maxPrice: null, taste: '' })
const scenarioCode = ref('')

const scenarioLabel = computed(() => {
  return { BREAKFAST: '早餐', LUNCH: '午餐', DINNER: '晚餐', HEALTHY: '健康轻食' }[scenarioCode.value] || ''
})

const filteredList = computed(() => {
  let arr = [...list.value]
  if (sortBy.value === 'sales_desc') arr.sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0))
  else if (sortBy.value === 'rating_desc') arr.sort((a, b) => (b.avgRating || 0) - (a.avgRating || 0))
  else if (sortBy.value === 'price_asc') arr.sort((a, b) => (a.price || 0) - (b.price || 0))
  else if (sortBy.value === 'price_desc') arr.sort((a, b) => (b.price || 0) - (a.price || 0))
  return arr
})

async function loadList() {
  loading.value = true
  try {
    const params = { status: 1 }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.minPrice != null) params.minPrice = filters.minPrice
    if (filters.maxPrice != null) params.maxPrice = filters.maxPrice
    if (filters.taste) params.taste = filters.taste
    const data = await dishApi.list(params)
    list.value = data || []
  } finally { loading.value = false }
}

async function loadScenario(code) {
  loading.value = true
  try {
    const data = await recommendApi.scenario(code, null, 30)
    list.value = data || []
  } finally { loading.value = false }
}

function reload() { loadList() }
function reset() {
  filters.keyword = ''; filters.minPrice = null; filters.maxPrice = null; filters.taste = ''
  scenarioCode.value = ''
  loadList()
}

watch(() => route.query.scenario, (val) => {
  if (val) {
    scenarioCode.value = String(val)
    loadScenario(scenarioCode.value)
  } else {
    scenarioCode.value = ''
    loadList()
  }
}, { immediate: true })
</script>

<style scoped>
.filter-bar { margin-bottom: 16px; border: none; box-shadow: var(--shadow-card); }
.scenario-tag {
  display: inline-block;
  background: var(--gradient-warm);
  color: #fff;
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 14px;
  margin-right: 12px;
  vertical-align: middle;
}

.toolbar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.view-toggle { display: flex; align-items: center; gap: 8px; }

.dish-container {
  display: grid; gap: 18px;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
}
.dish-container.list {
  grid-template-columns: 1fr;
}

.grid-item {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeUp 0.4s ease forwards;
}
@keyframes fadeUp { to { opacity: 1; transform: translateY(0); } }
</style>
