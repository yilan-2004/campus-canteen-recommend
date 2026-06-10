import { defineStore } from 'pinia'
import { cartApi } from '@/api'

export const useAppStore = defineStore('app', {
  state: () => ({
    theme: localStorage.getItem('theme') || 'light', // light / dark
    cart: JSON.parse(localStorage.getItem('cart') || '[]'),
    searchHistory: JSON.parse(localStorage.getItem('searchHistory') || '[]'),
    notifications: [],
    unreadCount: 0
  }),
  getters: {
    cartCount: (s) => s.cart.reduce((sum, c) => sum + (c.quantity || 0), 0),
    cartTotal: (s) => s.cart.reduce((sum, c) => sum + c.price * c.quantity, 0)
  },
  actions: {
    setTheme(t) {
      this.theme = t
      document.documentElement.setAttribute('data-theme', t)
      localStorage.setItem('theme', t)
    },
    toggleTheme() {
      this.setTheme(this.theme === 'light' ? 'dark' : 'light')
    },
    applyTheme() {
      document.documentElement.setAttribute('data-theme', this.theme)
    },
    addToCart(dish) {
      const exist = this.cart.find(c => c.id === dish.id)
      if (exist) exist.quantity += 1
      else this.cart.push({ id: dish.id, name: dish.name, price: dish.price, image: dish.image, quantity: 1 })
      this.persistCart()
      this.syncCartToServer(dish.id, exist ? exist.quantity : 1)
    },
    updateCartQty(dishId, qty) {
      const it = this.cart.find(c => c.id === dishId)
      if (it) {
        if (qty <= 0) {
          this.cart = this.cart.filter(c => c.id !== dishId)
          this.syncRemoveFromServer(dishId)
        } else {
          it.quantity = qty
          this.syncCartToServer(dishId, qty)
        }
        this.persistCart()
      }
    },
    removeFromCart(dishId) {
      this.cart = this.cart.filter(c => c.id !== dishId)
      this.persistCart()
      this.syncRemoveFromServer(dishId)
    },
    clearCart() {
      this.cart = []
      this.persistCart()
      this.syncClearServer()
    },
    persistCart() {
      localStorage.setItem('cart', JSON.stringify(this.cart))
    },
    // 从服务端加载购物车
    async loadCartFromServer() {
      try {
        const serverCart = await cartApi.list()
        if (serverCart && serverCart.length > 0) {
          // 合并服务端购物车到本地
          for (const item of serverCart) {
            const exist = this.cart.find(c => c.id === item.dishId)
            if (exist) {
              exist.quantity = Math.max(exist.quantity, item.quantity)
            } else {
              this.cart.push({ id: item.dishId, name: '', price: 0, image: '', quantity: item.quantity })
            }
          }
          this.persistCart()
        }
      } catch (e) { /* ignore */ }
    },
    // 同步购物车项到服务端
    async syncCartToServer(dishId, quantity) {
      try {
        const token = localStorage.getItem('token')
        if (!token) return
        await cartApi.add(dishId, quantity)
      } catch (e) { /* ignore */ }
    },
    async syncRemoveFromServer(dishId) {
      try {
        const token = localStorage.getItem('token')
        if (!token) return
        await cartApi.remove(dishId)
      } catch (e) { /* ignore */ }
    },
    async syncClearServer() {
      try {
        const token = localStorage.getItem('token')
        if (!token) return
        await cartApi.clear()
      } catch (e) { /* ignore */ }
    },
    // 加载通知
    async loadNotifications() {
      try {
        const token = localStorage.getItem('token')
        if (!token) return
        const { notificationApi } = await import('@/api')
        this.unreadCount = await notificationApi.unreadCount() || 0
      } catch (e) { /* ignore */ }
    },
    addSearchHistory(keyword) {
      if (!keyword || !keyword.trim()) return
      const k = keyword.trim()
      this.searchHistory = [k, ...this.searchHistory.filter(x => x !== k)].slice(0, 10)
      localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory))
    },
    clearSearchHistory() {
      this.searchHistory = []
      localStorage.removeItem('searchHistory')
    }
  }
})
