<template>
  <div class="auth-page">
    <div class="bg-deco">
      <div class="circle c1"></div>
      <div class="circle c2"></div>
      <div class="circle c3"></div>
    </div>
    <div class="auth-card">
      <div class="brand">
        <el-icon size="40" color="#ff6b35"><Bowl /></el-icon>
        <h1>校园食堂推荐</h1>
        <p>登录账号,开启个性化推荐</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="onLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%; height: 44px; font-size: 16px" @click="onLogin">
          登 录
        </el-button>
      </el-form>
      <div class="footer">
        <span>还没有账号?</span>
        <a @click="$router.push('/register')">立即注册</a>
      </div>
      <el-divider><span style="color:#999; font-size: 12px">演示账号(密码均为 123456)</span></el-divider>
      <div class="demo-accounts">
        <el-tag
          v-for="acc in demos"
          :key="acc.username"
          :type="acc.type"
          effect="light"
          class="demo-tag"
          @click="fillDemo(acc)"
        >{{ acc.label }}: {{ acc.username }}</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const demos = [
  { label: '学生', username: 'student1', type: 'success' },
  { label: '学生', username: 'student2', type: 'success' },
  { label: '商户', username: 'merchant1', type: 'warning' },
  { label: '管理员', username: 'admin', type: 'danger' }
]
function fillDemo(acc) { form.username = acc.username; form.password = '123456' }

async function onLogin() {
  await formRef.value.validate()
  try {
    loading.value = true
    await userStore.login(form)
    ElMessage.success('登录成功')
    // 按角色跳转:ADMIN/MERCHANT 走后台,STUDENT 走首页
    const role = userStore.userInfo?.role
    const target = route.query.redirect || (role === 'ADMIN' || role === 'MERCHANT' ? '/admin' : '/home')
    router.push(target)
  } catch (e) {
    // request 拦截器已经提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.bg-deco { position: absolute; inset: 0; pointer-events: none; }
.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
}
.c1 { width: 400px; height: 400px; background: #ff8c5a; top: -100px; left: -100px; }
.c2 { width: 350px; height: 350px; background: #ffb74d; bottom: -80px; right: -80px; }
.c3 { width: 250px; height: 250px; background: #ffab91; top: 50%; left: 50%; transform: translate(-50%, -50%); }
.auth-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.12);
}
.brand { text-align: center; margin-bottom: 28px; }
.brand h1 { font-size: 22px; margin: 12px 0 4px; color: #2c3e50; }
.brand p { color: #7f8c8d; font-size: 13px; margin: 0; }
.footer { text-align: center; margin-top: 16px; font-size: 13px; color: #7f8c8d; }
.footer a { color: var(--primary); cursor: pointer; margin-left: 6px; }
.demo-accounts { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.demo-tag { cursor: pointer; }
</style>
