<template>
  <div class="dish-card" @click="onClick">
    <div class="image-wrap">
      <img :src="dish.image || fallbackImg" :alt="dish.name" @error="onImgError" />
      <div v-if="rank && rank <= 3" class="rank-badge" :class="{ top3: rank <= 3 }">
        {{ rank }}
      </div>
      <div v-if="dish.reason" class="reason-badge">
        <el-icon><MagicStick /></el-icon>
        <span>{{ dish.reason }}</span>
      </div>
    </div>
    <div class="body">
      <h3 class="name">{{ dish.name }}</h3>
      <div class="price-row">
        <span class="price">¥{{ formatPrice(dish.price) }}</span>
        <el-rate
          v-model="rating"
          disabled
          show-score
          text-color="#ff9900"
          :score-template="dish.avgRating ? dish.avgRating.toFixed(1) : '0.0'"
        />
      </div>
      <div class="meta">
        <span v-if="dish.canteenName"><el-icon><OfficeBuilding /></el-icon>{{ dish.canteenName }}</span>
        <span v-if="dish.windowName">· {{ dish.windowName }}</span>
        <span v-if="dish.salesCount">· 已售 {{ dish.salesCount }}</span>
      </div>
      <div class="tags">
        <el-tag
          v-for="t in (dish.tags || []).slice(0, 3)"
          :key="t.id"
          size="small"
          effect="light"
          round
          :type="tagType(t)"
        >{{ t.name }}</el-tag>
        <el-tag v-if="dish.taste" size="small" type="warning" effect="plain" round>{{ dish.taste }}</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { recommendApi } from '@/api'

const props = defineProps({
  dish: { type: Object, required: true },
  rank: { type: Number, default: null }, // Top N 角标
  showFeedback: { type: Boolean, default: true }
})
const router = useRouter()
const rating = computed(() => props.dish.avgRating || 0)

const fallbackImg = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400'
function onImgError(e) { e.target.src = fallbackImg }
function formatPrice(p) { return Number(p || 0).toFixed(2) }

function tagType(tag) {
  const map = { TASTE: 'danger', NUTRITION: 'success', SCENE: 'warning', OTHER: 'info' }
  return map[tag.type] || 'info'
}

function onClick() {
  if (props.showFeedback && props.dish.reasonType) {
    recommendApi.feedback(props.dish.reasonType.split(':')[0], props.dish.id, 'CLICK').catch(() => {})
  }
  router.push(`/dishes/${props.dish.id}`)
}
</script>
