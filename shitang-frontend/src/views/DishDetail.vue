<template>
  <div class="page-container" v-loading="loading" element-loading-text="加载中...">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '菜品浏览', to: '/dishes' },
      { label: dish?.name || '详情' }
    ]" />
    <div v-if="dish" class="detail-wrap">
      <!-- 左侧图片 -->
      <div class="image-side">
        <img :src="dish.image || fallbackImg" :alt="dish.name" @error="onImgError" />
      </div>
      <!-- 右侧信息 -->
      <div class="info-side">
        <h1 class="name">{{ dish.name }}</h1>
        <div class="tags">
          <el-tag
            v-for="t in dish.tags"
            :key="t.id"
            :type="tagType(t)"
            effect="light"
            round
          >{{ t.name }}</el-tag>
          <el-tag v-if="dish.taste" type="warning" effect="plain" round>{{ dish.taste }}</el-tag>
        </div>
        <!-- 饮食禁忌提示 -->
        <el-alert
          v-if="allergyWarnings.length > 0"
          :title="'饮食提示: ' + allergyWarnings.join('、')"
          type="warning"
          show-icon
          :closable="false"
          style="margin-bottom: 12px"
        />
        <div class="price-row">
          <span class="price">¥{{ dish.price }}</span>
          <span class="kcal" v-if="dish.calories">{{ dish.calories }} kcal</span>
        </div>
        <div class="rating-row">
          <el-rate v-model="rating" disabled show-score :score-template="dish.avgRating?.toFixed(1) || '0.0'" />
          <span class="sales">已售 {{ dish.salesCount }} · 收藏 {{ dish.favoriteCount }} · 点赞 {{ dish.likeCount }}</span>
        </div>
        <el-divider />
        <p class="desc">{{ dish.description || '暂无描述' }}</p>
        <p class="loc">📍 {{ dish.canteenName }} · {{ dish.windowName }} · {{ dish.categoryName }}</p>
        <div class="actions">
          <el-button :type="favorited ? 'info' : 'danger'" :icon="Star" @click="onFavorite">
            {{ favorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button :type="liked ? 'info' : 'warning'" :icon="MagicStick" @click="onLike">{{ liked ? '已点赞' : '点赞' }} ({{ dish.likeCount }})</el-button>
          <el-button type="primary" :icon="ShoppingCart" @click="addToCart">加入购物车</el-button>
          <el-button type="success" :icon="Wallet" @click="onBuyNow">立即购买</el-button>
        </div>
      </div>
    </div>

    <!-- 评论区 -->
    <el-card v-if="dish" shadow="never" class="comments">
      <template #header>
        <div class="cmt-head">
          <span><el-icon><ChatDotRound /></el-icon> 菜品评价 ({{ sortedComments.length }})</span>
          <el-radio-group v-model="sortType" size="small">
            <el-radio-button value="time">最新</el-radio-button>
            <el-radio-button value="rating_desc">好评</el-radio-button>
            <el-radio-button value="rating_asc">差评</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 评分分布 -->
      <div v-if="comments.length" class="rating-dist">
        <div class="avg">
          <div class="big">{{ dish.avgRating?.toFixed(1) || '0.0' }}</div>
          <el-rate :model-value="dish.avgRating || 0" disabled />
          <div class="hint">基于 {{ comments.length }} 条评价</div>
        </div>
        <div class="bars">
          <div v-for="n in 5" :key="n" class="bar-row">
            <span class="label">{{ 6 - n }} 星</span>
            <el-progress
              :percentage="(ratingDist[5 - n + 1] || 0) * 100"
              :stroke-width="8"
              :show-text="false"
              :color="n === 1 ? '#67c23a' : '#ff8c5a'"
            />
            <span class="count">{{ ratingDist[5 - n + 1] || 0 }}</span>
          </div>
        </div>
      </div>

      <el-empty v-if="sortedComments.length === 0 && comments.length === 0" description="还没有评价,快来抢沙发~" :image-size="80" />
      <transition-group name="list" tag="div">
        <div v-for="c in sortedComments" :key="c.id" class="comment-item">
          <div class="comment-head">
            <el-avatar :size="32" style="background: var(--gradient-primary)">
              {{ (c.userRealName || `U${c.userId}`).charAt(0) }}
            </el-avatar>
            <span class="who">{{ c.userRealName || `用户${c.userId}` }}</span>
            <el-rate v-model="c.rating" disabled show-score :score-template="c.rating?.toFixed(1)" />
            <span class="when">{{ c.createTime }}</span>
          </div>
          <div class="comment-body">{{ c.content }}</div>
          <div v-if="c.reply" class="reply">商户回复:{{ c.reply }}</div>
        </div>
      </transition-group>

      <el-divider />
      <div v-if="userStore.isLogin" class="comment-form">
        <h4>发表评价</h4>
        <el-rate v-model="newRating" show-text />
        <el-input v-model="newContent" type="textarea" :rows="3" placeholder="说说你的体验..." style="margin-top: 12px" :maxlength="500" show-word-limit />
        <el-button type="primary" style="margin-top: 12px" :loading="submitting" @click="onSubmitComment">提交评价</el-button>
      </div>
      <el-empty v-else description="登录后发表评价" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound
} from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { dishApi, favoriteApi, commentApi, orderApi, recommendApi } from '@/api'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const loading = ref(false)
const dish = ref(null)
const comments = ref([])
const favorited = ref(false)
const liked = ref(false)
const newRating = ref(5)
const newContent = ref('')
const submitting = ref(false)
const sortType = ref('time')

const sortedComments = computed(() => {
  const list = [...comments.value]
  if (sortType.value === 'time') {
    list.sort((a, b) => (b.createTime || '').localeCompare(a.createTime || ''))
  } else if (sortType.value === 'rating_desc') {
    list.sort((a, b) => (b.rating || 0) - (a.rating || 0))
  } else if (sortType.value === 'rating_asc') {
    list.sort((a, b) => (a.rating || 0) - (b.rating || 0))
  }
  return list
})

const ratingDist = computed(() => {
  const dist = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  const total = comments.value.length
  if (total === 0) return dist
  for (const c of comments.value) {
    // 四舍五入到整数星
    const star = Math.max(1, Math.min(5, Math.round(c.rating || 0)))
    dist[star]++
  }
  for (const k in dist) dist[k] = dist[k] / total
  return dist
})

const rating = computed(() => dish.value?.avgRating || 0)
const fallbackImg = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600'

// 饮食禁忌提示:根据 NUTRITION 类型标签生成警告
const allergyWarnings = computed(() => {
  if (!dish.value?.tags) return []
  const warnings = []
  const nutritionTags = dish.value.tags.filter(t => t.type === 'NUTRITION').map(t => t.name)
  const allergenMap = {
    '高蛋白': ['含高蛋白，痛风/肾病患者慎食'],
    '低脂': [],
    '素食': [],
    '高纤维': [],
    '低糖': []
  }
  for (const tag of nutritionTags) {
    const w = allergenMap[tag]
    if (w) warnings.push(...w)
  }
  // 高热量警告
  if (dish.value.calories && dish.value.calories > 800) {
    warnings.push('高热量菜品，注意控制摄入量')
  }
  return warnings
})
function onImgError(e) { e.target.src = fallbackImg }
function tagType(t) {
  return { TASTE: 'danger', NUTRITION: 'success', SCENE: 'warning', OTHER: 'info' }[t.type] || 'info'
}

async function loadDish() {
  loading.value = true
  try {
    const id = route.params.id
    const d = await dishApi.detail(id)
    dish.value = d
    liked.value = !!d.liked
    favorited.value = !!d.favorited
    const c = await commentApi.byDish(id)
    comments.value = c || []
  } finally { loading.value = false }
}

async function onFavorite() {
  if (!userStore.isLogin) { ElMessage.warning('请先登录'); return }
  if (favorited.value) {
    await favoriteApi.cancel(dish.value.id)
    favorited.value = false
    dish.value.favoriteCount = Math.max(0, dish.value.favoriteCount - 1)
    ElMessage.success('已取消收藏')
  } else {
    await favoriteApi.add(dish.value.id)
    favorited.value = true
    dish.value.favoriteCount += 1
    ElMessage.success('已收藏')
  }
}

async function onLike() {
  if (!userStore.isLogin) { ElMessage.warning('请先登录'); return }
  const res = await dishApi.like(dish.value.id)
  dish.value.likeCount = res.dish.likeCount
  liked.value = res.liked
  ElMessage.success(res.liked ? '已点赞 👍' : '已取消点赞')
}

function addToCart() {
  if (!userStore.isLogin) { ElMessage.warning('请先登录'); return }
  appStore.addToCart(dish.value)
  ElMessage.success('已加入购物车')
}

async function onBuyNow() {
  if (!userStore.isLogin) { ElMessage.warning('请先登录'); return }
  try {
    await ElMessageBox.confirm(`确认下单: ${dish.value.name} × 1,共 ¥${dish.value.price}?`, '下单', { type: 'success' })
    await orderApi.create({ items: [{ dishId: dish.value.id, quantity: 1 }] })
    // 上报推荐转化
    recommendApi.feedback('DETAIL_BUY', dish.value.id, 'ORDER').catch(() => {})
    ElMessage.success('下单成功')
    router.push('/orders')
  } catch (e) { /* cancel */ }
}

async function onSubmitComment() {
  if (!newContent.value.trim()) { ElMessage.warning('请输入评价内容'); return }
  submitting.value = true
  try {
    await commentApi.create({ dishId: dish.value.id, content: newContent.value, rating: newRating.value })
    ElMessage.success('评价成功')
    newContent.value = ''
    await loadDish()
  } finally { submitting.value = false }
}

onMounted(loadDish)
</script>

<style scoped>
.detail-wrap {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: var(--shadow-card);
  margin-bottom: 24px;
}
.image-side img { width: 100%; border-radius: 12px; max-height: 480px; object-fit: cover; }
.info-side { display: flex; flex-direction: column; }
.name { font-size: 28px; margin: 0 0 12px; color: #2c3e50; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 16px; }
.price-row { display: flex; align-items: baseline; gap: 16px; margin-bottom: 8px; }
.price { font-size: 32px; font-weight: 700; color: var(--primary); }
.kcal { color: #7f8c8d; font-size: 14px; }
.rating-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.sales { color: #95a5a6; font-size: 13px; }
.desc { color: #2c3e50; line-height: 1.7; font-size: 14px; margin: 0 0 12px; }
.loc { color: #7f8c8d; font-size: 13px; margin: 0 0 16px; }
.actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: auto; }

.comments { margin-bottom: 24px; }
.cmt-head { display: flex; align-items: center; justify-content: space-between; }

.rating-dist {
  display: flex; gap: 32px; align-items: center;
  padding: 16px 0; margin-bottom: 16px;
  background: var(--primary-soft);
  border-radius: 8px; padding: 16px 20px;
}
.rating-dist .avg { text-align: center; min-width: 120px; }
.rating-dist .avg .big { font-size: 36px; font-weight: 700; color: var(--primary); }
.rating-dist .avg .hint { color: var(--text-muted); font-size: 12px; margin-top: 4px; }
.rating-dist .bars { flex: 1; }
.bar-row { display: flex; align-items: center; gap: 12px; margin: 4px 0; }
.bar-row .label { width: 50px; color: var(--text-secondary); font-size: 13px; }
.bar-row .count { width: 30px; text-align: right; color: var(--text-muted); font-size: 12px; }

.comment-item { padding: 16px 0; border-bottom: 1px solid var(--border); }
.comment-item:last-of-type { border-bottom: none; }
.comment-head { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.who { font-weight: 600; color: var(--text-primary); }
.when { color: var(--text-muted); font-size: 12px; margin-left: auto; }
.comment-body { color: var(--text-primary); line-height: 1.6; padding-left: 42px; }
.reply {
  margin: 8px 0 0 42px; padding: 10px 14px;
  background: var(--primary-soft); border-left: 3px solid var(--primary);
  color: var(--text-secondary); font-size: 13px; border-radius: 4px;
}
.comment-form h4 { margin: 0 0 12px; color: var(--text-primary); }

@media (max-width: 768px) {
  .detail-wrap { grid-template-columns: 1fr; }
}
</style>
