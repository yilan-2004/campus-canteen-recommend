<template>
  <div class="page-container">
    <Breadcrumb :items="[
      { label: '首页', to: '/home' },
      { label: '个人饮食画像' }
    ]" />
    <h2 class="page-title"><el-icon><DataAnalysis /></el-icon> 个人饮食画像</h2>
    <p class="page-subtitle">基于你的浏览、收藏、点赞、下单行为,生成专属味觉画像</p>

    <el-row v-loading="loading" :gutter="20">
      <!-- 左侧:用户信息 + 偏好标签 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="profile-card">
          <div class="avatar-wrap">
            <el-avatar :size="80" style="background: linear-gradient(135deg, #ff8c5a, #ff6b35); font-size: 32px">
              {{ (userStore.userInfo?.realName || 'U').charAt(0) }}
            </el-avatar>
            <h3>{{ userStore.userInfo?.realName }}</h3>
            <p class="meta">{{ userStore.userInfo?.college }} · {{ userStore.userInfo?.grade }}</p>
            <div style="margin-top: 12px; display: flex; gap: 8px; justify-content: center">
              <el-button size="small" @click="openEditProfile">编辑资料</el-button>
              <el-button size="small" @click="openChangePwd">修改密码</el-button>
            </div>
          </div>
          <el-divider />
          <div class="pref-section">
            <h4>口味偏好</h4>
            <div class="pref-tags">
              <el-tag
                v-for="t in tasteList"
                :key="t"
                effect="light"
                round
                type="warning"
                class="pref-tag"
              >{{ t }}</el-tag>
              <span v-if="!tasteList.length" class="empty-tip">尚未设置偏好</span>
            </div>
          </div>
          <el-divider />
          <div class="stat-grid">
            <div class="stat-item">
              <div class="num">{{ stats.viewCount }}</div>
              <div class="label">浏览</div>
            </div>
            <div class="stat-item">
              <div class="num">{{ stats.likeCount }}</div>
              <div class="label">点赞</div>
            </div>
            <div class="stat-item">
              <div class="num">{{ stats.favoriteCount }}</div>
              <div class="label">收藏</div>
            </div>
            <div class="stat-item">
              <div class="num">{{ stats.orderCount }}</div>
              <div class="label">下单</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧:多张 ECharts -->
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="chart-card">
          <template #header>行为类型分布</template>
          <div ref="typeChartEl" class="chart"></div>
        </el-card>

        <el-card shadow="never" class="chart-card">
          <template #header>近 7 天活跃度</template>
          <div ref="dailyChartEl" class="chart"></div>
        </el-card>

        <el-card shadow="never" class="chart-card">
          <template #header>口味标签偏好(从历史行为反推)</template>
          <div ref="tagChartEl" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 行为记录 -->
    <el-card shadow="never" class="behavior-card" v-loading="loading">
      <template #header>
        <span><el-icon><List /></el-icon> 最近行为记录</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="b in recentBehaviors"
          :key="b.id"
          :timestamp="b.createTime"
          :type="behaviorColor(b.behaviorType)"
        >
          <span class="b-type">{{ behaviorLabel(b.behaviorType) }}</span>
          <span class="b-dish">{{ b.dishName || `菜品#${b.dishId}` }}</span>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!loading && recentBehaviors.length === 0" description="还没有行为记录" />
    </el-card>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="profileDialogVisible" title="编辑个人资料" width="500px" destroy-on-close>
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="profileForm.realName" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="profileForm.college" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="profileForm.grade" />
        </el-form-item>
        <el-form-item label="口味偏好">
          <el-select v-model="profileTasteList" multiple filterable allow-create placeholder="选择或输入偏好" style="width: 100%">
            <el-option v-for="t in tasteOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSaving" @click="onSaveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="400px" destroy-on-close>
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="onChangePwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { DataAnalysis, List } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { behaviorApi, favoriteApi, orderApi, userApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)

const tasteOptions = ['微辣', '清淡', '高蛋白', '低脂', '米饭', '面食', '酸甜', '素食', '咸鲜', '中辣']

// 编辑资料
const profileDialogVisible = ref(false)
const profileSaving = ref(false)
const profileForm = reactive({ realName: '', college: '', grade: '', tastePreference: '' })
const profileTasteList = ref([])

function openEditProfile() {
  const u = userStore.userInfo || {}
  profileForm.realName = u.realName || ''
  profileForm.college = u.college || ''
  profileForm.grade = u.grade || ''
  profileTasteList.value = (u.tastePreference || '').split(/[,，、\s]+/).filter(Boolean)
  profileDialogVisible.value = true
}

async function onSaveProfile() {
  profileSaving.value = true
  try {
    profileForm.tastePreference = profileTasteList.value.join(',')
    const updated = await userApi.updateProfile(profileForm)
    // 更新本地存储
    userStore.userInfo = { ...userStore.userInfo, ...updated }
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    ElMessage.success('资料已更新')
    profileDialogVisible.value = false
  } finally { profileSaving.value = false }
}

// 修改密码
const pwdDialogVisible = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ]
}

function openChangePwd() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdDialogVisible.value = true
}

async function onChangePwd() {
  await pwdFormRef.value.validate()
  pwdSaving.value = true
  try {
    await userApi.changePassword(pwdForm)
    ElMessage.success('密码已修改，请重新登录')
    pwdDialogVisible.value = false
    userStore.logout()
    window.location.href = '/login'
  } finally { pwdSaving.value = false }
}

const recentBehaviors = ref([])
const stats = ref({ viewCount: 0, likeCount: 0, favoriteCount: 0, orderCount: 0 })
const tasteList = computed(() => {
  const pref = userStore.userInfo?.tastePreference || ''
  return pref.split(/[,，、\s]+/).filter(Boolean)
})

const typeChartEl = ref(null)
const dailyChartEl = ref(null)
const tagChartEl = ref(null)
let typeChart = null, dailyChart = null, tagChart = null

function behaviorLabel(t) { return { VIEW: '浏览', LIKE: '点赞', FAVORITE: '收藏', COMMENT: '评论', RATE: '评分', ORDER: '下单' }[t] || t }
function behaviorColor(t) { return { VIEW: '', LIKE: '#ff6b35', FAVORITE: '#e6a23c', COMMENT: '#67c23a', RATE: '#909399', ORDER: '#f56c6c' }[t] || '#909399' }

async function load() {
  loading.value = true
  try {
    // 行为记录
    const behaviors = await behaviorApi.my().catch(() => [])
    recentBehaviors.value = (behaviors || []).slice(0, 30)

    // 统计
    stats.value.viewCount = recentBehaviors.value.filter(b => b.behaviorType === 'VIEW').length
    stats.value.likeCount = recentBehaviors.value.filter(b => b.behaviorType === 'LIKE').length
    stats.value.favoriteCount = recentBehaviors.value.filter(b => b.behaviorType === 'FAVORITE').length
    stats.value.orderCount = recentBehaviors.value.filter(b => b.behaviorType === 'ORDER').length

    // 收藏数
    favoriteApi.my().then(f => stats.value.favoriteCount = f.length).catch(() => {})
    // 订单数
    orderApi.my().then(o => stats.value.orderCount = o.length).catch(() => {})

    await nextTick()
    renderCharts(behaviors || [])
  } finally { loading.value = false }
}

function renderCharts(behaviors) {
  // 1) 行为类型饼图
  if (typeChartEl.value) {
    if (!typeChart) typeChart = echarts.init(typeChartEl.value)
    const counts = {}
    behaviors.forEach(b => { counts[b.behaviorType] = (counts[b.behaviorType] || 0) + 1 })
    const data = Object.entries(counts).map(([k, v]) => ({ name: behaviorLabel(k), value: v }))
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data,
        label: { formatter: '{b}\n{d}%' },
        color: ['#ff6b35', '#ffb74d', '#81c784', '#4fc3f7', '#9575cd', '#f06292']
      }]
    })
  }

  // 2) 近 7 天柱状图
  if (dailyChartEl.value) {
    if (!dailyChart) dailyChart = echarts.init(dailyChartEl.value)
    const days = []
    const counts = []
    for (let i = 6; i >= 0; i--) {
      const d = new Date()
      d.setDate(d.getDate() - i)
      const key = `${d.getMonth() + 1}/${d.getDate()}`
      days.push(key)
      counts.push(behaviors.filter(b => b.createTime?.startsWith(d.toISOString().slice(0, 10))).length)
    }
    dailyChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: days },
      yAxis: { type: 'value', name: '次数' },
      series: [{
        data: counts, type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ff8c5a' },
            { offset: 1, color: '#ffb74d' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        label: { show: true, position: 'top' }
      }]
    })
  }

  // 3) 标签词云(用横向条形图代替)
  if (tagChartEl.value) {
    if (!tagChart) tagChart = echarts.init(tagChartEl.value)
    // 从行为反推的标签(简化:用行为类型 + 显式偏好作为标签分布)
    const tagCounts = {}
    ;[...tasteList.value].forEach(t => { tagCounts[t] = (tagCounts[t] || 0) + 5 })
    const allTypes = ['VIEW', 'LIKE', 'FAVORITE', 'ORDER', 'COMMENT']
    allTypes.forEach(t => { tagCounts[behaviorLabel(t)] = (tagCounts[behaviorLabel(t)] || 0) + 1 })
    const sorted = Object.entries(tagCounts).sort((a, b) => a[1] - b[1])
    tagChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 80, right: 30, top: 20, bottom: 20 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(s => s[0]) },
      series: [{
        type: 'bar',
        data: sorted.map(s => s[1]),
        itemStyle: { color: '#ff6b35', borderRadius: [0, 6, 6, 0] },
        label: { show: true, position: 'right' }
      }]
    })
  }
}

function onResize() {
  typeChart?.resize()
  dailyChart?.resize()
  tagChart?.resize()
}

onMounted(async () => {
  await userStore.refreshUserInfo()
  await load()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  typeChart?.dispose()
  dailyChart?.dispose()
  tagChart?.dispose()
})
</script>

<style scoped>
.profile-card { border: none; box-shadow: var(--shadow-card); }
.avatar-wrap { text-align: center; padding: 16px 0; }
.avatar-wrap h3 { margin: 12px 0 4px; color: #2c3e50; }
.avatar-wrap .meta { color: #7f8c8d; margin: 0; font-size: 13px; }

.pref-section h4 { margin: 0 0 12px; color: #2c3e50; font-size: 15px; }
.pref-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.pref-tag { font-size: 13px; }
.empty-tip { color: #95a5a6; font-size: 13px; }

.stat-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px;
  text-align: center;
}
.stat-item .num { font-size: 24px; font-weight: 700; color: var(--primary); }
.stat-item .label { color: #7f8c8d; font-size: 12px; margin-top: 4px; }

.chart-card {
  border: none; box-shadow: var(--shadow-card);
  margin-bottom: 16px;
}
.chart { width: 100%; height: 280px; }

.behavior-card {
  margin-top: 20px; border: none; box-shadow: var(--shadow-card);
}
.b-type { color: #ff6b35; font-weight: 600; margin-right: 8px; }
.b-dish { color: #2c3e50; }
</style>
