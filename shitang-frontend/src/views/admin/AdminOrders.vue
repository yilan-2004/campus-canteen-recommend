<template>
  <div>
    <h2 style="margin: 0 0 16px; color: var(--text-primary)">订单管理</h2>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="订单号" prop="id" width="100">
          <template #default="{ row }">#{{ row.id }}</template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">{{ row.userRealName || `用户${row.userId}` }}</template>
        </el-table-column>
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div v-for="it in row.items" :key="it.id" class="order-item">
              {{ it.dishName }} × {{ it.quantity }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ Number(row.totalAmount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="160">
          <template #default="{ row }">
            <el-select v-model="row.status" size="small" @change="(v) => onStatusChange(row, v)">
              <el-option label="待支付" value="CREATED" />
              <el-option label="已支付" value="PAID" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" prop="createTime" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api'

const loading = ref(false)
const list = ref([])

async function load() {
  loading.value = true
  try {
    const data = await orderApi.adminList()
    list.value = data || []
  } finally { loading.value = false }
}

async function onStatusChange(row, v) {
  try {
    await orderApi.adminUpdateStatus(row.id, v)
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = row.status === v ? '' : row.status
  }
}

onMounted(load)
</script>

<style scoped>
.order-item { padding: 2px 0; color: var(--text-primary); font-size: 13px; }
.price { color: var(--primary); font-weight: 700; font-size: 16px; }
</style>
