<template>
  <div>
    <h2 style="margin: 0 0 20px; color: var(--text-primary)">仪表盘</h2>

    <!-- 概览卡片 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="12" :md="6">
        <div class="stat-card" style="--c1: #ff8c5a; --c2: #ff6b35">
          <el-icon size="32"><Bowl /></el-icon>
          <div>
            <div class="num">{{ stats.dishCount }}</div>
            <div class="label">菜品总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="stat-card" style="--c1: #4fc3f7; --c2: #2196f3">
          <el-icon size="32"><Tickets /></el-icon>
          <div>
            <div class="num">{{ stats.orderCount }}</div>
            <div class="label">订单总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="stat-card" style="--c1: #81c784; --c2: #4caf50">
          <el-icon size="32"><User /></el-icon>
          <div>
            <div class="num">{{ stats.userCount }}</div>
            <div class="label">注册用户</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :md="6">
        <div class="stat-card" style="--c1: #ffb74d; --c2: #ff9800">
          <el-icon size="32"><DataLine /></el-icon>
          <div>
            <div class="num">¥{{ stats.revenue }}</div>
            <div class="label">订单总收入</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px" v-loading="loading">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>推荐效果总览(A/B 实验)</template>
          <div class="ab-summary">
            <div class="ab-block a">
              <div class="head">变体 A</div>
              <div class="metrics">
                <div><b>{{ abReport.variantA?.impressions || 0 }}</b><span>曝光</span></div>
                <div><b>{{ abReport.variantA?.clicks || 0 }}</b><span>点击</span></div>
                <div><b>{{ abReport.variantA?.orders || 0 }}</b><span>转化</span></div>
                <div><b>{{ ((abReport.variantA?.ctr || 0) * 100).toFixed(1) }}%</b><span>CTR</span></div>
              </div>
            </div>
            <div class="ab-block b">
              <div class="head">变体 B</div>
              <div class="metrics">
                <div><b>{{ abReport.variantB?.impressions || 0 }}</b><span>曝光</span></div>
                <div><b>{{ abReport.variantB?.clicks || 0 }}</b><span>点击</span></div>
                <div><b>{{ abReport.variantB?.orders || 0 }}</b><span>转化</span></div>
                <div><b>{{ ((abReport.variantB?.ctr || 0) * 100).toFixed(1) }}%</b><span>CTR</span></div>
              </div>
            </div>
          </div>
          <el-alert v-if="abReport.advice" :title="abReport.advice" type="info" :closable="false" show-icon />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>快捷操作</template>
          <div class="quick-actions">
            <el-button type="primary" :icon="Plus" @click="$router.push('/admin/dishes')">菜品管理</el-button>
            <el-button type="success" :icon="Tickets" @click="$router.push('/admin/orders')">订单管理</el-button>
            <el-button type="warning" :icon="TrendCharts" @click="$router.push('/admin/eval')">效果评估</el-button>
            <el-button :icon="CollectionTag" @click="$router.push('/admin/tags')">标签管理</el-button>
            <el-button :icon="OfficeBuilding" @click="$router.push('/admin/canteens')">食堂管理</el-button>
            <el-button :icon="ChatDotRound" @click="$router.push('/admin/comments')">评论管理</el-button>
            <el-button :icon="User" @click="$router.push('/admin/users')">用户管理</el-button>
            <el-button type="info" plain :icon="Promotion" @click="$router.push('/home')">回前台</el-button>
          </div>
          <el-divider />
          <div style="display: flex; gap: 8px; margin-bottom: 12px">
            <el-button size="small" :icon="Download" @click="doExport('dishes')">导出菜品</el-button>
            <el-button size="small" :icon="Download" @click="doExport('orders')">导出订单</el-button>
            <el-button size="small" :icon="Download" @click="doExport('users')">导出用户</el-button>
          </div>
          <p class="tip">系统提示:这是校园食堂推荐系统管理后台,支持菜品/食堂/窗口/标签 CRUD、订单状态流转、评论回复、用户管理、推荐效果评估。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bowl, Tickets, User, DataLine, Plus, TrendCharts, CollectionTag, Promotion, OfficeBuilding, ChatDotRound, Download } from '@element-plus/icons-vue'
import { statsApi, recommendApi, exportApi } from '@/api'

const loading = ref(false)
const stats = ref({ dishCount: 0, orderCount: 0, userCount: 0, revenue: 0 })
const abReport = ref({})

async function load() {
  loading.value = true
  try {
    const [statsData, ab] = await Promise.allSettled([
      statsApi.overview().catch(() => null),
      recommendApi.evalAb(30).catch(() => ({}))
    ])
    if (statsData.status === 'fulfilled' && statsData.value) {
      stats.value = {
        dishCount: statsData.value.dishCount || 0,
        orderCount: statsData.value.orderCount || 0,
        userCount: statsData.value.userCount || 0,
        revenue: Number(statsData.value.revenue || 0).toFixed(2)
      }
    }
    if (ab.status === 'fulfilled') abReport.value = ab.value || {}
  } finally { loading.value = false }
}

async function doExport(type) {
  try {
    const blob = await exportApi[type]()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = { dishes: '菜品列表', orders: '订单列表', users: '用户列表' }[type] + '.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) { ElMessage.error('导出失败') }
}

onMounted(load)
</script>

<style scoped>
.stat-card {
  background: linear-gradient(135deg, var(--c1), var(--c2));
  color: #fff;
  padding: 20px;
  border-radius: 14px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  margin-bottom: 16px;
}
.stat-card .num { font-size: 26px; font-weight: 700; line-height: 1.2; }
.stat-card .label { font-size: 13px; opacity: 0.9; }

.ab-summary { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px; }
.ab-block {
  background: var(--primary-soft);
  padding: 14px;
  border-radius: 10px;
  border-left: 4px solid var(--primary);
}
.ab-block.b { border-left-color: #4fc3f7; background: #e1f5fe; }
.ab-block .head { font-weight: 700; margin-bottom: 8px; color: var(--text-primary); }
.ab-block .metrics { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 12px; font-size: 13px; }
.ab-block .metrics b { color: var(--primary); font-size: 18px; margin-right: 4px; }
.ab-block.b .metrics b { color: #2196f3; }
.ab-block span { color: var(--text-secondary); }

.quick-actions { display: flex; flex-wrap: wrap; gap: 10px; }
.tip { color: var(--text-secondary); font-size: 13px; margin: 0; }
</style>
