<template>
  <div>
    <h2 style="margin: 0 0 16px; color: var(--text-primary)">标签 / 分类管理</h2>

    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>标签({{ tags.length }})</span>
              <el-button type="primary" size="small" :icon="Plus" @click="openTagDialog()">新增</el-button>
            </div>
          </template>
          <el-table :data="tags" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="tagType(row.type)" effect="light">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button type="warning" link size="small" @click="openTagDialog(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="removeTag(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>分类({{ categories.length }})</span>
              <el-button type="primary" size="small" :icon="Plus" @click="openCatDialog()">新增</el-button>
            </div>
          </template>
          <el-table :data="categories" max-height="500">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button type="warning" link size="small" @click="openCatDialog(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="removeCat(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="tagDialog" :title="tagForm.id ? '编辑标签' : '新增标签'" width="400px" destroy-on-close>
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="tagForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="tagForm.type">
            <el-option label="口味 (TASTE)" value="TASTE" />
            <el-option label="营养 (NUTRITION)" value="NUTRITION" />
            <el-option label="场景 (SCENE)" value="SCENE" />
            <el-option label="其它 (OTHER)" value="OTHER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTag">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="catDialog" :title="catForm.id ? '编辑分类' : '新增分类'" width="400px" destroy-on-close>
      <el-form :model="catForm" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="catForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="catForm.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCat">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { tagApi, categoryApi } from '@/api'

const loading = ref(false)
const tags = ref([])
const categories = ref([])
const tagDialog = ref(false)
const tagForm = reactive({ id: null, name: '', type: 'TASTE' })
const catDialog = ref(false)
const catForm = reactive({ id: null, name: '', description: '' })

function tagType(t) { return { TASTE: 'danger', NUTRITION: 'success', SCENE: 'warning', OTHER: 'info' }[t] || 'info' }

async function load() {
  loading.value = true
  try {
    const [t, c] = await Promise.allSettled([tagApi.list(), categoryApi.list()])
    if (t.status === 'fulfilled') tags.value = t.value || []
    if (c.status === 'fulfilled') categories.value = c.value || []
  } finally { loading.value = false }
}

function openTagDialog(row) {
  if (row) Object.assign(tagForm, row)
  else Object.assign(tagForm, { id: null, name: '', type: 'TASTE' })
  tagDialog.value = true
}
async function saveTag() {
  if (!tagForm.name) { ElMessage.warning('请输入名称'); return }
  if (tagForm.id) {
    await tagApi.update(tagForm.id, { name: tagForm.name, type: tagForm.type })
    ElMessage.success('已更新')
  } else {
    await tagApi.create({ name: tagForm.name, type: tagForm.type })
    ElMessage.success('已创建')
  }
  tagDialog.value = false
  load()
}
async function removeTag(row) {
  try {
    await ElMessageBox.confirm(`删除标签「${row.name}」?`, '提示', { type: 'warning' })
    await tagApi.remove(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) { /* */ }
}

function openCatDialog(row) {
  if (row) Object.assign(catForm, row)
  else Object.assign(catForm, { id: null, name: '', description: '' })
  catDialog.value = true
}
async function saveCat() {
  if (!catForm.name) { ElMessage.warning('请输入名称'); return }
  if (catForm.id) {
    await categoryApi.update(catForm.id, { name: catForm.name, description: catForm.description })
    ElMessage.success('已更新')
  } else {
    await categoryApi.create({ name: catForm.name, description: catForm.description })
    ElMessage.success('已创建')
  }
  catDialog.value = false
  load()
}
async function removeCat(row) {
  try {
    await ElMessageBox.confirm(`删除分类「${row.name}」?`, '提示', { type: 'warning' })
    await categoryApi.remove(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) { /* */ }
}

onMounted(load)
</script>
