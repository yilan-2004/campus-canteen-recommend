<template>
  <div>
    <h2 style="margin: 0 0 20px; color: var(--text-primary)">食堂管理</h2>

    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" @submit.prevent="load">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="食堂名称" clearable @clear="load" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable @change="load" style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="success" @click="openDialog()">新增食堂</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="食堂名称" min-width="140" />
        <el-table-column prop="location" label="位置" min-width="160" />
        <el-table-column prop="openTime" label="营业时间" width="140" />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="dark" round>
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定停用该食堂?" @confirm="onDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small">停用</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editId ? '编辑食堂' : '新增食堂'" width="500px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="食堂名称" prop="name">
          <el-input v-model="form.name" placeholder="如:第一食堂" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如:校园东区" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.openTime" placeholder="如:06:30-21:00" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { canteenApi } from '@/api'

const loading = ref(false)
const list = ref([])
const query = reactive({ keyword: '', status: undefined })

const dialogVisible = ref(false)
const editId = ref(null)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', location: '', openTime: '', description: '', status: 1 })
const rules = {
  name: [{ required: true, message: '请输入食堂名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    list.value = await canteenApi.list(query) || []
  } finally { loading.value = false }
}

function openDialog(row) {
  if (row) {
    editId.value = row.id
    Object.assign(form, { name: row.name, location: row.location, openTime: row.openTime || '', description: row.description || '', status: row.status ?? 1 })
  } else {
    editId.value = null
    Object.assign(form, { name: '', location: '', openTime: '', description: '', status: 1 })
  }
  dialogVisible.value = true
}

async function onSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editId.value) {
      await canteenApi.update(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await canteenApi.create(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally { saving.value = false }
}

async function onDelete(id) {
  await canteenApi.remove(id)
  ElMessage.success('已停用')
  await load()
}

onMounted(load)
</script>
