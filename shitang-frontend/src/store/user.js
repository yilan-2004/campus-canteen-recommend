import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  getters: {
    isLogin: (s) => !!s.token,
    isStudent: (s) => s.userInfo?.role === 'STUDENT',
    userId: (s) => s.userInfo?.id
  },
  actions: {
    async login(form) {
      const data = await authApi.login(form)
      this.token = data.token
      this.userInfo = data.userInfo
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      // 登录后加载购物车和通知
      try {
        const { useAppStore } = await import('@/store/app')
        const appStore = useAppStore()
        await appStore.loadCartFromServer()
        await appStore.loadNotifications()
      } catch (e) { /* ignore */ }
      return data
    },
    async register(form) {
      return authApi.register(form)
    },
    async refreshUserInfo() {
      if (!this.token) return null
      try {
        const info = await authApi.userInfo()
        this.userInfo = info
        localStorage.setItem('userInfo', JSON.stringify(info))
        return info
      } catch (e) {
        return null
      }
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
