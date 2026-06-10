<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '我的订单' }
    ]" />
    <h2 class="page-title"><el-icon><Tickets /></el-icon> 我的订单</h2>
    <p class="page-subtitle">共 {{ list.length }} 笔订单</p>

    <!-- 状态筛选 -->
    <el-tabs v-model="statusTab" class="status-tabs">
      <el-tab-pane label="全部" name="ALL" />
      <el-tab-pane label="已支付" name="PAID" />
      <el-tab-pane label="待支付" name="CREATED" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>

    <div v-loading="loading">
      <EmptyState v-if="!loading && filteredList.length === 0" icon="Tickets" :text="statusTab === 'ALL' ? '还没有订单,去菜品页逛逛吧' : '该状态下没有订单'" />

      <el-card v-for="o in filteredList" :key="o.id" shadow="hover" class="order-card" @click="openDetail(o)">
        <div class="order-head">
          <span class="order-no">订单号:#{{ o.id }}</span>
          <el-tag :type="statusType(o.status)" effect="dark" round>{{ statusLabel(o.status) }}</el-tag>
          <el-steps v-if="o.status === 'CREATED'" :active="1" finish-status="success" class="mini-steps">
            <el-step title="已下单" />
            <el-step title="支付" />
            <el-step title="完成" />
          </el-steps>
          <span class="time">{{ o.createTime }}</span>
        </div>
        <div class="order-body">
          <div v-for="item in o.items" :key="item.id" class="order-item" @click="$router.push(`/dishes/${item.dishId}`)">
            <img :src="item.dishImage || fallbackImg" :alt="item.dishName" class="item-img" @error="onImgError" />
            <div class="item-info">
              <div class="item-name">{{ item.dishName }}</div>
              <div class="item-meta">¥{{ Number(item.price).toFixed(2) }} × {{ item.quantity }}</div>
            </div>
            <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
        <div class="order-foot">
          <span class="qty">共 {{ totalQty(o) }} 件</span>
          <div class="right">
            <el-button v-if="o.status === 'CREATED'" type="primary" size="default" @click="onPay(o)">立即支付</el-button>
            <el-button v-if="['CREATED', 'PAID'].includes(o.status)" type="danger" plain size="default" @click="onCancel(o)">取消订单</el-button>
            <el-button v-if="o.status === 'PAID'" type="success" plain size="default" @click="onReorder(o)">再来一单</el-button>
            <span class="total">合计:<b>¥{{ Number(o.totalAmount).toFixed(2) }}</b></span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="订单详情" size="420px" destroy-on-close>
      <template v-if="detailOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">#{{ detailOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detailOrder.status)" effect="dark" round>{{ statusLabel(detailOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="商品总数">{{ totalQty(detailOrder) }} 件</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span style="color: var(--primary); font-weight: 700; font-size: 18px">¥{{ Number(detailOrder.totalAmount).toFixed(2) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 12px; color: var(--text-primary)">商品明细</h4>
        <div v-for="item in detailOrder.items" :key="item.id" class="detail-item" @click="$router.push(`/dishes/${item.dishId}`)">
          <img :src="item.dishImage || fallbackImg" :alt="item.dishName" class="detail-img" @error="onImgError" />
          <div class="detail-info">
            <div class="detail-name">{{ item.dishName }}</div>
            <div class="detail-meta">¥{{ Number(item.price).toFixed(2) }} × {{ item.quantity }}</div>
          </div>
          <div class="detail-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
        </div>

        <h4 style="margin: 20px 0 12px; color: var(--text-primary)">订单状态</h4>
        <el-timeline>
          <el-timeline-item timestamp="下单" placement="top" type="primary">
            {{ detailOrder.createTime }}
          </el-timeline-item>
          <el-timeline-item v-if="detailOrder.status === 'PAID' || detailOrder.status === 'CANCELLED'" :type="detailOrder.status === 'PAID' ? 'success' : 'info'" placement="top">
            {{ detailOrder.status === 'PAID' ? '已支付' : '已取消' }}
          </el-timeline-item>
          <el-timeline-item v-if="detailOrder.status === 'CREATED'" type="warning" placement="top">
            等待支付...
          </el-timeline-item>
        </el-timeline>

        <div class="drawer-actions">
          <el-button v-if="detailOrder.status === 'CREATED'" type="primary" @click="onPay(detailOrder)">立即支付</el-button>
          <el-button v-if="['CREATED', 'PAID'].includes(detailOrder.status)" type="danger" plain @click="onCancel(detailOrder)">取消订单</el-button>
          <el-button v-if="detailOrder.status === 'PAID'" type="success" plain @click="onReorder(detailOrder)">再来一单</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'
import EmptyState from '@/components/EmptyState.vue'
import { orderApi } from '@/api'
import { useAppStore } from '@/store/app'

const loading = ref(false)
const list = ref([])
const statusTab = ref('ALL')
const appStore = useAppStore()
const drawerVisible = ref(false)
const detailOrder = ref(null)
const fallbackImg = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200'
function onImgError(e) { e.target.src = fallbackImg }

function openDetail(o) {
  detailOrder.value = o
  drawerVisible.value = true
}

const filteredList = computed(() => {
  if (statusTab.value === 'ALL') return list.value
  return list.value.filter(o => o.status === statusTab.value)
})

async function load() {
  loading.value = true
  try {
    const data = await orderApi.my()
    list.value = data || []
  } finally { loading.value = false }
}

function totalQty(o) { return (o.items || []).reduce((s, i) => s + i.quantity, 0) }
function statusType(s) { return { PAID: 'success', CREATED: 'warning', CANCELLED: 'info' }[s] || '' }
function statusLabel(s) { return { PAID: '已支付', CREATED: '待支付', CANCELLED: '已取消' }[s] || s }

async function onPay(o) {
  try {
    await ElMessageBox.confirm(`确认支付订单 #${o.id},共 ¥${o.totalAmount}?`, '支付', { type: 'success' })
    await orderApi.updateStatus(o.id, 'PAID')
    o.status = 'PAID'
    ElMessage.success('支付成功')
  } catch (e) { /* cancel */ }
}

async function onCancel(o) {
  try {
    await ElMessageBox.confirm(`确认取消订单 #${o.id}?`, '取消订单', { type: 'warning' })
    await orderApi.updateStatus(o.id, 'CANCELLED')
    o.status = 'CANCELLED'
    ElMessage.success('已取消')
  } catch (e) { /* cancel */ }
}

function onReorder(o) {
  for (const item of (o.items || [])) {
    appStore.addToCart({ id: item.dishId, name: item.dishName, price: item.price, image: item.dishImage })
    // 如果数量>1,额外增加
    for (let i = 1; i < item.quantity; i++) {
      appStore.addToCart({ id: item.dishId, name: item.dishName, price: item.price, image: item.dishImage })
    }
  }
  ElMessage.success('已加入购物车')
}

onMounted(load)
</script>

<style scoped>
.status-tabs { background: var(--bg-card); padding: 4px 16px; border-radius: var(--radius-card); box-shadow: var(--shadow-card); margin-bottom: 16px; }
.order-card {
  margin-bottom: 16px;
  border: none;
  box-shadow: var(--shadow-card);
  transition: all 0.3s;
}
.order-card:hover { box-shadow: var(--shadow-card-hover); }

.order-head {
  display: flex; align-items: center; gap: 12px;
  padding-bottom: 12px; border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
}
.order-no { font-weight: 600; color: var(--text-primary); }
.time { color: var(--text-muted); font-size: 13px; margin-left: auto; }
.mini-steps { flex: 1; min-width: 300px; margin: 0 20px; }

.order-body { padding: 12px 0; }
.order-item {
  display: grid; grid-template-columns: 60px 1fr auto;
  gap: 12px; align-items: center;
  padding: 8px 0;
  cursor: pointer;
  transition: padding 0.2s;
  border-radius: 6px;
}
.order-item:hover { padding-left: 8px; background: var(--primary-soft); }
.item-img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; background: #f5f5f5; }
.item-name { font-weight: 500; color: var(--text-primary); }
.item-meta { color: var(--text-muted); font-size: 13px; margin-top: 4px; }
.item-subtotal { color: var(--primary); font-weight: 600; }

.order-foot {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 12px; border-top: 1px dashed var(--border);
  color: var(--text-secondary); font-size: 14px;
}
.order-foot .qty { color: var(--text-muted); }
.order-foot .right { display: flex; align-items: center; gap: 12px; }
.order-foot .total { color: var(--text-primary); font-size: 16px; margin-left: 8px; }
.order-foot .total b { color: var(--primary); font-size: 22px; }

.detail-item {
  display: grid; grid-template-columns: 50px 1fr auto;
  gap: 12px; align-items: center;
  padding: 10px 0; border-bottom: 1px solid var(--border);
  cursor: pointer;
}
.detail-item:last-child { border-bottom: none; }
.detail-img { width: 50px; height: 50px; border-radius: 8px; object-fit: cover; }
.detail-name { font-weight: 500; color: var(--text-primary); font-size: 14px; }
.detail-meta { color: var(--text-muted); font-size: 12px; margin-top: 2px; }
.detail-subtotal { color: var(--primary); font-weight: 600; }

.drawer-actions { margin-top: 24px; display: flex; gap: 12px; }
</style>
