import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/global.css'
import { useAppStore } from '@/store/app'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// NProgress 配置
NProgress.configure({ showSpinner: false, speed: 400, minimum: 0.25 })
router.beforeEach((to, from, next) => {
  if (to.path !== from.path) NProgress.start()
  next()
})
router.afterEach(() => NProgress.done())

const app = createApp(App)
// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')

// 启动时应用主题
useAppStore().applyTheme()
