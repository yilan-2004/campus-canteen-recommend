<template>
  <div class="page-container">
    <!-- 顶部欢迎 Hero -->
    <div class="hero">
      <div class="hero-bg">
        <div class="orb o1"></div>
        <div class="orb o2"></div>
        <div class="orb o3"></div>
      </div>
      <div class="hero-inner">
        <div class="hero-text">
          <div class="badge-emoji">🍱</div>
          <h2>{{ greeting }},{{ userStore.userInfo?.realName || '同学' }} 👋</h2>
          <p>{{ todayTip }}</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" :icon="MagicStick" @click="$router.push('/recommend')">
              看看今天吃什么
            </el-button>
            <el-button size="large" plain :icon="Reading" @click="$router.push('/dishes')">
              浏览全部菜品
            </el-button>
          </div>
        </div>
        <div class="hero-illustration">
          <div class="emoji-big">🍜</div>
        </div>
      </div>
    </div>

    <!-- 场景快捷入口 -->
    <div class="section-title">
      <span class="bar"></span>
      场景推荐
    </div>
    <div class="scenarios">
      <div
        v-for="s in scenarios"
        :key="s.code"
        class="scenario-card"
        :style="{ background: s.bg }"
        @click="goScenario(s.code)"
      >
        <div class="ic"><el-icon size="32"><component :is="s.icon" /></el-icon></div>
        <span>{{ s.label }}</span>
        <el-icon class="arr" size="14"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 猜你喜欢(个性化) -->
    <div v-if="userStore.isLogin" class="section-title">
      <span class="bar"></span>
      <el-icon><MagicStick /></el-icon>
      猜你喜欢
      <span class="more" @click="$router.push('/recommend')">查看更多 <el-icon><ArrowRight /></el-icon></span>
    </div>
    <div v-if="userStore.isLogin" class="dish-grid">
      <template v-if="loadingGuess">
        <SkeletonCard v-for="i in 6" :key="i" />
      </template>
      <template v-else>
        <div v-for="(d, i) in guessList" :key="d.id" class="grid-item" :style="{ animationDelay: (i * 60) + 'ms' }">
          <DishCard :dish="d" :rank="i + 1" />
        </div>
      </template>
    </div>
    <el-card v-else shadow="never" class="login-tip">
      <el-icon><User /></el-icon>
      <span>登录后查看为你量身推荐的菜品</span>
      <el-button type="primary" link @click="$router.push('/login')">立即登录</el-button>
    </el-card>

    <!-- 今日热门 -->
    <div class="section-title">
      <span class="bar"></span>
      <el-icon><TrendCharts /></el-icon>
      今日热门
      <span class="more" @click="$router.push('/dishes')">查看更多 <el-icon><ArrowRight /></el-icon></span>
    </div>
    <div class="dish-grid">
      <template v-if="loadingHot">
        <SkeletonCard v-for="i in 6" :key="i" />
      </template>
      <template v-else>
        <div v-for="(d, i) in hotList" :key="d.id" class="grid-item" :style="{ animationDelay: (i * 60) + 'ms' }">
          <DishCard :dish="d" :rank="i + 1" />
        </div>
      </template>
    </div>

    <!-- 高分菜品 -->
    <div class="section-title">
      <span class="bar"></span>
      <el-icon><Star /></el-icon>
      高分菜品
      <span class="more" @click="$router.push('/dishes')">查看更多 <el-icon><ArrowRight /></el-icon></span>
    </div>
    <div class="dish-grid">
      <template v-if="loadingHigh">
        <SkeletonCard v-for="i in 6" :key="i" />
      </template>
      <template v-else>
        <div v-for="(d, i) in highList" :key="d.id" class="grid-item" :style="{ animationDelay: (i * 60) + 'ms' }">
          <DishCard :dish="d" :rank="i + 1" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  MagicStick, TrendCharts, Star, User,
  Sunny, Moon, Coffee, KnifeFork, Reading, ArrowRight
} from '@element-plus/icons-vue'
import DishCard from '@/components/DishCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { recommendApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const guessList = ref([])
const hotList = ref([])
const highList = ref([])
const loadingGuess = ref(false)
const loadingHot = ref(false)
const loadingHigh = ref(false)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayTip = computed(() => {
  const h = new Date().getHours()
  if (h < 10) return '早餐要吃好,推荐几道营养的~'
  if (h < 14) return '午餐时间到,看看今天吃什么'
  if (h < 17) return '下午茶时间,来点轻食吧'
  if (h < 21) return '晚餐不要将就,犒劳一下自己'
  return '夜宵来一份,但别太油腻哦'
})

const scenarios = [
  { code: 'BREAKFAST', label: '早餐', icon: 'Coffee', bg: 'linear-gradient(135deg,#ffd54f,#ffb74d)' },
  { code: 'LUNCH', label: '午餐', icon: 'KnifeFork', bg: 'linear-gradient(135deg,#ff8a65,#ff6b35)' },
  { code: 'DINNER', label: '晚餐', icon: 'Moon', bg: 'linear-gradient(135deg,#a1887f,#6d4c41)' },
  { code: 'HEALTHY', label: '健康轻食', icon: 'Sunny', bg: 'linear-gradient(135deg,#81c784,#4caf50)' }
]

function goScenario(code) {
  router.push({ path: '/dishes', query: { scenario: code } })
}

async function loadAll() {
  loadingHot.value = true
  loadingHigh.value = true
  recommendApi.hot(6).then(d => hotList.value = d || []).finally(() => loadingHot.value = false)
  recommendApi.highScore(6).then(d => highList.value = d || []).finally(() => loadingHigh.value = false)

  if (userStore.isLogin && userStore.userId) {
    loadingGuess.value = true
    recommendApi.mixed(userStore.userId, 6)
      .then(d => guessList.value = d || [])
      .finally(() => loadingGuess.value = false)
  }
}

onMounted(loadAll)
</script>

<style scoped>
/* Hero 区 */
.hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 24px;
  background: var(--gradient-warm);
  box-shadow: 0 8px 24px rgba(255, 107, 53, 0.18);
}
.hero-bg { position: absolute; inset: 0; overflow: hidden; }
.orb {
  position: absolute; border-radius: 50%;
  filter: blur(40px); opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}
.o1 { width: 300px; height: 300px; background: #fff; top: -100px; left: -50px; }
.o2 { width: 250px; height: 250px; background: #ffeb3b; bottom: -80px; right: 10%; animation-delay: 2s; }
.o3 { width: 200px; height: 200px; background: #fff5e6; top: 30%; right: -60px; animation-delay: 4s; }
@keyframes float {
  0%, 100% { transform: translate(0,0) scale(1); }
  50% { transform: translate(20px, -20px) scale(1.05); }
}

.hero-inner {
  position: relative; z-index: 1;
  display: flex; align-items: center; justify-content: space-between;
  padding: 36px 40px;
  min-height: 200px;
}
.hero-text h2 {
  font-size: 30px; color: #fff; margin: 8px 0;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.hero-text p { color: rgba(255,255,255,0.92); font-size: 16px; margin: 0 0 20px; }
.badge-emoji { font-size: 28px; }
.hero-actions { display: flex; gap: 12px; }
.hero-actions .el-button { backdrop-filter: blur(8px); }
.hero-actions .el-button:first-child {
  background: #fff; color: var(--primary); border: none;
  font-weight: 600;
}
.hero-actions .el-button:first-child:hover { background: #fff5e6; }
.hero-actions .el-button.plain {
  background: rgba(255,255,255,0.2); color: #fff; border-color: rgba(255,255,255,0.4);
}
.hero-actions .el-button.plain:hover { background: rgba(255,255,255,0.3); }
.hero-illustration { font-size: 100px; opacity: 0.85; }
.emoji-big {
  animation: bounce 2s ease-in-out infinite;
  display: inline-block;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

/* 场景卡 */
.scenarios { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 8px; }
.scenario-card {
  position: relative;
  display: flex; align-items: center; gap: 12px;
  padding: 24px;
  border-radius: var(--radius-card);
  color: #fff;
  font-size: 17px; font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
  overflow: hidden;
}
.scenario-card::before {
  content: ''; position: absolute; inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0,0,0,0.1) 100%);
}
.scenario-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-card-hover); }
.scenario-card .ic { background: rgba(255,255,255,0.25); padding: 8px; border-radius: 10px; }
.scenario-card .arr { margin-left: auto; opacity: 0.8; transition: transform 0.2s; }
.scenario-card:hover .arr { transform: translateX(4px); }

/* 网格项入场动画 */
.grid-item {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeUp 0.5s ease forwards;
}
@keyframes fadeUp {
  to { opacity: 1; transform: translateY(0); }
}

/* 登录提示 */
.login-tip {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 8px;
  background: var(--primary-soft);
  border: none;
}

@media (max-width: 768px) {
  .hero-inner { flex-direction: column; text-align: center; padding: 24px; }
  .hero-actions { justify-content: center; }
  .scenarios { grid-template-columns: repeat(2, 1fr); }
  .hero-illustration { display: none; }
}
</style>
