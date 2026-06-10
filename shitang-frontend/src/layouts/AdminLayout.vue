<template>
  <el-container class="admin-layout">
    <el-aside :width="collapse ? '64px' : '220px'" class="aside">
      <div class="logo" @click="$router.push('/admin')">
        <el-icon size="22"><Setting /></el-icon>
        <span v-if="!collapse" class="logo-text">管理后台</span>
      </div>
      <el-menu :default-active="activeMenu" router :collapse="collapse" background-color="#1f2937" text-color="#cbd5e1" active-text-color="#ff8c5a">
        <el-menu-item index="/admin">
          <el-icon><DataLine /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/admin/dishes">
          <el-icon><Bowl /></el-icon>
          <template #title>菜品管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><Tickets /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/canteens">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>食堂管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/windows">
          <el-icon><Grid /></el-icon>
          <template #title>窗口管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>评论管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/eval">
          <el-icon><TrendCharts /></el-icon>
          <template #title>推荐效果评估</template>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><CollectionTag /></el-icon>
          <template #title>标签 / 分类</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-button text :icon="collapse ? Expand : Fold" @click="collapse = !collapse" circle />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-tooltip content="前台">
            <el-button text :icon="Promotion" @click="$router.push('/home')" circle />
          </el-tooltip>
          <el-tooltip content="主题">
            <el-button text :icon="themeIcon" @click="appStore.toggleTheme" circle />
          </el-tooltip>
          <el-tag :type="roleTagType" effect="dark" round style="font-weight:600">
            {{ userStore.userInfo?.realName }} · {{ userStore.userInfo?.role }}
          </el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-info">
              <el-avatar :size="32" style="background: var(--gradient-primary)">
                {{ (userStore.userInfo?.realName || 'A').charAt(0) }}
              </el-avatar>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="front" :icon="Promotion">回到前台</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Setting, DataLine, Bowl, Tickets, TrendCharts, CollectionTag,
  Expand, Fold, Promotion, Sunny, Moon, SwitchButton,
  OfficeBuilding, Grid, ChatDotRound, User
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const collapse = ref(false)

const themeIcon = computed(() => appStore.theme === 'light' ? Moon : Sunny)

const activeMenu = computed(() => {
  const p = route.path
  if (p === '/admin') return '/admin'
  if (p.startsWith('/admin/dishes')) return '/admin/dishes'
  if (p.startsWith('/admin/orders')) return '/admin/orders'
  if (p.startsWith('/admin/canteens')) return '/admin/canteens'
  if (p.startsWith('/admin/windows')) return '/admin/windows'
  if (p.startsWith('/admin/comments')) return '/admin/comments'
  if (p.startsWith('/admin/users')) return '/admin/users'
  if (p.startsWith('/admin/eval')) return '/admin/eval'
  if (p.startsWith('/admin/tags')) return '/admin/tags'
  return p
})

const currentTitle = computed(() => {
  const m = {
    '/admin': '仪表盘',
    '/admin/dishes': '菜品管理',
    '/admin/orders': '订单管理',
    '/admin/canteens': '食堂管理',
    '/admin/windows': '窗口管理',
    '/admin/comments': '评论管理',
    '/admin/users': '用户管理',
    '/admin/eval': '推荐效果评估',
    '/admin/tags': '标签 / 分类'
  }
  return m[activeMenu.value] || ''
})

const roleTagType = computed(() => ({ ADMIN: 'danger', MERCHANT: 'warning', STUDENT: 'success' }[userStore.userInfo?.role] || 'info'))

async function onCommand(cmd) {
  if (cmd === 'front') router.push('/home')
  else if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录?', '提示', { type: 'warning' })
      userStore.logout()
      ElMessage.success('已退出')
      router.push('/login')
    } catch (e) { /* cancel */ }
  }
}
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside {
  background: #1f2937;
  transition: width 0.3s;
  box-shadow: 2px 0 8px rgba(0,0,0,0.08);
}
.logo {
  display: flex; align-items: center; gap: 8px;
  height: 60px; padding: 0 20px;
  color: #fff; font-weight: 700; font-size: 18px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-text { white-space: nowrap; }
.aside :deep(.el-menu) { border-right: none; }

.header {
  background: var(--bg-card);
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  position: sticky; top: 0; z-index: 10;
}
.left, .right { display: flex; align-items: center; gap: 12px; }
.user-info { cursor: pointer; display: inline-flex; align-items: center; }

.main {
  background: var(--bg-page);
  padding: 20px;
  min-height: calc(100vh - 60px);
}
</style>
