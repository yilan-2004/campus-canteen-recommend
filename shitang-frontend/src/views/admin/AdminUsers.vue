<template>
  <div>
    <h2 style="margin: 0 0 20px; color: var(--text-primary)">用户管理</h2>

    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="load">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable @clear="load" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable @change="load" style="width: 130px">
            <el-option label="学生" value="STUDENT" />
            <el-option label="商户" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable @change="load" style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="college" label="学院" min-width="140" show-overflow-tooltip />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" effect="dark" round>{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="dark" round>
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openRoleDialog(row)">改角色</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" text size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 修改角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="修改角色" width="400px" destroy-on-close>
      <el-form label-width="60px">
        <el-form-item label="用户">
          <span>{{ editUser?.username }} ({{ editUser?.realName }})</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newRole" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="商户" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'

const loading = ref(false)
const list = ref([])
const query = reactive({ keyword: '', role: undefined, status: undefined })

const roleDialogVisible = ref(false)
const editUser = ref(null)
const newRole = ref('STUDENT')
const saving = ref(false)

function roleType(r) { return { ADMIN: 'danger', MERCHANT: 'warning', STUDENT: 'success' }[r] || 'info' }
function roleLabel(r) { return { ADMIN: '管理员', MERCHANT: '商户', STUDENT: '学生' }[r] || r }

async function load() {
  loading.value = true
  try {
    list.value = await userApi.list(query) || []
  } finally { loading.value = false }
}

function openRoleDialog(row) {
  editUser.value = row
  newRole.value = row.role
  roleDialogVisible.value = true
}

async function onRoleSubmit() {
  saving.value = true
  try {
    await userApi.updateRole(editUser.value.id, newRole.value)
    editUser.value.role = newRole.value
    ElMessage.success('角色已更新')
    roleDialogVisible.value = false
  } finally { saving.value = false }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${row.username}?`, '提示', { type: 'warning' })
    await userApi.updateStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`已${action}`)
  } catch (e) { /* cancel */ }
}

onMounted(load)
</script>
