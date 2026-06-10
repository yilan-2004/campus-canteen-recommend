<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '菜品对比' }
    ]" />
    <h2 class="page-title"><el-icon><DataAnalysis /></el-icon> 菜品对比</h2>
    <p class="page-subtitle">选择两道菜品,直观对比价格、热量、评分等信息</p>

    <!-- 选择菜品 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-select
            v-model="dishId1"
            filterable
            remote
            :remote-method="searchDish1"
            placeholder="搜索菜品 A..."
            style="width: 100%"
            @change="loadDish1"
            clearable
          >
            <el-option v-for="d in options1" :key="d.id" :label="d.name" :value="d.id">
              <span>{{ d.name }}</span>
              <span style="float: right; color: #999; font-size: 12px">¥{{ d.price }}</span>
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="12">
          <el-select
            v-model="dishId2"
            filterable
            remote
            :remote-method="searchDish2"
            placeholder="搜索菜品 B..."
            style="width: 100%"
            @change="loadDish2"
            clearable
          >
            <el-option v-for="d in options2" :key="d.id" :label="d.name" :value="d.id">
              <span>{{ d.name }}</span>
              <span style="float: right; color: #999; font-size: 12px">¥{{ d.price }}</span>
            </el-option>
          </el-select>
        </el-col>
      </el-row>
    </el-card>

    <!-- 对比结果 -->
    <el-row v-if="dish1 && dish2" :gutter="20">
      <!-- 基本信息对比 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="compare-card">
          <template #header>基本信息</template>
          <el-table :data="basicRows" :show-header="false" stripe>
            <el-table-column prop="label" width="100" />
            <el-table-column label="菜品 A">
              <template #default="{ row }">
                <span :class="{ better: row.winner === 'A' }">{{ row.a }}</span>
              </template>
            </el-table-column>
            <el-table-column label="菜品 B">
              <template #default="{ row }">
                <span :class="{ better: row.winner === 'B' }">{{ row.b }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 雷达图 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="compare-card">
          <template #header>综合对比</template>
          <div ref="radarEl" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-else description="请在上方选择两个菜品进行对比" :image-size="120" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { DataAnalysis } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { dishApi } from '@/api'

const dishId1 = ref(null)
const dishId2 = ref(null)
const dish1 = ref(null)
const dish2 = ref(null)
const options1 = ref([])
const options2 = ref([])
const radarEl = ref(null)
let radarChart = null

async function searchDish1(q) {
  options1.value = await dishApi.list({ keyword: q, status: 1 }) || []
}
async function searchDish2(q) {
  options2.value = await dishApi.list({ keyword: q, status: 1 }) || []
}

async function loadDish1(id) {
  dish1.value = id ? await dishApi.detail(id) : null
  await nextTick()
  renderRadar()
}
async function loadDish2(id) {
  dish2.value = id ? await dishApi.detail(id) : null
  await nextTick()
  renderRadar()
}

const basicRows = computed(() => {
  if (!dish1.value || !dish2.value) return []
  const a = dish1.value, b = dish2.value
  const priceA = Number(a.price), priceB = Number(b.price)
  const ratingA = Number(a.avgRating || 0), ratingB = Number(b.avgRating || 0)
  const calA = a.calories || 0, calB = b.calories || 0
  const salesA = a.salesCount || 0, salesB = b.salesCount || 0
  return [
    { label: '名称', a: a.name, b: b.name },
    { label: '价格', a: `¥${priceA.toFixed(2)}`, b: `¥${priceB.toFixed(2)}`, winner: priceA < priceB ? 'A' : priceB < priceA ? 'B' : '' },
    { label: '评分', a: ratingA.toFixed(1), b: ratingB.toFixed(1), winner: ratingA > ratingB ? 'A' : ratingB > ratingA ? 'B' : '' },
    { label: '热量', a: `${calA} kcal`, b: `${calB} kcal`, winner: calA < calB ? 'A' : calB < calA ? 'B' : '' },
    { label: '销量', a: `${salesA} 份`, b: `${salesB} 份`, winner: salesA > salesB ? 'A' : salesB > salesA ? 'B' : '' },
    { label: '口味', a: a.taste || '-', b: b.taste || '-' },
    { label: '窗口', a: a.windowName || '-', b: b.windowName || '-' }
  ]
})

function renderRadar() {
  if (!radarEl.value || !dish1.value || !dish2.value) return
  if (!radarChart) radarChart = echarts.init(radarEl.value)
  const a = dish1.value, b = dish2.value
  const maxPrice = Math.max(Number(a.price), Number(b.price)) * 1.2
  const maxCal = Math.max(a.calories || 600, b.calories || 600) * 1.2
  const maxSales = Math.max(a.salesCount || 100, b.salesCount || 100) * 1.2
  radarChart.setOption({
    tooltip: {},
    legend: { data: [a.name, b.name], bottom: 0 },
    radar: {
      indicator: [
        { name: '价格', max: maxPrice },
        { name: '评分', max: 5 },
        { name: '热量', max: maxCal },
        { name: '销量', max: maxSales },
        { name: '收藏', max: Math.max(a.favoriteCount || 0, b.favoriteCount || 0, 1) * 1.2 },
        { name: '点赞', max: Math.max(a.likeCount || 0, b.likeCount || 0, 1) * 1.2 }
      ]
    },
    series: [{
      type: 'radar',
      data: [
        { value: [Number(a.price), Number(a.avgRating || 0), a.calories || 0, a.salesCount || 0, a.favoriteCount || 0, a.likeCount || 0], name: a.name, areaStyle: { opacity: 0.2 } },
        { value: [Number(b.price), Number(b.avgRating || 0), b.calories || 0, b.salesCount || 0, b.favoriteCount || 0, b.likeCount || 0], name: b.name, areaStyle: { opacity: 0.2 } }
      ]
    }]
  })
}

function onResize() { radarChart?.resize() }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  radarChart?.dispose()
})
</script>

<style scoped>
.compare-card { border: none; box-shadow: var(--shadow-card); margin-bottom: 16px; }
.better { color: var(--primary); font-weight: 700; }
</style>
