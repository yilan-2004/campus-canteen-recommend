<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '购物车' }
    ]" />
    <h2 class="page-title"><el-icon><ShoppingCart /></el-icon> 我的购物车</h2>
    <p class="page-subtitle">共 {{ appStore.cartCount }} 件商品,合计 ¥{{ appStore.cartTotal.toFixed(2) }}</p>

    <EmptyState v-if="appStore.cart.length === 0" icon="Box" text="购物车空空如也,去菜品页逛逛吧">
      <template #action>
        <el-button type="primary" @click="$router.push('/dishes')">浏览菜品</el-button>
      </template>
    </EmptyState>

    <div v-else class="cart-wrap">
      <el-card shadow="never" class="cart-list">
        <div v-for="item in appStore.cart" :key="item.id" class="cart-item">
          <img :src="item.image || fallbackImg" :alt="item.name" class="item-img" @error="onImgError" />
          <div class="item-info">
            <div class="item-name" @click="goDetail(item.id)">{{ item.name }}</div>
            <div class="item-price">¥{{ Number(item.price).toFixed(2) }}</div>
          </div>
          <el-input-number
            :model-value="item.quantity"
            :min="0"
            :max="99"
            size="small"
            @change="(v) => appStore.updateCartQty(item.id, v)"
          />
          <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          <el-button text :icon="Delete" type="danger" @click="appStore.removeFromCart(item.id)" circle />
        </div>
      </el-card>

      <el-card shadow="never" class="summary">
        <div class="sum-row">
          <span>商品总数</span>
          <span>{{ appStore.cartCount }} 件</span>
        </div>
        <div class="sum-row total">
          <span>应付总额</span>
          <span class="big">¥{{ appStore.cartTotal.toFixed(2) }}</span>
        </div>
        <div class="sum-actions">
          <el-button @click="appStore.clearCart">清空购物车</el-button>
          <el-button type="primary" :loading="submitting" :icon="Wallet" @click="onCheckout">立即结算</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ShoppingCart, Wallet } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import EmptyState from '@/components/EmptyState.vue'
import { orderApi, recommendApi } from '@/api'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const submitting = ref(false)
const fallbackImg = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200'
function onImgError(e) { e.target.src = fallbackImg }
function goDetail(id) { router.push(`/dishes/${id}`) }

async function onCheckout() {
  if (!userStore.isLogin) { ElMessage.warning('请先登录'); return }
  if (appStore.cart.length === 0) { ElMessage.warning('购物车为空'); return }
  try {
    await ElMessageBox.confirm(`确认结算 ${appStore.cartCount} 件商品,共 ¥${appStore.cartTotal.toFixed(2)}?`, '结算订单', { type: 'success' })
    submitting.value = true
    const items = appStore.cart.map(c => ({ dishId: c.id, quantity: c.quantity }))
    await orderApi.create({ items })
    // 上报每个菜品的 ORDER 转化(从购物车结算)
    for (const c of appStore.cart) {
      recommendApi.feedback('CART_BUY', c.id, 'ORDER').catch(() => {})
    }
    ElMessage.success('下单成功!')
    appStore.clearCart()
    router.push('/orders')
  } catch (e) { /* cancel */ } finally { submitting.value = false }
}
</script>

<style scoped>
.cart-wrap { display: grid; grid-template-columns: 1fr 320px; gap: 20px; }
.cart-list { border: none; box-shadow: var(--shadow-card); }
.cart-item {
  display: grid;
  grid-template-columns: 80px 1fr auto 100px 40px;
  gap: 16px; align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
  transition: background 0.2s;
}
.cart-item:hover { background: var(--primary-soft); }
.cart-item:last-child { border-bottom: none; }
.item-img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; background: #f5f5f5; }
.item-name { font-weight: 600; cursor: pointer; }
.item-name:hover { color: var(--primary); }
.item-price { color: var(--primary); font-weight: 600; margin-top: 4px; }
.item-subtotal { color: var(--primary); font-weight: 700; text-align: right; }

.summary {
  position: sticky; top: 84px;
  align-self: start;
  border: none; box-shadow: var(--shadow-card);
}
.sum-row {
  display: flex; justify-content: space-between;
  padding: 8px 0; color: var(--text-secondary);
}
.sum-row.total {
  border-top: 1px dashed var(--border);
  margin-top: 8px; padding-top: 16px;
  color: var(--text-primary); font-size: 16px;
}
.sum-row.total .big { color: var(--primary); font-size: 26px; font-weight: 700; }
.sum-actions { display: flex; gap: 12px; margin-top: 20px; }
.sum-actions .el-button { flex: 1; height: 44px; font-size: 15px; }

@media (max-width: 768px) {
  .cart-wrap { grid-template-columns: 1fr; }
  .cart-item { grid-template-columns: 60px 1fr 100px 30px; }
  .item-subtotal { display: none; }
}
</style>
