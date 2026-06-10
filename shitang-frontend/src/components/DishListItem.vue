<template>
  <div class="dish-list-item" @click="$router.push(`/dishes/${dish.id}`)">
    <img :src="dish.image || fallbackImg" :alt="dish.name" class="thumb" @error="onImgError" />
    <div class="info">
      <div class="name-row">
        <h3 class="name">{{ dish.name }}</h3>
        <el-rate v-model="rating" disabled show-score :score-template="dish.avgRating?.toFixed(1) || '0.0'" />
      </div>
      <p class="desc">{{ dish.description || '暂无描述' }}</p>
      <div class="meta">
        <span v-if="dish.canteenName">📍 {{ dish.canteenName }} · {{ dish.windowName }}</span>
        <span v-if="dish.salesCount">已售 {{ dish.salesCount }}</span>
        <span v-if="dish.calories">{{ dish.calories }} kcal</span>
      </div>
      <div class="tags">
        <el-tag v-for="t in (dish.tags || []).slice(0, 4)" :key="t.id" size="small" effect="light" round :type="tagType(t)">
          {{ t.name }}
        </el-tag>
      </div>
    </div>
    <div class="action">
      <div class="price">¥{{ Number(dish.price || 0).toFixed(2) }}</div>
      <el-button type="primary" :icon="ArrowRight" round>查看详情</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({ dish: { type: Object, required: true } })
const rating = computed(() => props.dish.avgRating || 0)
const fallbackImg = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400'
function onImgError(e) { e.target.src = fallbackImg }
function tagType(tag) { return { TASTE: 'danger', NUTRITION: 'success', SCENE: 'warning', OTHER: 'info' }[tag.type] || 'info' }
</script>

<style scoped>
.dish-list-item {
  display: grid; grid-template-columns: 200px 1fr 180px;
  gap: 20px; align-items: center;
  background: var(--bg-card);
  border-radius: var(--radius-card);
  padding: 16px;
  margin-bottom: 14px;
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid var(--border);
}
.dish-list-item:hover {
  transform: translateX(4px);
  box-shadow: var(--shadow-card-hover);
}
.thumb { width: 200px; height: 130px; border-radius: 8px; object-fit: cover; background: #f5f5f5; }
.info { min-width: 0; }
.name-row { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.name { font-size: 18px; font-weight: 600; margin: 0; color: var(--text-primary); }
.desc { color: var(--text-secondary); font-size: 13px; margin: 4px 0 8px; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.meta { display: flex; gap: 16px; color: var(--text-muted); font-size: 12px; margin-bottom: 8px; flex-wrap: wrap; }
.tags { display: flex; gap: 4px; flex-wrap: wrap; }
.action { text-align: right; }
.action .price { font-size: 26px; color: var(--primary); font-weight: 700; margin-bottom: 8px; }

@media (max-width: 768px) {
  .dish-list-item { grid-template-columns: 100px 1fr; }
  .action { display: none; }
  .thumb { width: 100px; height: 80px; }
}
</style>
