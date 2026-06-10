<template>
  <div>
    <h2 style="margin: 0 0 20px; color: var(--text-primary)">评论管理</h2>

    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="load">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable @change="load" style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="已隐藏" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="用户" width="100">
          <template #default="{ row }">{{ row.realName || row.username || `U${row.userId}` }}</template>
        </el-table-column>
        <el-table-column label="菜品" width="120">
          <template #default="{ row }">{{ row.dishName || `#${row.dishId}` }}</template>
        </el-table-column>
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <el-rate :model-value="Number(row.rating)" disabled :max="5" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="商家回复" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply" style="color: var(--primary)">{{ row.reply }}</span>
            <span v-else style="color: var(--text-muted)">未回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openReply(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评论" width="500px" destroy-on-close>
      <div style="margin-bottom: 16px">
        <p style="color: var(--text-secondary); margin: 0 0 8px">用户评价:</p>
        <el-card shadow="never" style="background: var(--primary-soft)">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px">
            <el-rate :model-value="Number(replyTarget?.rating)" disabled :max="5" />
            <span style="color: var(--text-muted); font-size: 13px">{{ replyTarget?.realName || replyTarget?.username }}</span>
          </div>
          <p style="margin: 0; color: var(--text-primary)">{{ replyTarget?.content }}</p>
        </el-card>
      </div>
      <el-form label-width="60px">
        <el-form-item label="回复">
          <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="输入商家回复..." :maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replySaving" @click="onReplySubmit">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { commentApi } from '@/api'

const loading = ref(false)
const list = ref([])
const query = reactive({ status: undefined })

const replyDialogVisible = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')
const replySaving = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await commentApi.adminList(query) || []
  } finally { loading.value = false }
}

function openReply(row) {
  replyTarget.value = row
  replyContent.value = row.reply || ''
  replyDialogVisible.value = true
}

async function onReplySubmit() {
  if (!replyContent.value.trim()) { ElMessage.warning('请输入回复内容'); return }
  replySaving.value = true
  try {
    const updated = await commentApi.reply(replyTarget.value.id, replyContent.value)
    replyTarget.value.reply = updated.reply
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
  } finally { replySaving.value = false }
}

onMounted(load)
</script>
