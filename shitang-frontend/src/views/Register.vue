<template>
  <div class="auth-page">
    <div class="bg-deco">
      <div class="circle c1"></div>
      <div class="circle c2"></div>
    </div>
    <div class="auth-card">
      <div class="brand">
        <el-icon size="40" color="#ff6b35"><Bowl /></el-icon>
        <h1>注册学生账号</h1>
        <p>告诉我们你的口味,推荐更精准</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="学号" :prefix-icon="Postcard" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" :prefix-icon="UserFilled" clearable />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item prop="college">
              <el-input v-model="form.college" placeholder="学院" :prefix-icon="School" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="grade">
              <el-input v-model="form.grade" placeholder="年级,如 2023级" :prefix-icon="Calendar" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="口味偏好(可多选,逗号分隔)" prop="tastePreference">
          <el-input
            v-model="form.tastePreference"
            placeholder="如:微辣,米饭,高蛋白"
            :prefix-icon="Goblet"
          />
          <div class="taste-suggest">
            <el-tag
              v-for="t in tasteOptions"
              :key="t"
              size="small"
              :type="form.tastePreference.includes(t) ? 'primary' : 'info'"
              effect="light"
              class="taste-tag"
              @click="toggleTaste(t)"
            >{{ t }}</el-tag>
          </div>
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%; height: 44px; font-size: 16px" @click="onRegister">
          注 册
        </el-button>
      </el-form>
      <div class="footer">
        <span>已有账号?</span>
        <a @click="$router.push('/login')">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Postcard, School, Calendar, Goblet } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '', password: '', studentNo: '', realName: '',
  college: '', grade: '', tastePreference: ''
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const tasteOptions = ['微辣', '清淡', '高蛋白', '低脂', '米饭', '面食', '酸甜', '素食']
function toggleTaste(t) {
  const cur = form.tastePreference.split(/[,，、\s]+/).filter(Boolean)
  const idx = cur.indexOf(t)
  if (idx >= 0) cur.splice(idx, 1)
  else cur.push(t)
  form.tastePreference = cur.join(',')
}

async function onRegister() {
  await formRef.value.validate()
  try {
    loading.value = true
    await userStore.register(form)
    ElMessage.success('注册成功,请登录')
    router.push('/login')
  } catch (e) { /* */ } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.bg-deco { position: absolute; inset: 0; pointer-events: none; }
.circle { position: absolute; border-radius: 50%; filter: blur(60px); opacity: 0.4; }
.c1 { width: 400px; height: 400px; background: #ff8c5a; top: -100px; left: -100px; }
.c2 { width: 350px; height: 350px; background: #ffb74d; bottom: -80px; right: -80px; }
.auth-card {
  position: relative; z-index: 1;
  width: 480px; padding: 40px;
  background: #fff; border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.12);
}
.brand { text-align: center; margin-bottom: 20px; }
.brand h1 { font-size: 22px; margin: 12px 0 4px; color: #2c3e50; }
.brand p { color: #7f8c8d; font-size: 13px; margin: 0; }
.taste-suggest { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 6px; }
.taste-tag { cursor: pointer; }
.footer { text-align: center; margin-top: 16px; font-size: 13px; color: #7f8c8d; }
.footer a { color: var(--primary); cursor: pointer; margin-left: 6px; }
</style>
