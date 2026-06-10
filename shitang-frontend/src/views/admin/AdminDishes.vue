<template>
  <div>
    <div class="page-head">
      <h2 style="margin: 0; color: var(--text-primary)">菜品管理</h2>
      <div style="display: flex; gap: 8px">
        <el-button type="success" :icon="Download" @click="onExport">导出 Excel</el-button>
        <el-button type="primary" :icon="Plus" @click="onAdd">新增菜品</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-bar">
      <el-form :inline="true" size="default">
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="菜名" clearable style="width:160px" @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width:120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 批量操作栏 -->
    <el-card v-if="selectedIds.length > 0" shadow="never" style="margin-bottom: 12px; border-left: 3px solid var(--primary)">
      <div style="display: flex; align-items: center; gap: 12px">
        <span style="color: var(--text-secondary)">已选 {{ selectedIds.length }} 项</span>
        <el-button type="success" size="small" @click="batchStatus(1)">批量上架</el-button>
        <el-button type="warning" size="small" @click="batchStatus(0)">批量下架</el-button>
        <el-button type="danger" size="small" @click="batchDelete">批量删除</el-button>
      </div>
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-table :data="list" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="菜品" min-width="240">
          <template #default="{ row }">
            <div class="dish-cell">
              <img :src="row.image || fallbackImg" :alt="row.name" @error="(e) => (e.target.src = fallbackImg)" class="thumb" />
              <div>
                <div class="name">{{ row.name }}</div>
                <div class="meta">{{ row.canteenName }} · {{ row.windowName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" prop="price" width="100">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="评分" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.avgRating" disabled show-score :score-template="row.avgRating?.toFixed(1) || '0.0'" />
          </template>
        </el-table-column>
        <el-table-column label="销量" prop="salesCount" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1" :inactive-value="0"
              @change="(v) => onToggleStatus(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" :icon="View" @click="$router.push(`/dishes/${row.id}`)">预览</el-button>
            <el-button type="warning" link size="small" :icon="Edit" @click="onEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" :icon="Delete" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="reload"
        @size-change="reload"
      />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜品' : '新增菜品'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="100px" size="default">
        <el-form-item label="菜名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="口味">
          <el-select v-model="form.taste" placeholder="选择口味" clearable>
            <el-option label="微辣" value="微辣" />
            <el-option label="中辣" value="中辣" />
            <el-option label="清淡" value="清淡" />
            <el-option label="酸甜" value="酸甜" />
            <el-option label="咸鲜" value="咸鲜" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡路里">
          <el-input-number v-model="form.calories" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="图片">
          <div style="display: flex; align-items: center; gap: 12px; width: 100%">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="onUpload"
              accept="image/*"
            >
              <el-button size="small" :loading="uploading">上传图片</el-button>
            </el-upload>
            <el-input v-model="form.image" placeholder="或输入图片URL" style="flex: 1" />
          </div>
          <img v-if="form.image" :src="form.image" style="margin-top: 8px; max-height: 80px; border-radius: 6px" />
        </el-form-item>
        <el-form-item label="窗口ID" required>
          <el-input-number v-model="form.windowId" :min="1" />
        </el-form-item>
        <el-form-item label="分类ID" required>
          <el-input-number v-model="form.categoryId" :min="1" />
        </el-form-item>
        <el-form-item label="标签ID">
          <el-input v-model="form.tagIdsText" placeholder="逗号分隔,如 1,3,5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshLeft, View, Edit, Delete, Download } from '@element-plus/icons-vue'
import { dishApi, exportApi, uploadApi } from '@/api'

const loading = ref(false)
const list = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const fallbackImg = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200'

const selectedIds = ref([])
const uploading = ref(false)
const filters = reactive({ keyword: '', status: null })

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片大小不能超过 5MB')
  return isImage && isLt5M
}

async function onUpload({ file }) {
  uploading.value = true
  try {
    const res = await uploadApi.image(file)
    form.image = res.url
    ElMessage.success('上传成功')
  } catch (e) { ElMessage.error('上传失败') }
  finally { uploading.value = false }
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function batchStatus(status) {
  const action = status === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确认批量${action} ${selectedIds.value.length} 个菜品?`, '批量操作', { type: 'warning' })
    await Promise.all(selectedIds.value.map(id => dishApi.updateStatus(id, status)))
    ElMessage.success(`已批量${action}`)
    reload()
  } catch (e) { /* cancel */ }
}

async function batchDelete() {
  try {
    await ElMessageBox.confirm(`确认批量删除 ${selectedIds.value.length} 个菜品?不可恢复`, '批量删除', { type: 'error' })
    await Promise.all(selectedIds.value.map(id => dishApi.remove(id)))
    ElMessage.success('已批量删除')
    reload()
  } catch (e) { /* cancel */ }
}

async function onExport() {
  try {
    const blob = await exportApi.dishes()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '菜品列表.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) { ElMessage.error('导出失败') }
}
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, name: '', price: 0, taste: '', calories: 0, description: '', image: '', windowId: 1, categoryId: 1, tagIdsText: '' })

async function reload() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.status != null) params.status = filters.status
    const data = await dishApi.list(params)
    list.value = data || []
    total.value = list.value.length
  } finally { loading.value = false }
}
function reset() { filters.keyword = ''; filters.status = null; page.value = 1; reload() }

function onAdd() {
  Object.assign(form, { id: null, name: '', price: 0, taste: '', calories: 0, description: '', image: '', windowId: 1, categoryId: 1, tagIdsText: '' })
  dialogVisible.value = true
}
function onEdit(row) {
  Object.assign(form, {
    id: row.id, name: row.name, price: row.price, taste: row.taste,
    calories: row.calories, description: row.description, image: row.image,
    windowId: row.windowId, categoryId: row.categoryId,
    tagIdsText: (row.tags || []).map(t => t.id).join(',')
  })
  dialogVisible.value = true
}

async function onSave() {
  if (!form.name || !form.windowId || !form.categoryId) {
    ElMessage.warning('请填菜名 / 窗口ID / 分类ID')
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name, price: form.price, taste: form.taste,
      calories: form.calories, description: form.description, image: form.image,
      windowId: form.windowId, categoryId: form.categoryId,
      status: 1
    }
    if (form.id) {
      await dishApi.update(form.id, payload)
      const tagIds = form.tagIdsText.split(/[,，\s]+/).filter(Boolean).map(Number)
      if (tagIds.length) await dishApi.updateTags(form.id, tagIds)
      ElMessage.success('已更新')
    } else {
      const created = await dishApi.create(payload)
      const newId = created?.id || created?.data?.id
      const tagIds = form.tagIdsText.split(/[,，\s]+/).filter(Boolean).map(Number)
      if (newId && tagIds.length) await dishApi.updateTags(newId, tagIds)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    reload()
  } finally { saving.value = false }
}

async function onToggleStatus(row, v) {
  try {
    await dishApi.updateStatus(row.id, v)
    ElMessage.success(v ? '已上架' : '已下架')
  } catch (e) {
    row.status = v ? 0 : 1
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除「${row.name}」?该操作不可恢复`, '警告', { type: 'error' })
    await dishApi.remove(row.id)
    ElMessage.success('已删除')
    reload()
  } catch (e) { /* cancel */ }
}

onMounted(reload)
</script>

<style scoped>
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.filter-bar { margin-bottom: 16px; border: none; box-shadow: var(--shadow-card); }
.dish-cell { display: flex; align-items: center; gap: 10px; }
.thumb { width: 50px; height: 50px; border-radius: 6px; object-fit: cover; background: #f5f5f5; }
.name { font-weight: 600; color: var(--text-primary); }
.meta { font-size: 12px; color: var(--text-muted); }
</style>
