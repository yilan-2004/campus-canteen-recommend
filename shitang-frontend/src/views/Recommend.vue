<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '推荐' }
    ]" />
    <h2 class="page-title">推荐</h2>
    <p class="page-subtitle">基于你的口味偏好和历史行为,智能推荐你可能喜欢的菜品</p>

    <el-tabs v-model="activeTab" @tab-change="onTabChange" class="rec-tabs">
      <el-tab-pane label="为你推荐" name="mixed">
        <template #label>
          <span><el-icon><MagicStick /></el-icon> 为你推荐</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="今日热门" name="hot">
        <template #label>
          <span><el-icon><TrendCharts /></el-icon> 今日热门</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="高分菜品" name="high">
        <template #label>
          <span><el-icon><Star /></el-icon> 高分菜品</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="早餐" name="BREAKFAST" />
      <el-tab-pane label="午餐" name="LUNCH" />
      <el-tab-pane label="晚餐" name="DINNER" />
      <el-tab-pane label="健康轻食" name="HEALTHY" />
    </el-tabs>

    <div v-loading="loading" class="dish-grid">
      <template v-if="loading">
        <SkeletonCard v-for="i in 8" :key="i" />
      </template>
      <template v-else>
        <div v-for="(d, i) in currentList" :key="d.id" class="grid-item" :style="{ animationDelay: (i * 50) + 'ms' }">
          <DishCard :dish="d" :rank="i + 1" />
        </div>
      </template>
    </div>
    <EmptyState v-if="!loading && currentList.length === 0" icon="Bowl" text="暂无推荐数据,先浏览几道菜吧">
      <template #action>
        <el-button type="primary" @click="$router.push('/dishes')">浏览菜品</el-button>
      </template>
    </EmptyState>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import DishCard from '@/components/DishCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { recommendApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
// 未登录默认显示 hot,登录后默认显示 mixed(个性化)
const activeTab = ref(userStore.isLogin ? 'mixed' : 'hot')
const loading = ref(false)

const lists = ref({ mixed: [], hot: [], high: [], BREAKFAST: [], LUNCH: [], DINNER: [], HEALTHY: [] })

const currentList = computed(() => lists.value[activeTab.value] || [])

async function loadAll() {
  if (!userStore.isLogin) {
    ElMessage.warning('登录后查看为你推荐')
  }
  loading.value = true
  try {
    // 通用:hot / high 不需要 userId,所有用户都能看
    load('hot', () => recommendApi.hot(20))
    load('high', () => recommendApi.highScore(20))

    // 场景推荐:userId 选填,匿名也能调
    const uid = userStore.userId || null
    load('BREAKFAST', () => recommendApi.scenario('BREAKFAST', uid, 20))
    load('LUNCH', () => recommendApi.scenario('LUNCH', uid, 20))
    load('DINNER', () => recommendApi.scenario('DINNER', uid, 20))
    load('HEALTHY', () => recommendApi.scenario('HEALTHY', uid, 20))

    // 个性化混合:mixed 接口 userId 必填,只有登录后才调
    if (userStore.isLogin && userStore.userId) {
      load('mixed', () => recommendApi.mixed(userStore.userId, 20))
    }
  } finally {
    loading.value = false
  }
}

function load(key, fetcher) {
  fetcher().then(d => lists.value[key] = d || []).catch((e) => {
    console.warn(`加载 ${key} 失败:`, e?.message || e)
  })
}

onMounted(loadAll)
</script>

<style scoped>
.rec-tabs {
  background: var(--bg-card);
  padding: 8px 16px;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
  margin-bottom: 16px;
}
.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.grid-item {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeUp 0.4s ease forwards;
}
@keyframes fadeUp { to { opacity: 1; transform: translateY(0); } }
</style>
