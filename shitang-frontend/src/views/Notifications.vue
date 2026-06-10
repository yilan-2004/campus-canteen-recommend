<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '消息通知' }
    ]" />
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
      <h2 class="page-title" style="margin: 0"><el-icon><Bell /></el-icon> 消息通知</h2>
      <el-button v-if="list.length > 0" type="primary" text @click="onMarkAllRead">全部已读</el-button>
    </div>

    <el-tabs v-model="tab" class="status-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="未读" name="unread" />
    </el-tabs>

    <div v-loading="loading">
      <EmptyState v-if="!loading && filteredList.length === 0" icon="Bell" :text="tab === 'unread' ? '没有未读消息' : '暂无消息通知'" />

      <el-card
        v-for="n in filteredList"
        :key="n.id"
        shadow="hover"
        class="notification-card"
        :class="{ unread: n.isRead === 0 }"
        @click="onRead(n)"
      >
        <div class="n-head">
          <el-tag :type="typeTag(n.type)" size="small" effect="light">{{ typeLabel(n.type) }}</el-tag>
          <span class="n-time">{{ n.createTime }}</span>
          <el-badge v-if="n.isRead === 0" is-dot class="unread-dot" />
        </div>
        <h4 class="n-title">{{ n.title }}</h4>
        <p class="n-content">{{ n.content }}</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import EmptyState from '@/components/EmptyState.vue'
import { notificationApi } from '@/api'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const loading = ref(false)
const list = ref([])
const tab = ref('all')

const filteredList = computed(() => {
  if (tab.value === 'unread') return list.value.filter(n => n.isRead === 0)
  return list.value
})

function typeLabel(t) { return { ORDER_STATUS: '订单', COMMENT_REPLY: '评论', SYSTEM: '系统' }[t] || t }
function typeTag(t) { return { ORDER_STATUS: 'success', COMMENT_REPLY: 'warning', SYSTEM: 'info' }[t] || '' }

async function load() {
  loading.value = true
  try {
    list.value = await notificationApi.list() || []
  } finally { loading.value = false }
}

async function onRead(n) {
  if (n.isRead === 0) {
    await notificationApi.markRead(n.id)
    n.isRead = 1
    appStore.unreadCount = Math.max(0, appStore.unreadCount - 1)
  }
}

async function onMarkAllRead() {
  await notificationApi.markAllRead()
  list.value.forEach(n => n.isRead = 1)
  appStore.unreadCount = 0
}

onMounted(load)
</script>

<style scoped>
.status-tabs { background: var(--bg-card); padding: 4px 16px; border-radius: var(--radius-card); box-shadow: var(--shadow-card); margin-bottom: 16px; }
.notification-card {
  margin-bottom: 12px; cursor: pointer;
  border: none; box-shadow: var(--shadow-card);
  transition: all 0.3s;
}
.notification-card:hover { box-shadow: var(--shadow-card-hover); }
.notification-card.unread { border-left: 3px solid var(--primary); }
.n-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.n-time { color: var(--text-muted); font-size: 12px; margin-left: auto; }
.unread-dot { margin-left: 4px; }
.n-title { margin: 0 0 4px; color: var(--text-primary); font-size: 15px; }
.n-content { margin: 0; color: var(--text-secondary); font-size: 13px; }
</style>
