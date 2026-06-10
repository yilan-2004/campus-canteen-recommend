<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <h2>页面走丢了</h2>
      <p>你访问的页面不存在或已被移除</p>
      <div class="actions">
        <el-button type="primary" @click="$router.push('/home')">返回首页</el-button>
        <el-button @click="$router.back()">返回上页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  display: flex; align-items: center; justify-content: center;
  min-height: 80vh; text-align: center;
}
.code {
  font-size: 120px; font-weight: 900;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  line-height: 1; margin-bottom: 16px;
}
h2 { font-size: 24px; color: var(--text-primary); margin: 0 0 8px; }
p { color: var(--text-muted); margin: 0 0 24px; }
.actions { display: flex; gap: 12px; justify-content: center; }
</style>
