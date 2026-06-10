<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/home')">
          <el-icon size="24"><Bowl /></el-icon>
          <span>校园食堂推荐</span>
        </div>
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          :ellipsis="false"
          class="menu"
        >
          <el-menu-item index="/home"><el-icon><HomeFilled /></el-icon>首页</el-menu-item>
          <el-menu-item index="/dishes"><el-icon><Reading /></el-icon>菜品</el-menu-item>
          <el-menu-item index="/recommend"><el-icon><MagicStick /></el-icon>推荐</el-menu-item>
          <el-menu-item index="/compare"><el-icon><DataBoard /></el-icon>对比</el-menu-item>
          <el-menu-item index="/favorites"><el-icon><Star /></el-icon>收藏</el-menu-item>
          <el-menu-item index="/orders"><el-icon><Tickets /></el-icon>订单</el-menu-item>
          <el-menu-item index="/profile"><el-icon><DataAnalysis /></el-icon>画像</el-menu-item>
        </el-menu>
        <div class="user-area">
          <el-tooltip content="主题切换" placement="bottom">
            <el-button text :icon="themeIcon" @click="appStore.toggleTheme" circle style="font-size: 18px" />
          </el-tooltip>
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="appStore.unreadCount" :hidden="appStore.unreadCount === 0" :max="99">
              <el-button text :icon="Bell" @click="$router.push('/notifications')" circle style="font-size: 18px" />
            </el-badge>
          </el-tooltip>
          <el-tooltip content="购物车" placement="bottom">
            <el-badge :value="appStore.cartCount" :hidden="appStore.cartCount === 0" :max="99">
              <el-button text :icon="ShoppingCart" @click="$router.push('/cart')" circle style="font-size: 18px" />
            </el-badge>
          </el-tooltip>
          <template v-if="userStore.isLogin">
            <el-dropdown @command="onCommand">
              <span class="user-info">
                <el-avatar :size="32" style="background: var(--gradient-primary)">
                  {{ (userStore.userInfo?.realName || userStore.userInfo?.username || 'U').charAt(0) }}
                </el-avatar>
                <span class="name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
                <el-tag v-if="userStore.userInfo" size="small" :type="roleTagType" effect="light" round>
                  {{ roleLabel }}
                </el-tag>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile" :icon="User">个人画像</el-dropdown-item>
                  <el-dropdown-item command="cart" :icon="ShoppingCart">购物车</el-dropdown-item>
                  <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Sunny, Moon, ShoppingCart, User, SwitchButton, DataBoard, Bell } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const themeIcon = computed(() => appStore.theme === 'light' ? Moon : Sunny)

onMounted(() => {
  if (userStore.isLogin) {
    appStore.loadNotifications()
  }
})

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/dishes')) return '/dishes'
  return p
})

const roleLabel = computed(() => {
  const r = userStore.userInfo?.role
  return { STUDENT: '学生', MERCHANT: '商户', ADMIN: '管理员' }[r] || r
})
const roleTagType = computed(() => {
  return { STUDENT: 'success', MERCHANT: 'warning', ADMIN: 'danger' }[userStore.userInfo?.role] || ''
})

async function onCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'cart') router.push('/cart')
  else if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗?', '提示', { type: 'warning' })
      userStore.logout()
      ElMessage.success('已退出')
      router.push('/login')
    } catch (e) { /* cancel */ }
  }
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  height: 60px !important;
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 0 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
  cursor: pointer;
  white-space: nowrap;
}
.menu { flex: 1; border-bottom: none !important; }
.menu :deep(.el-menu-item) { padding: 0 16px !important; height: 60px; line-height: 60px; }
.user-area { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-info .name { font-size: 14px; color: #2c3e50; }
.main { padding: 0; background: transparent; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
