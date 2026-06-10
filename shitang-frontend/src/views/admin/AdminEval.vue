<template>
  <div>
    <h2 style="margin: 0 0 16px; color: var(--text-primary)">推荐效果评估</h2>

    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>A/B 实验报告</span>
              <el-radio-group v-model="windowDays" size="small" @change="load">
                <el-radio-button :value="7">7 天</el-radio-button>
                <el-radio-button :value="30">30 天</el-radio-button>
                <el-radio-button :value="90">90 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div class="ab-compare">
            <div class="ab-side a">
              <div class="head">🅰️ 变体 A(默认权重)</div>
              <div class="metrics">
                <div class="metric"><div class="num">{{ ab.variantA?.impressions || 0 }}</div><div class="label">曝光</div></div>
                <div class="metric"><div class="num">{{ ab.variantA?.clicks || 0 }}</div><div class="label">点击</div></div>
                <div class="metric"><div class="num">{{ ab.variantA?.orders || 0 }}</div><div class="label">转化</div></div>
                <div class="metric"><div class="num">{{ ((ab.variantA?.ctr || 0) * 100).toFixed(2) }}%</div><div class="label">CTR</div></div>
                <div class="metric"><div class="num">{{ ((ab.variantA?.cvr || 0) * 100).toFixed(2) }}%</div><div class="label">CVR</div></div>
                <div class="metric"><div class="num">{{ ab.variantA?.uniqueUsers || 0 }}</div><div class="label">UV</div></div>
              </div>
            </div>
            <div class="ab-side b">
              <div class="head">🅱️ 变体 B(强内容)</div>
              <div class="metrics">
                <div class="metric"><div class="num">{{ ab.variantB?.impressions || 0 }}</div><div class="label">曝光</div></div>
                <div class="metric"><div class="num">{{ ab.variantB?.clicks || 0 }}</div><div class="label">点击</div></div>
                <div class="metric"><div class="num">{{ ab.variantB?.orders || 0 }}</div><div class="label">转化</div></div>
                <div class="metric"><div class="num">{{ ((ab.variantB?.ctr || 0) * 100).toFixed(2) }}%</div><div class="label">CTR</div></div>
                <div class="metric"><div class="num">{{ ((ab.variantB?.cvr || 0) * 100).toFixed(2) }}%</div><div class="label">CVR</div></div>
                <div class="metric"><div class="num">{{ ab.variantB?.uniqueUsers || 0 }}</div><div class="label">UV</div></div>
              </div>
            </div>
          </div>

          <el-alert v-if="ab.advice" :title="ab.advice" type="info" :closable="false" show-icon style="margin-top: 12px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>离线 NDCG 评估</template>
          <div v-loading="loadingNdcg">
            <el-statistic v-if="ndcg.overall" :value="ndcg.overall" title="NDCG@{{ ndcg.topN || 10 }} (总体)" :precision="4" style="margin-bottom: 12px" />
            <el-table v-if="ndcg.perUser?.length" :data="ndcg.perUser" stripe max-height="320">
              <el-table-column type="index" label="#" width="60" />
              <el-table-column label="用户ID" prop="userId" width="100" />
              <el-table-column label="预测命中数" prop="hits" width="120" />
              <el-table-column label="NDCG" width="120">
                <template #default="{ row }">{{ Number(row.ndcg).toFixed(4) }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="NDCG 样本不足,需更多用户行为数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { recommendApi } from '@/api'

const loading = ref(false)
const loadingNdcg = ref(false)
const windowDays = ref(30)
const ab = ref({})
const ndcg = ref({})

async function load() {
  loading.value = true
  try {
    const r = await recommendApi.evalAb(windowDays.value)
    ab.value = r || {}
  } finally { loading.value = false }

  loadingNdcg.value = true
  try {
    const r = await recommendApi.evalNdcg(10)
    ndcg.value = r || {}
  } finally { loadingNdcg.value = false }
}

onMounted(load)
</script>

<style scoped>
.ab-compare { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.ab-side { padding: 14px; border-radius: 10px; }
.ab-side.a { background: var(--primary-soft); border-left: 4px solid var(--primary); }
.ab-side.b { background: #e1f5fe; border-left: 4px solid #2196f3; }
.head { font-weight: 700; margin-bottom: 10px; color: var(--text-primary); }
.metrics { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.metric { background: rgba(255,255,255,0.6); padding: 8px; border-radius: 6px; text-align: center; }
.metric .num { font-size: 20px; font-weight: 700; color: var(--primary); }
.ab-side.b .metric .num { color: #2196f3; }
.metric .label { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
</style>
